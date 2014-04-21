package org.pavanecce.cmmn.jpa;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.FlushEntityEvent;
import org.hibernate.event.spi.FlushEntityEventListener;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.spi.FlushEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.pavanecce.cmmn.flow.CaseFileItem;
import org.pavanecce.cmmn.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.instance.AbstractSubscriptionManager;
import org.pavanecce.cmmn.instance.CaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.instance.CaseInstance;
import org.pavanecce.cmmn.instance.CaseSubscriptionKey;
import org.pavanecce.cmmn.instance.SubscriptionManager;

public class HibernateSubscriptionManager extends AbstractSubscriptionManager<JpaCaseSubscriptionInfo, JpaCaseFileItemSubscriptionInfo> implements
		SubscriptionManager, PostUpdateEventListener, PreCollectionUpdateEventListener, FlushEntityEventListener, FlushEventListener {

	private static final long serialVersionUID = -9103789384930931973L;
 	@Override
	public void subscribe(CaseInstance process, CaseFileItem item, Object target) {
		Environment env = process.getKnowledgeRuntime().getEnvironment();
		EntityManagerFactory emf = (EntityManagerFactory) env.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
		JpaObjectPersistence p = new JpaObjectPersistence(emf);
		subscribeToUnknownNumberOfObjects(process, item, target, p);
	}

	@Override
	public void unsubscribe(CaseInstance process, CaseFileItem caseFileItem, Object target) {
		Environment env = process.getKnowledgeRuntime().getEnvironment();
		EntityManagerFactory emf = (EntityManagerFactory) env.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
		JpaObjectPersistence p = new JpaObjectPersistence(emf);
		unsubscribeFromUnknownNumberOfObjects(process, p, caseFileItem, target);
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
	}

	public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
	}

	@Override
	protected JpaCaseFileItemSubscriptionInfo createCaseFileItemSubscriptionInfo() {
		return new JpaCaseFileItemSubscriptionInfo();
	}

	@Override
	protected JpaCaseSubscriptionInfo createCaseSubscriptionInfo(Object currentInstance) {
		return new JpaCaseSubscriptionInfo(currentInstance);
	}

	@Override
	protected CaseSubscriptionKey createCaseSubscriptionKey(Object currentInstance) {
		return new JpaCaseSubscriptionKey(currentInstance);
	}

	@Override
	public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
		if (event.getEntityEntry().isExistsInDatabase()) {
			/*
			 * Only interested in existing objects - new ones won't have
			 * subscriptions yet, and the CREATE subscription is stored against
			 * the parent
			 */
			Object entity = event.getEntity();
			JpaCaseSubscriptionKey key = new JpaCaseSubscriptionKey(entity);
			JpaCaseSubscriptionInfo inf = (JpaCaseSubscriptionInfo) event.getSession().get(JpaCaseSubscriptionInfo.class, key);
			if (inf != null) {
				for (CaseFileItemSubscriptionInfo is : inf.getCaseFileItemSubscriptions()) {
					if (is.getTransition() == CaseFileItemTransition.UPDATE) {
						fireUpdateEventIfDirty(event, is);
					} else {
						fireStructuralEvents(event, is);
					}
				}
			}
		}
	}

	protected void fireStructuralEvents(FlushEntityEvent event, CaseFileItemSubscriptionInfo is) {
		for (int i = 0; i < event.getEntityEntry().getLoadedState().length; i++) {

			String propertyName = event.getEntityEntry().getPersister().getPropertyNames()[i];
			if (isMatchingCreateOrDelete(is, propertyName) || isMatchingAddOrRemove(is, propertyName)) {
				if (event.getPropertyValues()[i] instanceof Collection) {
					fireCollectionEvents(event, is, i);
				} else {
					fireSingletonEvents(event, is, i);
				}
			}
		}
	}

	protected boolean isMatchingAddOrRemove(CaseFileItemSubscriptionInfo is, String propertyName) {
		return is.getRelatedItemName() != null
				&& propertyName.equals(is.getRelatedItemName())
				&& (is.getTransition() == CaseFileItemTransition.ADD_CHILD || is.getTransition() == CaseFileItemTransition.REMOVE_CHILD
						|| is.getTransition() == CaseFileItemTransition.ADD_REFERENCE || is.getTransition() == CaseFileItemTransition.REMOVE_REFERENCE);
	}

	protected boolean isMatchingCreateOrDelete(CaseFileItemSubscriptionInfo is, String propertyName) {
		/*
		 * In the case of CREATE and DELETE the itemName is actually the name of
		 * the property on the parent to the child
		 */
		return propertyName.equals(is.getItemName())
				&& (is.getTransition() == CaseFileItemTransition.CREATE || is.getTransition() == CaseFileItemTransition.DELETE);
	}

	protected void fireSingletonEvents(FlushEntityEvent event, CaseFileItemSubscriptionInfo is, int i) {
		Object oldValue = event.hasDatabaseSnapshot()? event.getDatabaseSnapshot()[i]:event.getEntityEntry().getLoadedState()[i];
		Object owner = event.getEntity();
		Object[] deletedState = event.getEntityEntry().getDeletedState();
		Object newValue = event.getPropertyValues()[i];
		if (isEntityValueDirty(oldValue, newValue, event.getSession())) {
			if (oldValue != null) {
				if (is.getTransition() == CaseFileItemTransition.DELETE || is.getTransition() == CaseFileItemTransition.REMOVE_CHILD
						|| is.getTransition() == CaseFileItemTransition.REMOVE_REFERENCE) {
					fireEvent(is, owner, oldValue);
				}
			}
			if (newValue != null) {
				if (is.getTransition() == CaseFileItemTransition.CREATE || is.getTransition() == CaseFileItemTransition.ADD_CHILD
						|| is.getTransition() == CaseFileItemTransition.ADD_REFERENCE) {
					fireEvent(is, owner, newValue);
				}
			}
		}
	}

	private boolean isEntityValueDirty(Object oldValue, Object newValue, EventSource session) {
		if (oldValue == null) {
			return newValue!=null;
		}else if(newValue==null){
			return oldValue!=null;
		}else {
			return  !(oldValue==newValue || oldValue.equals(newValue));
		}
	}

	protected void fireCollectionEvents(FlushEntityEvent event, CaseFileItemSubscriptionInfo is, int i) {
		Collection<?> newState = (Collection<?>) event.getPropertyValues()[i];
		Collection<?> oldState = null;
		if(newState instanceof PersistentCollection){
			Serializable storedSnapshot = ((PersistentCollection) newState).getStoredSnapshot();
			if(storedSnapshot instanceof Map){
				oldState=((Map) storedSnapshot).values();
			}else{
				oldState=(Collection<?>) storedSnapshot;
			}
		}
		if(oldState==null){
			oldState=new HashSet<Object>();
		}
		Object owner = event.getEntity();
		for (Object oldObject : oldState) {
			if (!newState.contains(oldObject)) {
				if (is.getTransition() == CaseFileItemTransition.DELETE || is.getTransition() == CaseFileItemTransition.REMOVE_CHILD
						|| is.getTransition() == CaseFileItemTransition.REMOVE_REFERENCE) {
					fireEvent(is, owner, oldObject);
				}
			}
		}
		for (Object newObject : newState) {
			if (!oldState.contains(newObject)) {
				if (is.getTransition() == CaseFileItemTransition.CREATE || is.getTransition() == CaseFileItemTransition.ADD_CHILD
						|| is.getTransition() == CaseFileItemTransition.ADD_REFERENCE) {
					fireEvent(is, owner, newObject);
				}
			}
		}
	}

	protected void fireUpdateEventIfDirty(FlushEntityEvent event, CaseFileItemSubscriptionInfo is) {
		inner: for (int i = 0; i < event.getEntityEntry().getLoadedState().length; i++) {
			if (!event.getEntityEntry().getPersister().getPropertyTypes()[i].isEntityType()
					&& !event.getEntityEntry().getPersister().getPropertyTypes()[i].isCollectionType()
					&& event.getEntityEntry().getPersister().getPropertyTypes()[i].isDirty(event.getEntityEntry().getLoadedState()[i],
							event.getPropertyValues()[i], event.getSession())) {
				fireEvent(is, event.getEntity(), event.getEntity());
				break inner;
			}
		}
	}

	@Override
	public void onFlush(FlushEvent event) throws HibernateException {
		flushEntityManagers();
	}
}
