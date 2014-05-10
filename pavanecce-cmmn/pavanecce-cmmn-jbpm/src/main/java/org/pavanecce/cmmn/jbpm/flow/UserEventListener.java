package org.pavanecce.cmmn.jbpm.flow;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.core.event.EventFilter;
import org.jbpm.process.core.event.EventTypeFilter;
import org.jbpm.workflow.core.node.EventNode;

public class UserEventListener extends EventNode implements PlanItemDefinition {

	private static final long serialVersionUID = 1144314141L;
	private String elementId;
	private String eventName;
	private Map<String,Role> authorizedRoles = new HashMap<String,Role>();
	public String getElementId() {
		return elementId;
	}
	public void putAuthorizedRole(String id,Role role){
		authorizedRoles.put(id, role);
	}
	public Collection<Role> getAuthorizedRoles() {
		return authorizedRoles.values();
	}
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}
	@Override
	public String getType() {
		return getEventName();
	}

	public String getEventName() {
		return eventName;
	}


	public void setEventName(String eventName) {
		EventTypeFilter eventTypeFilter = new EventTypeFilter();
		eventTypeFilter.setType(eventName);
		super.setEventFilters(Arrays.<EventFilter>asList(eventTypeFilter));
		this.eventName = eventName;
	}
}
