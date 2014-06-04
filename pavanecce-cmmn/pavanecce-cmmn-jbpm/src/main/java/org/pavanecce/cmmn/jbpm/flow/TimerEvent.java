package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.Constraint;

public class TimerEvent extends AbstractPlanItemDefinition {

	private static final long serialVersionUID = 123L;
	private Constraint timerExpression;
	private OnPart startTrigger;

	public Constraint getTimerExpression() {
		return timerExpression;
	}

	public void setTimerExpression(Constraint timerExpression) {
		this.timerExpression = timerExpression;
	}

	public void setStartTrigger(OnPart part) {
		this.startTrigger = part;
	}

	public OnPart getStartTrigger() {
		return startTrigger;
	}

}
