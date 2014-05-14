package org.pavanecce.cmmn.jbpm.flow;

import java.util.List;

import org.jbpm.process.core.event.EventFilter;
import org.jbpm.workflow.core.node.EventNode;

public class UserEventPlanItem extends EventNode implements PlanItem<UserEventListener> {
	private static final long serialVersionUID = 3392205893370057689L;
	private String elementId;
	private PlanItemInfo<UserEventListener> planInfo;

	public UserEventPlanItem(PlanItemInfo<UserEventListener> info) {
		this.planInfo = info;
	}

	public String getElementId() {
		return elementId;
	}
	
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public PlanItemInfo<UserEventListener> getPlanInfo() {
		return planInfo;
	}
	public UserEventListener getUserEvent(){
		return (UserEventListener) getPlanInfo().getDefinition();
	}
	@Override
	public List<EventFilter> getEventFilters() {
		return getUserEvent().getEventFilters();
	}
	public boolean acceptsEvent(String type, Object event) {
		return getUserEvent().acceptsEvent(type, event);
    }
	@Override
	public String getType() {
		return getUserEvent().getType();
	}
}
