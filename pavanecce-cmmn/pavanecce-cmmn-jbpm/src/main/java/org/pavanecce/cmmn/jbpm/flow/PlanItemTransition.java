package org.pavanecce.cmmn.jbpm.flow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.pavanecce.cmmn.jbpm.instance.CaseElementLifecycle;
import org.pavanecce.cmmn.jbpm.instance.ControllablePlanItemInstanceLifecycle;
import org.pavanecce.common.util.NameConverter;

public enum PlanItemTransition {
	CLOSE, COMPLETE, CREATE, DISABLE, ENABLE, EXIT, PARENT_TERMINATE, FAULT, MANUAL_START, OCCUR, PARENT_RESUME, PARENT_SUSPEND, REACTIVATE, REENABLE, RESUME, START, SUSPEND, TERMINATE;
	private static Map<String, PlanItemTransition> BY_NAME = new HashMap<String, PlanItemTransition>();
	Method method;
	public Method getMethod() {
		if(method==null){
			String name = NameConverter.underscoredToCamelCase(name().toLowerCase());
			try {
				method=ControllablePlanItemInstanceLifecycle.class.getMethod(name);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		return method;
	}
	public static PlanItemTransition resolveByName(String name) {
		if (BY_NAME.isEmpty()) {
			PlanItemTransition[] values = values();
			for (PlanItemTransition planItemTransition : values) {
				BY_NAME.put(planItemTransition.name().toLowerCase().replace("_", ""), planItemTransition);
			}
		}
		return BY_NAME.get(name.toLowerCase());
	}

	public void invokeOn(CaseElementLifecycle target) {
		try {
			getMethod().invoke(target);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof RuntimeException) {
				throw (RuntimeException) e.getTargetException();
			} else {
				throw new RuntimeException(e.getTargetException());
			}
		}
	}

}
