package org.pavanecce.cmmn.jbpm.flow;

public class TimerEventPlanItem extends AbstractPlanItem<TimerEventListener> {
	private static final long serialVersionUID = 3392205893370057689L;

	public TimerEventPlanItem() {
	}

	public TimerEventPlanItem(PlanItemInfo<TimerEventListener> info) {
		super(info);
	}
}
