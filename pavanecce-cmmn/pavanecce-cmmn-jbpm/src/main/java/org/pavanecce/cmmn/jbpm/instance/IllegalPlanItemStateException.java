package org.pavanecce.cmmn.jbpm.instance;

import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;

public class IllegalPlanItemStateException extends IllegalStateException {

	private static final long serialVersionUID = 1222L;
	PlanItemState state;
	PlanItemTransition transition;
	public IllegalPlanItemStateException(PlanItemState state, PlanItemTransition transition) {
		super("State " + state.name() + " does not support transition " + transition.name());
		this.state = state;
		this.transition = transition;
	}
}
 