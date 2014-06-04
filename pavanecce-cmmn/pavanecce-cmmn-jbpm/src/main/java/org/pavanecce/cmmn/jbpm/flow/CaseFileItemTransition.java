package org.pavanecce.cmmn.jbpm.flow;

import java.util.HashMap;
import java.util.Map;

public enum CaseFileItemTransition {
	CREATE, DELETE, ADD_CHILD, REMOVE_CHILD, REPLACE, UPDATE, ADD_REFERENCE, REMOVE_REFERENCE;

	private static Map<String, CaseFileItemTransition> BY_NAME = new HashMap<String, CaseFileItemTransition>();

	public boolean requiresParentSubscription() {
		return this == CREATE || this == DELETE;
	}

	public static CaseFileItemTransition resolveByName(String name) {
		if (BY_NAME.isEmpty()) {
			CaseFileItemTransition[] values = values();
			for (CaseFileItemTransition t : values) {
				BY_NAME.put(t.name().toLowerCase().replace("_", ""), t);
			}
		}
		return BY_NAME.get(name.toLowerCase());
	}
}
