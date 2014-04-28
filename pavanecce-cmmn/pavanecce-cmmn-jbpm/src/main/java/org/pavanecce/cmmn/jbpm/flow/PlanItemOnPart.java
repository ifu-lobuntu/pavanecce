package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;

public class PlanItemOnPart extends OnPart implements Serializable {
	private static final long serialVersionUID = -9167236068103073693L;
	private PlanItemTransition standardEvent;
	private PlanItem planItem;
	private String sourceRef;

	public PlanItemTransition getStandardEvent() {
		return standardEvent;
	}
	public void setStandardEvent(PlanItemTransition transition) {
		this.standardEvent = transition;
	}
	public PlanItem getPlanItem() {
		return planItem;
	}
	public void setPlanItem(PlanItem planItem) {
		this.planItem = planItem;
	}
	public void setSourceRef(String value) {
		this.sourceRef=value;
	}
	public String getSourceRef() {
		return sourceRef;
	}
	@Override
	public String getType() {
		return getType(this.planItem.getName(),standardEvent);
	}

}
