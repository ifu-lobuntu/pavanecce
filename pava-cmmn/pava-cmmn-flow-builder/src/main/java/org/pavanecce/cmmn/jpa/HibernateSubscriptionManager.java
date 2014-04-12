package org.pavanecce.cmmn.jpa;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.persistence.PersistenceContextManager;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.pavanecce.cmmn.flow.Case;
import org.pavanecce.cmmn.flow.CaseFileItem;
import org.pavanecce.cmmn.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.flow.OnCaseFileItemPart;
import org.pavanecce.cmmn.instance.CaseFileItemEvent;
import org.pavanecce.cmmn.instance.CaseInstance;
import org.pavanecce.cmmn.instance.CaseInstanceFactory;
import org.pavanecce.cmmn.instance.SubscriptionManager;

public class HibernateSubscriptionManager implements SubscriptionManager, PostUpdateEventListener, PreCollectionUpdateEventListener{

	@Override
	public void subscribe(CaseInstance process, CaseFileItem item, Object target) {
		Environment env = process.getKnowledgeRuntime().getEnvironment();
		EntityManagerFactory emf = (EntityManagerFactory) env.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
		EntityManager em = emf.createEntityManager();
		subscribeToUnknownNumberOfObjects(process, item, target, em);
	}

	private void subscribeToUnknownNumberOfObjects(CaseInstance process, CaseFileItem caseFileItem, Object val, EntityManager em) {
		if (val instanceof Collection) {
			Collection<?> coll = (Collection<?>) val;
			for (Object child : coll) {
				subscribeToSingleObject(process, caseFileItem, child, em);
			}
		} else if (val != null) {
			subscribeToSingleObject(process, caseFileItem, val, em);
		}
	}

	private void subscribeToSingleObject(CaseInstance process, CaseFileItem caseFileItem, Object currentInstance, EntityManager em) {
		CaseSubscriptionKey key = new CaseSubscriptionKey(currentInstance);
		CaseSubscriptionInfo info = em.find(CaseSubscriptionInfo.class, key);
		if (info == null) {
			info = new CaseSubscriptionInfo(currentInstance);
			em.persist(info);
		}
		Case theCase = (Case) process.getProcess();
		storeVariable(process, caseFileItem, currentInstance);
		Set<OnCaseFileItemPart> onCaseFileItemParts = theCase.findCaseFileItemOnPartsFor(caseFileItem);
		for (OnCaseFileItemPart part : onCaseFileItemParts) {
			switch (part.getStandardEvent()) {
			case ADD_CHILD:
			case ADD_REFERENCE:
			case REMOVE_CHILD:
			case REMOVE_REFERENCE:
			case REPLACE:
			case UPDATE:
				// These are actually pretty useless until CMMN finds a way to
				// make the affected property available in the event
				// subscription
			case DELETE:
				new CaseFileItemSubscriptionInfo(info, caseFileItem.getName(), part.getStandardEvent(), process);
				break;
			default:
				break;
			}
		}
		for (CaseFileItem childItem : caseFileItem.getChildren()) {
			Set<OnCaseFileItemPart> on = theCase.findCaseFileItemOnPartsFor(childItem);
			for (OnCaseFileItemPart part : on) {
				if (part.getStandardEvent() == CaseFileItemTransition.CREATE) {
					// Only the parent object will know if this child has been
					// created
					new CaseFileItemSubscriptionInfo(info, childItem.getName(), part.getStandardEvent(), process);
				}
			}
		}
		cascadeSubscribe(process, currentInstance, caseFileItem.getChildren(), em);
		cascadeSubscribe(process, currentInstance, caseFileItem.getTargets(), em);
	}

	private void storeVariable(CaseInstance process, CaseFileItem caseFileItem, Object target) {
		if (caseFileItem.isCollection()) {
			Collection<Object> variable = (Collection<Object>) process.getVariable(caseFileItem.getName());
			if (variable == null) {
				variable = new HashSet<Object>();
				process.setVariable(caseFileItem.getName(), variable);
			}
			// TODO think about the possibility of the objects being read from
			// different entityManagers
			variable.add(target);
		} else {
			process.setVariable(caseFileItem.getName(), target);
		}
	}

