package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.node.StateNode;

public class UserEventPlanItem extends StateNode implements PlanItem<UserEventListener> {
	private static final long serialVersionUID = 3392205893370057689L;
	private String elementId;
	private PlanItemInfo<UserEventListener> planInfo;
	private PlanItemContainer planItemContainer;
	private String description;
	public UserEventPlanItem (){
		
	}
	
	public UserEventPlanItem(PlanItemInfo<UserEventListener> info) {
		this.planInfo = info;
	}
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String s){
		this.description=s;
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
	public boolean acceptsEvent(String type, Object event) {
		return getUserEvent().acceptsEvent(type, event);
    }

	public PlanItemContainer getPlanItemContainer() {
		return planItemContainer;
	}

	public void setPlanItemContainer(PlanItemContainer planItemContainer) {
		this.planItemContainer = planItemContainer;
	}
}
