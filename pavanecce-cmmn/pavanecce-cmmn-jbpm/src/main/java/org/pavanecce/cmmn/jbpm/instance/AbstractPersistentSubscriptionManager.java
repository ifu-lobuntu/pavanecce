package org.pavanecce.cmmn.jbpm.instance;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.persistence.PersistenceContext;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.jpa.JpaPersistenceContextManager;
import org.kie.api.runtime.EnvironmentName;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.common.ObjectPersistence;

public abstract class AbstractPersistentSubscriptionManager<T extends CaseSubscriptionInfo<X>, X extends PersistedCaseFileItemSubscriptionInfo> implements SubscriptionManager {

	private boolean cascadeSubscription = false;
	private static Map<Object,Map<CaseSubscriptionKey, CaseSubscriptionInfo<?>>> cachedSubscriptions = new HashMap<Object, Map<CaseSubscriptionKey, CaseSubscriptionInfo<?>>>();

	// private ThreadLocal<Map<String, X>> cachedItemSubscriptions = new ThreadLocal<Map<String, X>>();

	public AbstractPersistentSubscriptionManager() {
		super();
	}

	public AbstractPersistentSubscriptionManager(boolean cascade) {
		super();
		this.cascadeSubscription = cascade;
	}

	@Override
	public void updateSubscriptions(CaseInstance caseInstance, Collection<Object> targets, Map<CaseFileItem, Collection<Object>> parentSubscriptions, ObjectPersistence p) {
		cacheSubscriptions(caseInstance, p);
		Set<OnPartInstanceSubscription> findOnPartInstanceSubscriptions = caseInstance.findOnPartInstanceSubscriptions();
		subscribeToUnknownNumberOfObjects(caseInstance, findOnPartInstanceSubscriptions, targets, p);
		subscribeToCreateAndDeleteOfChildren(caseInstance, parentSubscriptions, p);
	}

	private void cacheSubscriptions(CaseInstance caseInstance, ObjectPersistence p) {
		Map<CaseSubscriptionKey, CaseSubscriptionInfo<?>> map = getCachedSubscriptions(p);
		for (T t : getAllSubscriptionsAgainst(caseInstance, p)) {
			invalidateCaseFileItemSubscriptions(caseInstance, t);
			map.put(t.getId(), t);
		}
	}

	protected Map<CaseSubscriptionKey, CaseSubscriptionInfo<?>> getCachedSubscriptions(Object p) {
		Map<CaseSubscriptionKey, CaseSubscriptionInfo<?>> map = cachedSubscriptions.get(p);
		if (map == null) {
			cachedSubscriptions.put(p, map=new HashMap<CaseSubscriptionKey,CaseSubscriptionInfo<?>>());
		}
		return map;
	}

	private void invalidateCaseFileItemSubscriptions(CaseInstance caseInstance, T t) {
		for (X x : t.getCaseFileItemSubscriptions()) {
			if (x.getProcessInstanceId() == caseInstance.getId()) {
				x.deactivate();
			}
		}

	}

	private void subscribeToUnknownNumberOfObjects(CaseInstance process, Collection<OnPartInstanceSubscription> subs, Object target, ObjectPersistence em) {
		if (target instanceof Collection) {
			Collection<?> targets = (Collection<?>) target;
			for (Object currentTarget : targets) {
				subscribeToSingleObject(process, subs, currentTarget, em);
			}
		} else if (target != null) {
			subscribeToSingleObject(process, subs, target, em);
		}
	}



	protected static void fireEvent(CaseFileItemSubscriptionInfo is, Object parentObject, Object value) {
		CaseFileItemEvent event = new CaseFileItemEvent(is.getItemName(), is.getTransition(), parentObject, value);
		if (is instanceof OnPartInstanceSubscription) {
			OnPartInstanceSubscription opis = (OnPartInstanceSubscription) is;
			InternalKnowledgeRuntime eventManager = CaseInstanceFactory.getEventManager(opis.getCaseKey());
			CaseInstance ci = (CaseInstance) eventManager.getProcessInstance(opis.getProcessInstanceId());
			if (opis.meetsBindingRefinementCriteria(value, ci)) {
				fireEvent(ci.getCase().getCaseKey(), ci.getId(), event);
			}
		} else if (is instanceof PersistedCaseFileItemSubscriptionInfo) {
			PersistedCaseFileItemSubscriptionInfo pcfis = (PersistedCaseFileItemSubscriptionInfo) is;
			fireEvent(pcfis.getCaseKey(), pcfis.getProcessInstanceId(), event);
		}
	}

