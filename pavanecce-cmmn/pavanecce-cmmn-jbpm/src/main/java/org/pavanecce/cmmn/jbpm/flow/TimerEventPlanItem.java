package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.node.StateNode;

public class TimerEventPlanItem extends StateNode implements PlanItem<TimerEventListener> {
	private static final long serialVersionUID = 3392205893370057689L;
	private String elementId;
	private PlanItemInfo<TimerEventListener> planInfo;
	private PlanItemContainer planItemContainer;
	private String description;

	public TimerEventPlanItem() {
	}

	public TimerEventPlanItem(PlanItemInfo<TimerEventListener> info) {
		this.planInfo = info;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String s) {
		this.description = s;
	}

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public PlanItemInfo<TimerEventListener> getPlanInfo() {
		return planInfo;
	}

	public TimerEventListener getTimerEventListener() {
		return (TimerEventListener) getPlanInfo().getDefinition();
	}

	public PlanItemContainer getPlanItemContainer() {
		return planItemContainer;
	}

	public void setPlanItemContainer(PlanItemContainer planItemContainer) {
		this.planItemContainer = planItemContainer;
	}

}
