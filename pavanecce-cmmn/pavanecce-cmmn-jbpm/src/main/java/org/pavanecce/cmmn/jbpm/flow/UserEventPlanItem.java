package org.pavanecce.cmmn.jbpm.flow;

public class UserEventPlanItem extends AbstractPlanItem<UserEventListener> {
	private static final long serialVersionUID = 3392205893370057689L;

	public UserEventPlanItem() {

	}

	public UserEventPlanItem(PlanItemInfo<UserEventListener> info) {
		super(info);
	}

	public boolean acceptsEvent(String type, Object event) {
		return getDefinition().getEventName().equals(type);
	}

}
