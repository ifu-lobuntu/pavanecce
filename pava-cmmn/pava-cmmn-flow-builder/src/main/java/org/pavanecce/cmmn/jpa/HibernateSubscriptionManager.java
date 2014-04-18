package org.pavanecce.cmmn.jpa;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

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
		SubscriptionManager, PostUpdateEventListener, PreCollectionUpdateEventListener {

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
		String[] propertyNames = event.getPersister().getPropertyNames();
		int[] dirtyProperties = event.getDirtyProperties();
		JpaCaseSubscriptionKey key = new JpaCaseSubscriptionKey(event.getEntity());
		JpaCaseSubscriptionInfo inf = (JpaCaseSubscriptionInfo) event.getSession().get(JpaCaseSubscriptionInfo.class, key);
		if (inf != null) {
			for (CaseFileItemSubscriptionInfo is : inf.getCaseFileItemSubscriptions()) {
				for (int i : dirtyProperties) {
					// In the case of CREATE itemName is actually the name
					// of the property on the parent to the child
					if (propertyNames[i].equals(is.getItemName())) {
						if (!(event.getState()[i] instanceof Collection)) {
							if (event.getState()[i] != null) {
								if (is.getTransition() == CaseFileItemTransition.CREATE) {
									fireEvent(is, event.getState()[i]);
								} else if (is.getTransition() == CaseFileItemTransition.ADD_CHILD || is.getTransition() == CaseFileItemTransition.ADD_REFERENCE) {
									fireEvent(is, event.getEntity());
								}
							}
						}
						if (!(event.getOldState()[i] instanceof Collection)) {
							if (event.getOldState()[i] != null) {
								if (is.getTransition() == CaseFileItemTransition.DELETE) {
									fireEvent(is, event.getState()[i]);
								} else if (is.getTransition() == CaseFileItemTransition.REMOVE_CHILD
										|| is.getTransition() == CaseFileItemTransition.REMOVE_REFERENCE) {
									fireEvent(is, event.getEntity());
								}
							}
						}
					}
				}
			}
			// TODO modification of children and references. It is problematic
			// in the current spec as there is no indication WHICH children
			// property or reference property to observe.
		}
	}

	public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
		Object owner = event.getAffectedOwnerOrNull();
		JpaCaseSubscriptionKey key = new JpaCaseSubscriptionKey(owner);
		JpaCaseSubscriptionInfo inf = (JpaCaseSubscriptionInfo) event.getSession().get(JpaCaseSubscriptionInfo.class, key);
		if (inf != null) {
			for (CaseFileItemSubscriptionInfo is : inf.getCaseFileItemSubscriptions()) {
				String[] role = event.getCollection().getRole().split("\\.");
				if (role[role.length - 1].equals(is.getItemName())) {
					Collection<?> newState = (Collection<?>) event.getCollection();
					Serializable storedSnapshot = event.getCollection().getStoredSnapshot();
					Collection<?> oldState;
					if (storedSnapshot instanceof Map) {// ???
						oldState = ((Map) storedSnapshot).values();
					} else {
						oldState = (Collection<?>) storedSnapshot;
					}
					for (Object newObject : newState) {
						if (!oldState.contains(newObject)) {
							if (is.getTransition() == CaseFileItemTransition.CREATE) {
								// In the case of CREATE itemName is actually
								// the name
								// of the property on the parent to the child
								fireEvent(is, newObject);
							} else if (is.getTransition() == CaseFileItemTransition.ADD_CHILD || is.getTransition() == CaseFileItemTransition.ADD_REFERENCE) {
								fireEvent(is, owner);
							}
						}
					}
					for (Object oldObject : oldState) {
						if (!newState.contains(oldObject)) {
							if (is.getTransition() == CaseFileItemTransition.DELETE) {
								// In the case of DELETE itemName is actually
								// the name
								// of the property on the parent to the child
								fireEvent(is, oldObject);
							} else if (is.getTransition() == CaseFileItemTransition.REMOVE_CHILD
									|| is.getTransition() == CaseFileItemTransition.REMOVE_REFERENCE) {
								fireEvent(is, owner);
							}
						}
					}
				}
			}
			// TODO modification of children and references. It is problematic
			// in the current spec as there is no indication WHICH children
			// property or reference property to observe.
		}

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

}