	protected static void fireEvent(String caseKey, long processId, CaseFileItemEvent caseFileItemEvent) {
		InternalKnowledgeRuntime eventManager = CaseInstanceFactory.getEventManager(caseKey);
		String eventType = OnPart.getType(caseFileItemEvent.getCaseFileItemName(), caseFileItemEvent.getTransition());
		PersistenceContextManager pcm = (PersistenceContextManager) eventManager.getEnvironment().get(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER);
		pcm.beginCommandScopedEntityManager();
		PersistenceContext pc = pcm.getCommandScopedPersistenceContext();
		pc.joinTransaction();
		try {
			// mmm.... desperate measures
			Method m = JpaPersistenceContextManager.class.getDeclaredMethod("getInternalCommandScopedEntityManager");
			m.setAccessible(true);
			EntityManager em = (EntityManager) m.invoke(pcm);
			em.joinTransaction();
			eventManager.signalEvent(eventType, caseFileItemEvent, processId);
			em.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		pcm.endCommandScopedEntityManager();
	}



	private void subscribeToSingleObject(CaseInstance caseInstance, Collection<OnPartInstanceSubscription> subs, Object target, ObjectPersistence em) {
		T info = findOrCreateCaseSubscriptionInfo(caseInstance, target, em);
		for (OnPartInstanceSubscription sub : subs) {
			if (sub.isListeningTo(target, sub.getTransition()) && !sub.getTransition().requiresParentSubscription()) {
				storeVariable(caseInstance, sub.getVariable(), target);
				buildCaseFileItemSubscriptionInfo(caseInstance, info, sub);
			}
			if (cascadeSubscription) {
				/*
				 * CREATE and DELETE events are only relevant if the object created is added as a child to a parent
				 * object that is already involved in the case. We therefore need to listen for that event too
				 */
				for (CaseFileItem childCaseFileItem : sub.getVariable().getChildren()) {
					registerChildCreateAndDeleteSubscriptions(caseInstance, subs, info, childCaseFileItem);
				}
				if (sub.isListeningTo(target, sub.getTransition())) {
					cascadeSubscribe(caseInstance, target, sub.getVariable().getChildren(), em, subs);
					cascadeSubscribe(caseInstance, target, sub.getVariable().getTargets(), em, subs);
				}
			}
		}
	}

	protected void registerChildCreateAndDeleteSubscriptions(CaseInstance caseInstance, Collection<OnPartInstanceSubscription> subs, T parentInfo, CaseFileItem childCaseFileItem) {
		for (OnPartInstanceSubscription childSubscription : subs) {
			if (childSubscription.getVariable().getElementId().equals(childCaseFileItem.getElementId()) && childSubscription.getTransition().requiresParentSubscription()) {
				buildCaseFileItemSubscriptionInfo(caseInstance, parentInfo, childSubscription);
			}
		}
	}

	private void subscribeToCreateAndDeleteOfChildren(CaseInstance caseInstance, Map<CaseFileItem, Collection<Object>> parents, ObjectPersistence p) {
		Set<OnPartInstanceSubscription> subs = caseInstance.findOnPartInstanceSubscriptions();
		for (Entry<CaseFileItem, Collection<Object>> entry : parents.entrySet()) {
			CaseFileItem caseFileItem = entry.getKey();
			for (Object currentParent : entry.getValue()) {
				T parentSubscription = findOrCreateCaseSubscriptionInfo(caseInstance, currentParent, p);
				registerChildCreateAndDeleteSubscriptions(caseInstance, subs, parentSubscription, caseFileItem);
			}
		}
	}

	public T getCaseSubscriptionInfoFor(Object currentInstance, ObjectPersistence em) {
		CaseSubscriptionKey key = createCaseSubscriptionKey(currentInstance);
		return em.find(caseSubscriptionInfoClass(), key);
	}

	protected abstract Collection<T> getAllSubscriptionsAgainst(CaseInstance caseInstance, ObjectPersistence em);

	@SuppressWarnings("unchecked")
	private T findOrCreateCaseSubscriptionInfo(CaseInstance caseInstance, Object currentInstance, ObjectPersistence em) {
		CaseSubscriptionKey key = createCaseSubscriptionKey(currentInstance);
		T info = (T) getCachedSubscriptions(em.getDelegate()).get(key);
		if (info == null) {
			info = em.find(caseSubscriptionInfoClass(), key);
			if (info == null) {
				info = createCaseSubscriptionInfo(currentInstance);
				em.persist(info);
			} else {
				// found, deactivate subscriptions for this caseInstance to be activated only if matched subscription
				// found
				invalidateCaseFileItemSubscriptions(caseInstance, info);
			}
			cachedSubscriptions.get(em.getDelegate()).put(key, info);
		}
		return info;
	}

	@SuppressWarnings("unchecked")
	private Class<T> caseSubscriptionInfoClass() {
		ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
		Type result = genericSuperclass.getActualTypeArguments()[0];
		return (Class<T>) result;
	}

	private X buildCaseFileItemSubscriptionInfo(CaseInstance process, T info, OnPartInstanceSubscription sub) {
		// first try to find it and the actiate it
		for (X x : info.getCaseFileItemSubscriptions()) {
			if (x.isEquivalent(sub)) {
				x.activate();
				return x;
			}
		}
		// not foud, create new
		X result = createCaseFileItemSubscriptionInfo();
		result.setCaseSubscription(info);
		result.setItemName(sub.getItemName());
		result.setTransition(sub.getTransition());
		result.setProcessInstanceId(process.getId());
		result.setCaseKey(((Case) process.getProcess()).getCaseKey());
		result.setRelatedItemName(sub.getRelatedItemName());
		info.addCaseFileItemSubscription(result);
		return result;
	}

	protected abstract X createCaseFileItemSubscriptionInfo();

	protected abstract T createCaseSubscriptionInfo(Object currentInstance);

	protected abstract CaseSubscriptionKey createCaseSubscriptionKey(Object currentInstance);

	@SuppressWarnings("unchecked")
	private void storeVariable(CaseInstance process, CaseFileItem caseFileItem, Object target) {
		if (caseFileItem.isCollection()) {
			Collection<Object> variable = (Collection<Object>) process.getVariable(caseFileItem.getName());
			if (variable == null) {
				variable = new HashSet<Object>();
				process.setVariable(caseFileItem.getName(), variable);
			}
			variable.add(target);
		} else {
			process.setVariable(caseFileItem.getName(), target);
		}
	}

	private void cascadeSubscribe(CaseInstance process, Object target, List<CaseFileItem> related, ObjectPersistence em, Collection<OnPartInstanceSubscription> subs) {
		for (CaseFileItem caseFileItem : related) {
			String propName = caseFileItem.getName();
			try {
				Method getter = target.getClass().getMethod("get" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1));
				subscribeToUnknownNumberOfObjects(process, subs, getter.invoke(target), em);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected boolean isMatchingAddChild(CaseFileItemSubscriptionInfo is, String propertyName) {
		return is.getRelatedItemName() != null && propertyName.equals(is.getRelatedItemName()) && (is.getTransition() == CaseFileItemTransition.ADD_CHILD);
	}

	protected boolean isMatchingCreate(CaseFileItemSubscriptionInfo is, String propertyName) {
		return propertyName.equals(is.getItemName()) && (is.getTransition() == CaseFileItemTransition.CREATE);
	}

	protected boolean isMatchingRemoveChild(CaseFileItemSubscriptionInfo is, String propertyName) {
		return is.getRelatedItemName() != null && propertyName.equals(is.getRelatedItemName()) && (is.getTransition() == CaseFileItemTransition.REMOVE_CHILD);
	}

	protected boolean isMatchingDelete(CaseFileItemSubscriptionInfo is, String propertyName) {
		return propertyName.equals(is.getItemName()) && (is.getTransition() == CaseFileItemTransition.DELETE);
	}

	protected boolean isMatchingAddOrRemove(CaseFileItemSubscriptionInfo is, String propertyName) {
		return is.getRelatedItemName() != null
				&& propertyName.equals(is.getRelatedItemName())
				&& (is.getTransition() == CaseFileItemTransition.ADD_CHILD || is.getTransition() == CaseFileItemTransition.REMOVE_CHILD || is.getTransition() == CaseFileItemTransition.ADD_REFERENCE || is
						.getTransition() == CaseFileItemTransition.REMOVE_REFERENCE);
	}

	protected boolean isMatchingCreateOrDelete(CaseFileItemSubscriptionInfo is, String propertyName) {
		/*
		 * In the case of CREATE and DELETE the itemName is actually the name of the property on the parent to the child
		 */
		return propertyName.equals(is.getItemName()) && (is.getTransition() == CaseFileItemTransition.CREATE || is.getTransition() == CaseFileItemTransition.DELETE);
	}
}
