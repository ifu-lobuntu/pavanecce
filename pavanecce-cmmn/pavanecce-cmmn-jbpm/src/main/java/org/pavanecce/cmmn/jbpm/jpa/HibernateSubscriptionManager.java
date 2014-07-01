package org.pavanecce.cmmn.jbpm.jpa;

import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.ADD_CHILD;
import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.ADD_REFERENCE;
import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.CREATE;
import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.DELETE;
import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.REMOVE_CHILD;
import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.REMOVE_REFERENCE;
import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.UPDATE;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.FlushEntityEvent;
import org.hibernate.event.spi.FlushEntityEventListener;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.spi.FlushEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.type.OneToOneType;
import org.kie.api.runtime.Environment;
import org.pavanecce.cmmn.jbpm.event.AbstractPersistentSubscriptionManager;
import org.pavanecce.cmmn.jbpm.event.CaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.event.CaseSubscriptionKey;
import org.pavanecce.cmmn.jbpm.event.DemarcatedSubscriptionContext;
import org.pavanecce.cmmn.jbpm.event.SubscriptionManager;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.jbpm.infra.OnPartInstanceSubscription;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;
import org.pavanecce.common.jpa.JpaObjectPersistence;
import org.pavanecce.common.util.ObjectPersistence;

public class HibernateSubscriptionManager extends AbstractPersistentSubscriptionManager<JpaCaseSubscriptionInfo, JpaCaseFileItemSubscriptionInfo> implements
		SubscriptionManager, PostInsertEventListener, PostDeleteEventListener, FlushEntityEventListener, FlushEventListener {

	private static final long serialVersionUID = -9103789384930931973L;

	public JpaObjectPersistence getObjectPersistence(CaseInstance process) {
		Environment env = process.getKnowledgeRuntime().getEnvironment();
		return (JpaObjectPersistence) env.get(JpaObjectPersistence.ENV_NAME);
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
		if (event.getEntityEntry().isExistsInDatabase()
				&& !(event.getEntity() instanceof JpaCaseFileItemSubscriptionInfo || event.getEntity() instanceof JpaCaseSubscriptionInfo)) {
			/*
			 * Only interested in existing objects - new ones won't have subscriptions yet, and the CREATE subscription
			 * is stored against the parent
			 */
			Object entity = event.getEntity();
			JpaCaseSubscriptionKey key = new JpaCaseSubscriptionKey(entity);
			JpaCaseSubscriptionInfo inf = (JpaCaseSubscriptionInfo) event.getSession().get(JpaCaseSubscriptionInfo.class, key);
			Set<DirtyOneToOne> dirtyOneToOnes = new HashSet<HibernateSubscriptionManager.DirtyOneToOne>();
			if (inf != null) {
				Set<? extends JpaCaseFileItemSubscriptionInfo> caseFileItemSubscriptions = inf.getCaseFileItemSubscriptions();
				fireEventsFor(event, dirtyOneToOnes, caseFileItemSubscriptions);
			}
			fireEventsFor(event, dirtyOneToOnes, DemarcatedSubscriptionContext.getSubscriptionsInScopeForFor(event.getEntity(), ADD_CHILD, ADD_REFERENCE,
					REMOVE_CHILD, REMOVE_REFERENCE, UPDATE));
			/**
			 * For inverse OneToOnes to always allow for positive dirty comparison, whenever a change is made it needs
			 * to be reflected in the loadedState
			 */
			for (DirtyOneToOne dirtyOneToOne : dirtyOneToOnes) {
				event.getEntityEntry().getLoadedState()[dirtyOneToOne.i] = dirtyOneToOne.newValue;
			}
		}
	}

	protected void fireEventsFor(FlushEntityEvent event, Set<DirtyOneToOne> dirtyOneToOnes,
			Collection<? extends CaseFileItemSubscriptionInfo> caseFileItemSubscriptions) {
		for (CaseFileItemSubscriptionInfo is : caseFileItemSubscriptions) {
			if (is.getTransition() == CaseFileItemTransition.UPDATE) {
				fireUpdateEventIfDirty(event, is);
			} else {
				fireStructuralEvents(event, is, dirtyOneToOnes);
			}
		}
	}

	private void fireStructuralEvents(FlushEntityEvent event, CaseFileItemSubscriptionInfo is, Set<DirtyOneToOne> dirtyOneToOnes) {
		for (int i = 0; i < event.getEntityEntry().getLoadedState().length; i++) {
			String propertyName = event.getEntityEntry().getPersister().getPropertyNames()[i];
			if (isMatchingCreateOrDelete(is, propertyName) || isMatchingAddOrRemove(is, propertyName)) {
				if (event.getPropertyValues()[i] instanceof Collection) {
					fireCollectionEvents(event, is, i);
				} else {
					DirtyOneToOne dirtyProp = fireSingletonEvents(event, is, i);
					if (dirtyProp != null) {
						dirtyOneToOnes.add(dirtyProp);
					}
				}
			}
		}
	}

	private DirtyOneToOne fireSingletonEvents(FlushEntityEvent event, CaseFileItemSubscriptionInfo is, int i) {
		Object oldValue = event.getEntityEntry().getLoadedState()[i];
		Object owner = event.getEntity();
		Object newValue = event.getPropertyValues()[i];
		if (isEntityValueDirty(oldValue, newValue, event.getSession())) {
			if (oldValue != null) {
				if (is.getTransition() == CaseFileItemTransition.DELETE || is.getTransition() == CaseFileItemTransition.REMOVE_CHILD
						|| is.getTransition() == CaseFileItemTransition.REMOVE_REFERENCE) {
					queueEvent(is, owner, oldValue);
				}
			}
			if (newValue != null) {
				if (is.getTransition() == CaseFileItemTransition.CREATE || is.getTransition() == CaseFileItemTransition.ADD_CHILD
						|| is.getTransition() == CaseFileItemTransition.ADD_REFERENCE) {
					queueEvent(is, owner, newValue);
				}
			}
			if (event.getEntityEntry().getPersister().getPropertyTypes()[i] instanceof OneToOneType) {
				return new DirtyOneToOne(newValue, i);
			}
		}
		return null;
	}

	private boolean isEntityValueDirty(Object oldValue, Object newValue, EventSource session) {
		if (oldValue == null) {
			return newValue != null;
		} else if (newValue == null) {
			return oldValue != null;
		} else {
			return !(oldValue == newValue || oldValue.equals(newValue));
		}
	}

	private void fireCollectionEvents(FlushEntityEvent event, CaseFileItemSubscriptionInfo is, int i) {
		Collection<?> newState = (Collection<?>) event.getPropertyValues()[i];
		Collection<?> oldState = null;
		// TODO this forces a read - try to optimize
		newState.size();
		if (newState instanceof PersistentCollection) {
			Serializable storedSnapshot = ((PersistentCollection) newState).getStoredSnapshot();
			if (storedSnapshot instanceof Map) {
				oldState = ((Map<?, ?>) storedSnapshot).values();
			} else {
				oldState = (Collection<?>) storedSnapshot;
			}
		}
		if (oldState == null) {
			oldState = new HashSet<Object>();
		}
		Object owner = event.getEntity();
		for (Object oldObject : oldState) {
			if (!newState.contains(oldObject)) {
				if (is.getTransition() == CaseFileItemTransition.DELETE || is.getTransition() == CaseFileItemTransition.REMOVE_CHILD
						|| is.getTransition() == CaseFileItemTransition.REMOVE_REFERENCE) {
					queueEvent(is, owner, oldObject);
				}
			}
		}
		for (Object newObject : newState) {
			if (!oldState.contains(newObject)) {
				if (is.getTransition() == CaseFileItemTransition.CREATE || is.getTransition() == CaseFileItemTransition.ADD_CHILD
						|| is.getTransition() == CaseFileItemTransition.ADD_REFERENCE) {
					queueEvent(is, owner, newObject);
				}
			}
		}
	}

	private void fireUpdateEventIfDirty(FlushEntityEvent event, CaseFileItemSubscriptionInfo is) {
		for (int i = 0; i < event.getEntityEntry().getLoadedState().length; i++) {
			if (!event.getEntityEntry().getPersister().getPropertyTypes()[i].isEntityType()
					&& !event.getEntityEntry().getPersister().getPropertyTypes()[i].isCollectionType()
					&& event.getEntityEntry().getPersister().getPropertyTypes()[i].isDirty(event.getEntityEntry().getLoadedState()[i],
							event.getPropertyValues()[i], event.getSession())) {
				queueEvent(is, event.getEntity(), event.getEntity());
				break;
			}
		}
	}

	@Override
	public void onFlush(FlushEvent event) throws HibernateException {
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		for (OnPartInstanceSubscription s : DemarcatedSubscriptionContext.getSubscriptionsInScopeForFor(event.getEntity(), DELETE)) {
			queueEvent(s, null, event.getEntity());
		}

	}

	@Override
	public void onPostInsert(PostInsertEvent event) {
		for (OnPartInstanceSubscription s : DemarcatedSubscriptionContext.getSubscriptionsInScopeForFor(event.getEntity(), CREATE)) {
			queueEvent(s, null, event.getEntity());
		}
	}

	@Override
	protected Collection<JpaCaseSubscriptionInfo> getAllSubscriptionsAgainst(CaseInstance caseInstance, ObjectPersistence p) {
		EntityManager em = ((JpaObjectPersistence) p).getEntityManager();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<JpaCaseSubscriptionInfo> q = criteriaBuilder.createQuery(JpaCaseSubscriptionInfo.class);
		Root<JpaCaseFileItemSubscriptionInfo> from = q.from(JpaCaseFileItemSubscriptionInfo.class);
		q.where(criteriaBuilder.equal(from.get("processInstanceId"), criteriaBuilder.literal(caseInstance.getId())));
		q.select(from.<JpaCaseSubscriptionInfo> get("caseSubscription"));
		q.distinct(true);
		Map<JpaCaseSubscriptionKey, JpaCaseSubscriptionInfo> result = new HashMap<JpaCaseSubscriptionKey, JpaCaseSubscriptionInfo>();
		TypedQuery<JpaCaseSubscriptionInfo> typedQuery = em.createQuery(q);
		for (JpaCaseSubscriptionInfo i : typedQuery.getResultList()) {
			result.put(i.getId(), i);
		}
		return result.values();

	}

	private static class DirtyOneToOne {
		int i;
		Object newValue;

		public DirtyOneToOne(Object newValue, int i) {
			super();
			this.newValue = newValue;
			this.i = i;
		}

	}

}
