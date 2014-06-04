package org.pavanecce.cmmn.jbpm.lifecycle;

import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;

public class IllegalPlanItemStateException extends IllegalStateException {

	private static final long serialVersionUID = 1222L;
	PlanElementState state;
	PlanItemTransition transition;

	public IllegalPlanItemStateException(PlanElementState state, PlanItemTransition transition) {
		super("State " + state.name() + " does not support transition " + transition.name());
		this.state = state;
		this.transition = transition;
	}
}
