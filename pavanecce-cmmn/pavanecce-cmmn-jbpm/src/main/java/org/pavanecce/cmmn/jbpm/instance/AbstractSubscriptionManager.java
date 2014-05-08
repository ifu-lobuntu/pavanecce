package org.pavanecce.cmmn.jbpm.instance;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;
import org.pavanecce.cmmn.jbpm.flow.builder.CollectionDataType;
import org.pavanecce.common.ObjectPersistence;

public abstract class AbstractSubscriptionManager<T extends CaseSubscriptionInfo<X>, X extends PersistedCaseFileItemSubscriptionInfo> {

	private static ThreadLocal<Set<EntityManager>> entityManagersToFlush = new ThreadLocal<Set<EntityManager>>();
	private boolean cascadeSubscription = false;
	private static ThreadLocal<Map<CaseInstance, Set<CaseParameter>>> explicitlyScopedSubscriptions = new ThreadLocal<Map<CaseInstance, Set<CaseParameter>>>();

	public AbstractSubscriptionManager() {
		super();
	}

	public AbstractSubscriptionManager(boolean cascade) {
		super();
		this.cascadeSubscription = cascade;
	}

	public final void subscribe(CaseInstance process, CaseFileItem item, Object target, ObjectPersistence p) {
		subscribeToUnknownNumberOfObjects(process, item, target, p);
	}

	public final void subscribeToParent(CaseInstance process, CaseFileItem caseFileItem, Object parent, ObjectPersistence p) {
		if (parent instanceof Collection) {
			Collection<?> coll = (Collection<?>) parent;
			for (Object object : coll) {
				T info = findOrCreateCaseSubscriptionInfo(object, p);
				subscribeChildItem(process, info, process.getCase(), caseFileItem);
				p.update(info);
			}
		} else {
			T info = findOrCreateCaseSubscriptionInfo(parent, p);
			subscribeChildItem(process, info, process.getCase(), caseFileItem);
			p.update(info);
		}
	}

	public void unsubscribeFromParent(CaseInstance process, CaseFileItem caseFileItem, Object parent, ObjectPersistence p) {
		// TODO do this correctly
		unsubscribe(process, caseFileItem, parent, p);
	}
	public static void addScopedSubscriptions(CaseInstance theCase) {
		Set<CaseParameter> params = new HashSet<CaseParameter>();
		params.addAll(theCase.getCase().getInputParameters());
		for (NodeInstance ni : theCase.getNodeInstances()) {
			if (ni.getNode() instanceof PlanItem) {
				PlanItem pi = (PlanItem) ni.getNode();
				PlanItemDefinition def = pi.getPlanInfo().getDefinition();
				if (def instanceof TaskDefinition) {
					params.addAll(((TaskDefinition) def).getOutputs());
				}
			}
		}
		Map<CaseInstance, Set<CaseParameter>> map = explicitlyScopedSubscriptions.get();
		if (map == null) {
			explicitlyScopedSubscriptions.set(map = new HashMap<CaseInstance, Set<CaseParameter>>());
		}
		map.put(theCase, params);
	}

	public static Map<CaseInstance, Set<CaseParameter>> getScopedSubscriptions() {
		return explicitlyScopedSubscriptions.get();
	}

	public static Collection<OnPartInstanceSubscription> getExplicitlyScopedSubscriptionsFor(Object source, CaseFileItemTransition... transitions) {
		if (getScopedSubscriptions() != null) {
			Collection<OnPartInstanceSubscription> result = new HashSet<OnPartInstanceSubscription>();
			for (Entry<CaseInstance, Set<CaseParameter>> entry : getScopedSubscriptions().entrySet()) {
				for (CaseParameter caseParameter : entry.getValue()) {
					boolean isListed = false;
					if (caseParameter.getVariable().isCollection()) {
						CollectionDataType dt = (CollectionDataType) caseParameter.getVariable().getType();
						String elementClassName = dt.getElementClassName();
						if (isInstance(source, elementClassName)) {
							isListed = true;
						}
					} else {
						String stringType = caseParameter.getVariable().getType().getStringType();
						if (isInstance(source, stringType)) {
							isListed = true;
						}
					}
					if (isListed) {
						for (OnPartInstance opi : entry.getKey().findCaseFileItemOnPartInstancesFor(caseParameter.getVariable())) {
							for (CaseFileItemTransition t : transitions) {
								if (t == opi.getOnPart().getStandardEvent()) {
									result.add(new OnPartInstanceSubscription(opi, caseParameter));
									break;
								}
							}
						}
					}
				}
			}
			return result;
		}
		return Collections.emptySet();
	}

