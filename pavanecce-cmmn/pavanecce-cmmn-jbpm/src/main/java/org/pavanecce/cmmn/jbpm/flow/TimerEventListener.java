package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.process.core.timer.Timer;
import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.node.TimerNode;

public class TimerEventListener extends TimerNode implements PlanItemDefinition {

	private static final long serialVersionUID = 123L;
	private String elementId;
	private Constraint timerExpression;

	@Override
	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public Constraint getTimerExpression() {
		return timerExpression;
	}

	public void setTimerExpression(Constraint timerExpression) {
		this.timerExpression = timerExpression;
		if (timerExpression != null) {
			Timer t = new Timer();
			t.setDelay(timerExpression.getConstraint());
			t.setPeriod(timerExpression.getConstraint());
			super.setTimer(t);
		}
	}

}
