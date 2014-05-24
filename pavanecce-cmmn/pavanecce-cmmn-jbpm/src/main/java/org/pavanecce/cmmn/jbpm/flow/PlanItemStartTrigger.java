package org.pavanecce.cmmn.jbpm.flow;

public class PlanItemStartTrigger extends PlanItemOnPart {
	private static final long serialVersionUID = 1602328317980746322L;

	@Override
	public OnPart copy() {
		PlanItemStartTrigger result = new PlanItemStartTrigger();
		result.setId(getId());
		result.setName(getName());
		result.setStandardEvent(getStandardEvent());
		result.setSourcePlanItem(getSourcePlanItem());
		result.setSourceRef(getSourceRef());
		return result;
	}
}