	private static boolean isInstance(Object source, String stringType) {
		try {
			return Class.forName(stringType).isInstance(source);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	protected void subscribeToUnknownNumberOfObjects(CaseInstance process, CaseFileItem caseFileItem, Object val, ObjectPersistence em) {
		if (val instanceof Collection) {
			Collection<?> coll = (Collection<?>) val;
			for (Object child : coll) {
				subscribeToSingleObject(process, caseFileItem, child, em);
			}
		} else if (val != null) {
			subscribeToSingleObject(process, caseFileItem, val, em);
		}
	}

	public static void flushEntityManagers() {
		for (EntityManager em : getEntitymanagersToFlush()) {
			em.flush();
		}
		entityManagersToFlush.get().clear();
	}

	protected static void fireEvent(CaseFileItemSubscriptionInfo is, Object parentObject, Object value) {
		CaseFileItemEvent event = new CaseFileItemEvent(is.getItemName(), is.getTransition(), parentObject, value);
		if (is instanceof OnPartInstanceSubscription) {
			OnPartInstanceSubscription onPartInstanceSubscription = (OnPartInstanceSubscription) is;
			if (onPartInstanceSubscription.meetsBindingRefinementCriteria(value)) {
				OnPartInstance source = onPartInstanceSubscription.getSource();
				CaseInstance ci=(CaseInstance) source.getProcessInstance();
				fireEvent(ci.getCase().getCaseKey(), ci.getId(), event);
			}
		} else if (is instanceof PersistedCaseFileItemSubscriptionInfo) {
			PersistedCaseFileItemSubscriptionInfo pcfis = (PersistedCaseFileItemSubscriptionInfo) is;
			fireEvent(pcfis.getCaseKey(), pcfis.getProcessId(), event);
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
			addEntityManagerToFlush(em);
		} catch (Exception e) {
			e.printStackTrace();
		}
		pcm.endCommandScopedEntityManager();
	}

	private static void addEntityManagerToFlush(EntityManager em) {
		// DIsabled - attempted performance optimization but it broke WorkItem
		// persistence
		// getEntitymanagersToFlush().add(em);
	}

	protected static Set<EntityManager> getEntitymanagersToFlush() {
		Set<EntityManager> collection = entityManagersToFlush.get();
		if (collection == null) {
			entityManagersToFlush.set(collection = new HashSet<EntityManager>());
		}
		return collection;
	}

	private void subscribeToSingleObject(CaseInstance process, CaseFileItem caseFileItem, Object currentInstance, ObjectPersistence em) {
		T info = findOrCreateCaseSubscriptionInfo(currentInstance, em);
		Case theCase = (Case) process.getProcess();
		storeVariable(process, caseFileItem, currentInstance);
		Collection<CaseFileItemOnPart> onCaseFileItemParts = theCase.findCaseFileItemOnPartsFor(caseFileItem);
		for (CaseFileItemOnPart part : onCaseFileItemParts) {
			switch (part.getStandardEvent()) {
			case ADD_CHILD:
			case ADD_REFERENCE:
			case REMOVE_CHILD:
			case REMOVE_REFERENCE:
			case REPLACE:
			case UPDATE:
				buildCaseFileItemSubscriptionInfo(process, caseFileItem, info, part);
				break;
			default:
				break;
			}
		}
		if (cascadeSubscription) {
			addSubscriptionsForCreationOrDeletionOfChildren(process, caseFileItem, info, theCase);
			em.update(info);
			cascadeSubscribe(process, currentInstance, caseFileItem.getChildren(), em);
			cascadeSubscribe(process, currentInstance, caseFileItem.getTargets(), em);
		} else {
			em.update(info);
		}
	}

	protected T findOrCreateCaseSubscriptionInfo(Object currentInstance, ObjectPersistence em) {
		CaseSubscriptionKey key = createCaseSubscriptionKey(currentInstance);
		T info = em.find(caseSubscriptionInfoClass(), key);
		if (info == null) {
			info = createCaseSubscriptionInfo(currentInstance);
			em.persist(info);
		}
		return info;
	}

	protected void addSubscriptionsForCreationOrDeletionOfChildren(CaseInstance process, CaseFileItem caseFileItem, T info, Case theCase) {
		/*
		 * CREATE and DELETE events are only relevant if the object created is added as a child to a parent object that
		 * is already involved in the case We therefore need to listen for that event too
		 */
		for (CaseFileItem childItem : caseFileItem.getChildren()) {
			subscribeChildItem(process, info, theCase, childItem);
		}
	}

	protected void subscribeChildItem(CaseInstance process, T info, Case theCase, CaseFileItem childItem) {
		Collection<CaseFileItemOnPart> on = theCase.findCaseFileItemOnPartsFor(childItem);
		for (CaseFileItemOnPart part : on) {
			if (part.getStandardEvent() == CaseFileItemTransition.CREATE || part.getStandardEvent() == CaseFileItemTransition.DELETE) {
				buildCaseFileItemSubscriptionInfo(process, childItem, info, part);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Class<T> caseSubscriptionInfoClass() {
		ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
		Type result = genericSuperclass.getActualTypeArguments()[0];
		return (Class<T>) result;
	}

	protected PersistedCaseFileItemSubscriptionInfo buildCaseFileItemSubscriptionInfo(CaseInstance process, CaseFileItem caseFileItem, T info, CaseFileItemOnPart part) {
		X result = createCaseFileItemSubscriptionInfo();
		result.setCaseSubscription(info);
		info.addCaseFileItemSubscription(result);
		result.setItemName(caseFileItem.getName());
		result.setTransition(part.getStandardEvent());
		result.setProcessId(process.getId());
		result.setCaseKey(((Case) process.getProcess()).getCaseKey());
		if (part.getRelatedCaseFileItem() != null) {
			result.setRelatedItemName(part.getRelatedCaseFileItem().getName());
		}
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
			// TODO think about the possibility of the objects being read from
			// different entityManagers
			variable.add(target);
		} else {
			process.setVariable(caseFileItem.getName(), target);
		}
	}

	private void cascadeSubscribe(CaseInstance process, Object target, List<CaseFileItem> related, ObjectPersistence em) {
		for (CaseFileItem caseFileItem : related) {
			String propName = caseFileItem.getName();
			try {
				Method getter = target.getClass().getMethod("get" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1));
				subscribeToUnknownNumberOfObjects(process, caseFileItem, getter.invoke(target), em);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected void unsubscribeFromUnknownNumberOfObjects(CaseInstance process, ObjectPersistence em, CaseFileItem caseFileItem, Object val) {
		if (val instanceof Collection) {
			Collection<?> coll = (Collection<?>) val;
			for (Object object : coll) {
				unsubscribe(process, caseFileItem, object, em);
			}
		} else if (val != null) {
			unsubscribe(process, caseFileItem, val, em);

		}
	}

	private void cascadeUnsubscribe(CaseInstance process, Object target, List<CaseFileItem> children, ObjectPersistence em) {
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

	public void unsubscribe(CaseInstance process, CaseFileItem caseFileItem, Object target, ObjectPersistence em) {
		CaseSubscriptionKey key = createCaseSubscriptionKey(target);
		T info = em.find(caseSubscriptionInfoClass(), key);
		if (info != null) {
			for (PersistedCaseFileItemSubscriptionInfo s : new ArrayList<PersistedCaseFileItemSubscriptionInfo>(info.getCaseFileItemSubscriptions())) {
				if (s.getProcessId() == process.getId()) {
					em.remove(s);
					info.getCaseFileItemSubscriptions().remove(s);
				}
			}
		}
		cascadeUnsubscribe(process, target, caseFileItem.getChildren(), em);
		cascadeUnsubscribe(process, target, caseFileItem.getTargets(), em);
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

	public static void removeScopedSubscriptions() {
		explicitlyScopedSubscriptions.set(null);
	}
}
