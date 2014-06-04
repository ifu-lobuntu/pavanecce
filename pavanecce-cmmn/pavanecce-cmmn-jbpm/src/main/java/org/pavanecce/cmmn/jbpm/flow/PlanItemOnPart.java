package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;

import org.pavanecce.cmmn.jbpm.event.CaseEvent;
import org.pavanecce.cmmn.jbpm.event.PlanItemEvent;

public class PlanItemOnPart extends OnPart implements Serializable {
	private static final long serialVersionUID = -9167236068103073693L;
	private PlanItemTransition standardEvent;
	private PlanItem<?> planSourceItem;
	private String sourceRef;

	public PlanItemTransition getStandardEvent() {
		return standardEvent;
	}

	public void setStandardEvent(PlanItemTransition transition) {
		this.standardEvent = transition;
	}

	public PlanItem<?> getSourcePlanItem() {
		return planSourceItem;
	}

	public void setSourcePlanItem(PlanItem<?> planItem) {
		this.planSourceItem = planItem;
	}

	public void setSourceRef(String value) {
		this.sourceRef = value;
	}

	public String getSourceRef() {
		return sourceRef;
	}

	@Override
	public String getType() {
		return getType(this.planSourceItem.getName(), standardEvent);
	}

	@Override
	public CaseEvent createEvent(Object peek) {
		return new PlanItemEvent(planSourceItem.getName(), getStandardEvent(), peek);
	}

}
