package org.pavanecce.cmmn.instance;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.persistence.PersistenceContext;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.jpa.JpaPersistenceContextManager;
import org.kie.api.runtime.EnvironmentName;
import org.pavanecce.cmmn.flow.Case;
import org.pavanecce.cmmn.flow.CaseFileItem;
import org.pavanecce.cmmn.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.flow.CaseFileItemTransition;

public abstract class AbstractSubscriptionManager<T extends CaseSubscriptionInfo<X>, X extends CaseFileItemSubscriptionInfo> {

	public AbstractSubscriptionManager() {
		super();
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

	protected void fireEvent(CaseFileItemSubscriptionInfo is, Object parentObject, Object value) {
		InternalKnowledgeRuntime eventManager = CaseInstanceFactory.getEventManager(is.getCaseKey());
		String eventType = CaseFileItemOnPart.getType(is.getItemName(), is.getTransition());
		PersistenceContextManager pcm = (PersistenceContextManager) eventManager.getEnvironment().get(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER);
		pcm.beginCommandScopedEntityManager();
		PersistenceContext pc = pcm.getCommandScopedPersistenceContext();
		pc.joinTransaction();
		eventManager.signalEvent(eventType, new CaseFileItemEvent(is.getItemName(), is.getTransition(), parentObject, value), is.getProcessId());
		try {
			// mmm.... desperate measures
			Method m = JpaPersistenceContextManager.class.getDeclaredMethod("getInternalCommandScopedEntityManager");
			m.setAccessible(true);
			EntityManager em = (EntityManager) m.invoke(pcm);
			em.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		pcm.endCommandScopedEntityManager();
	}

	private void subscribeToSingleObject(CaseInstance process, CaseFileItem caseFileItem, Object currentInstance, ObjectPersistence em) {
		CaseSubscriptionKey key = createCaseSubscriptionKey(currentInstance);
		T info = (T) em.find(caseSubscriptionInfoClass(), key);
		if (info == null) {
			info = createCaseSubscriptionInfo(currentInstance);
			em.persist(info);
		}
		Case theCase = (Case) process.getProcess();
		storeVariable(process, caseFileItem, currentInstance);
		Set<CaseFileItemOnPart> onCaseFileItemParts = theCase.findCaseFileItemOnPartsFor(caseFileItem);
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
		addSubscriptionsForCreationOrDeletionOfChildren(process, caseFileItem, info, theCase);
		em.update(info);
		cascadeSubscribe(process, currentInstance, caseFileItem.getChildren(), em);
		cascadeSubscribe(process, currentInstance, caseFileItem.getTargets(), em);
	}

	protected void addSubscriptionsForCreationOrDeletionOfChildren(CaseInstance process, CaseFileItem caseFileItem, T info, Case theCase) {
		/*
		 * CREATE events are only relevant if the object created is added as a child to a parent object that is already involved in the case
		 * We therefore need to listen for that event too
		 */
		for (CaseFileItem childItem : caseFileItem.getChildren()) {
			Set<CaseFileItemOnPart> on = theCase.findCaseFileItemOnPartsFor(childItem);
			for (CaseFileItemOnPart part : on) {
				if (part.getStandardEvent() == CaseFileItemTransition.CREATE || part.getStandardEvent() == CaseFileItemTransition.DELETE) {
					buildCaseFileItemSubscriptionInfo(process, childItem, info, part);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Class<T> caseSubscriptionInfoClass() {
		ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
		Type result = genericSuperclass.getActualTypeArguments()[0];
		return (Class<T>) result;
	}

	protected CaseFileItemSubscriptionInfo buildCaseFileItemSubscriptionInfo(CaseInstance process, CaseFileItem caseFileItem, T info, CaseFileItemOnPart part) {
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

	private void unsubscribe(CaseInstance process, CaseFileItem caseFileItem, Object target, ObjectPersistence em) {
		CaseSubscriptionKey key = createCaseSubscriptionKey(target);
		T info = em.find(caseSubscriptionInfoClass(), key);
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

}