package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.Constraint;

public class TimerEventListener extends AbstractPlanItemDefinition {

	private static final long serialVersionUID = 123L;
	private Constraint timerExpression;

	public Constraint getTimerExpression() {
		return timerExpression;
	}

	public void setTimerExpression(Constraint timerExpression) {
		this.timerExpression = timerExpression;
	}

}
