package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UserEvent extends AbstractPlanItemDefinition {

	private static final long serialVersionUID = 1144314141L;
	private String eventName;
	private Map<String, Role> authorizedRoles = new HashMap<String, Role>();

	public void putAuthorizedRole(String id, Role role) {
		authorizedRoles.put(id, role);
	}

	public Collection<Role> getAuthorizedRoles() {
		return authorizedRoles.values();
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
}
