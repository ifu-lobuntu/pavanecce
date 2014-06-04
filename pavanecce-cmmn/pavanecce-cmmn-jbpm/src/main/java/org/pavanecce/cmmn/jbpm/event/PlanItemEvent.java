package org.pavanecce.cmmn.jbpm.event;

import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;

public class PlanItemEvent extends CaseEvent {
	private String planItemName;
	private PlanItemTransition transition;
	private Object value;
	private String user;

	public PlanItemEvent(String planItemName, PlanItemTransition transition, Object value, String user) {
		this(planItemName, transition, value);
		this.user = user;
	}

	public PlanItemEvent(String planItemName, PlanItemTransition transition, Object value) {
		super();
		this.planItemName = planItemName;
		this.transition = transition;
		this.value = value;
	}

	public String getUser() {
		return user;
	}

	public String getPlanItemName() {
		return planItemName;
	}

	@Override
	public PlanItemTransition getTransition() {
		return transition;
	}

	@Override
	public Object getValue() {
		return value;
	}

}
