package org.pavanecce.cmmn.jbpm.flow;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementLifecycle;
import org.pavanecce.common.util.NameConverter;

public enum PlanItemTransition {
	CLOSE, COMPLETE, CREATE, DISABLE, ENABLE, EXIT, PARENT_TERMINATE, FAULT, MANUAL_START, OCCUR, PARENT_RESUME, PARENT_SUSPEND, REACTIVATE, REENABLE, RESUME, START, SUSPEND, TERMINATE;
	private static Map<String, PlanItemTransition> BY_NAME = new HashMap<String, PlanItemTransition>();

	public static PlanItemTransition resolveByName(String name) {
		if (BY_NAME.isEmpty()) {
			PlanItemTransition[] values = values();
			for (PlanItemTransition planItemTransition : values) {
				BY_NAME.put(planItemTransition.name().toLowerCase().replace("_", ""), planItemTransition);
			}
		}
		return BY_NAME.get(name.toLowerCase());
	}

	public void invokeOn(PlanElementLifecycle target) {
		try {
			target.getClass().getMethod(NameConverter.underscoredToCamelCase(name().toLowerCase())).invoke(target);
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
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

}
