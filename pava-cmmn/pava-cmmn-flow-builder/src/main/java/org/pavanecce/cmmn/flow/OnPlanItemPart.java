package org.pavanecce.cmmn.flow;

import java.io.Serializable;

import org.jbpm.process.core.context.variable.Variable;

public class OnPlanItemPart extends OnPart implements Serializable {
	private static final long serialVersionUID = -9167236068103073693L;
	private PlanItemTransition standardEvent;
	private PlanItem planItem;
	private String sourceRef;
	@Override
	public boolean acceptsEvent(String type, Object event) {
		return type.equals(planItem.getName()+standardEvent.name());
	}
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
