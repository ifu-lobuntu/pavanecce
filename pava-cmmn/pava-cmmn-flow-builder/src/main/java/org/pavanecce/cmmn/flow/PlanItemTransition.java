package org.pavanecce.cmmn.flow;

import java.util.HashMap;
import java.util.Map;

public enum PlanItemTransition {

	CLOSE, COMPLETE, CREATE, DISABLE, ENABLE, EXIT, FAULT, MANUAL_START, OCCUR, PARENT_RESUME, PARENT_SUSPEND, REACTIVATE, REENABLE, RESUME, START, SUSPEND, TERMINATE;
	private static Map<String, PlanItemTransition> BY_NAME = new HashMap<String, PlanItemTransition>();


	public static PlanItemTransition resolveByName(String name) {
		if (BY_NAME.isEmpty()) {
			PlanItemTransition[] values = values();
			for (PlanItemTransition planItemTransition : values) {
				BY_NAME.put(
						planItemTransition.name().toLowerCase()
								.replace("_", ""), planItemTransition);
			}
		}
		return BY_NAME.get(name.toLowerCase());
	}

}