	private void cascadeSubscribe(CaseInstance process, Object target, List<CaseFileItem> associatied, EntityManager em) {
		for (CaseFileItem caseFileItem : associatied) {
			String propName = caseFileItem.getName();
			try {
				Method getter = target.getClass().getMethod("get" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1));
				subscribeToUnknownNumberOfObjects(process, caseFileItem, getter.invoke(target), em);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void unsubscribe(CaseInstance process, CaseFileItem caseFileItem, Object target) {
		Environment env = process.getKnowledgeRuntime().getEnvironment();
		EntityManagerFactory emf = (EntityManagerFactory) env.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
		EntityManager em = emf.createEntityManager();
		unsubscribeFromUnknownNumberOfObjects(process, em, caseFileItem, target);
	}

	private void unsubscribeFromUnknownNumberOfObjects(CaseInstance process, EntityManager em, CaseFileItem caseFileItem, Object val) {
		if (val instanceof Collection) {
			Collection<?> coll = (Collection<?>) val;
			for (Object object : coll) {
				unsubscribe(process, caseFileItem, object, em);
			}
		} else if (val != null) {
			unsubscribe(process, caseFileItem, val, em);

		}
	}

	private void cascadeUnsubscribe(CaseInstance process, Object target, List<CaseFileItem> children, EntityManager em) {
		for (CaseFileItem caseFileItem : children) {
			String propName = caseFileItem.getName();
			try {
				Method getter = target.getClass().getMethod("get" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1));
				Object val = getter.invoke(target);
				unsubscribeFromUnknownNumberOfObjects(process, em, caseFileItem, val);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void unsubscribe(CaseInstance process, CaseFileItem caseFileItem, Object target, EntityManager em) {
		CaseSubscriptionKey key = new CaseSubscriptionKey(target);
		CaseSubscriptionInfo info = em.find(CaseSubscriptionInfo.class, key);
		if (info != null) {
			for (CaseFileItemSubscriptionInfo s : new ArrayList<CaseFileItemSubscriptionInfo>(info.getCaseFileItemSubscriptions())) {
				if (s.getProcessId() == process.getId()) {
					em.remove(s);
					info.getCaseFileItemSubscriptions().remove(s);
				}
			}
		}
		cascadeUnsubscribe(process, target, caseFileItem.getChildren(), em);
		cascadeUnsubscribe(process, target, caseFileItem.getTargets(), em);
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		String[] propertyNames = event.getPersister().getPropertyNames();
		int[] dirtyProperties = event.getDirtyProperties();
		CaseSubscriptionKey key = new CaseSubscriptionKey(event.getEntity());
		CaseSubscriptionInfo inf = (CaseSubscriptionInfo) event.getSession().get(CaseSubscriptionInfo.class, key);
		if (inf != null) {
			for (CaseFileItemSubscriptionInfo is : inf.getCaseFileItemSubscriptions()) {
				if (is.getTransition() == CaseFileItemTransition.CREATE) {
					for (int i : dirtyProperties) {
						// In the case of CREATE itemName is actually the name
						// of the property on the parent to the child
						if (propertyNames[i].equals(is.getItemName())) {
							if (event.getState()[i] instanceof Collection) {
								Collection<?> newState = (Collection<?>) event.getState()[i];
								Collection<?> oldState = (Collection<?>) event.getOldState()[i];
								for (Object newObject : newState) {
									if (!oldState.contains(newObject)) {
										fireEvent(is, newObject);
									}
								}
							} else {
								fireEvent(is, event.getState()[i]);
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

	private void fireEvent(CaseFileItemSubscriptionInfo is, Object value) {
		InternalKnowledgeRuntime eventManager = CaseInstanceFactory.getEventManager(is.getCaseKey());
		String eventType = OnCaseFileItemPart.getType(is.getItemName(), is.getTransition());
		PersistenceContextManager em =  (PersistenceContextManager) eventManager.getEnvironment().get(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER);
		em.beginCommandScopedEntityManager();
		eventManager.signalEvent(eventType, new CaseFileItemEvent(is.getItemName(), is.getTransition(), value), is.getProcessId());
		em.endCommandScopedEntityManager();
	}

	public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
		Object owner = event.getAffectedOwnerOrNull();
		CaseSubscriptionKey key = new CaseSubscriptionKey(owner);
		CaseSubscriptionInfo inf = (CaseSubscriptionInfo) event.getSession().get(CaseSubscriptionInfo.class, key);
		if (inf != null) {
			for (CaseFileItemSubscriptionInfo is : inf.getCaseFileItemSubscriptions()) {
				if (is.getTransition() == CaseFileItemTransition.CREATE) {
					// In the case of CREATE itemName is actually the name
					// of the property on the parent to the child
					String[] role = event.getCollection().getRole().split("\\.");
					if (role[role.length-1].equals(is.getItemName())) {
						Collection<?> newState = (Collection<?>) event.getCollection();
						Serializable storedSnapshot = event.getCollection().getStoredSnapshot();
						Collection<?> oldState;
						if(storedSnapshot instanceof Map){//???
							oldState=((Map) storedSnapshot).values();
						}else{
							oldState = (Collection<?>) storedSnapshot;
						}
						for (Object newObject : newState) {
							if (!oldState.contains(newObject)) {
								fireEvent(is, newObject);
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


}
