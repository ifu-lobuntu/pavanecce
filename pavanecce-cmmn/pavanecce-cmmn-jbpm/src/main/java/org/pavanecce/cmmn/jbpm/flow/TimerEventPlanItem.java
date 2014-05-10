package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.process.core.timer.Timer;
import org.jbpm.workflow.core.node.TimerNode;

public class TimerEventPlanItem extends TimerNode implements PlanItem {
	private static final long serialVersionUID = 3392205893370057689L;
	private String elementId;
	private PlanItemInfo planInfo;

	public TimerEventPlanItem(PlanItemInfo info) {
		this.planInfo = info;
	}

	public String getElementId() {
		return elementId;
	}
	
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public PlanItemInfo getPlanInfo() {
		return planInfo;
	}
	public TimerEventListener getTimerEventListener(){
		return (TimerEventListener) getPlanInfo().getDefinition();
	}
	@Override
	public Timer getTimer() {
		return getTimerEventListener().getTimer();
	}

}
