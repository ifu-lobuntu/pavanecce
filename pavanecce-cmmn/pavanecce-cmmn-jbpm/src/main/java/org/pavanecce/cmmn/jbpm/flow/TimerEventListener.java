package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.impl.ExtendedNodeImpl;

public class TimerEventListener extends ExtendedNodeImpl implements PlanItemDefinition {

	private static final long serialVersionUID = 123L;
	private String elementId;
	private Constraint timerExpression;
	private PlanItemControl defaultControl;

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
	}

	public PlanItemControl getDefaultControl() {
		return defaultControl;
	}

	public void setDefaultControl(PlanItemControl defaultControl) {
		this.defaultControl = defaultControl;
	}

}
