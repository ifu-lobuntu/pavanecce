package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.node.StateNode;

public class MilestonePlanItem extends StateNode implements PlanItem<Milestone> {

	private static final long serialVersionUID = -1183275791860455366L;
	private PlanItemInfo<Milestone> planInfo;
	private String elementId;
	private PlanItemContainer planItemContainer;
	private String description;

	public MilestonePlanItem(PlanItemInfo<Milestone> planInfo) {
		super();
		this.planInfo = planInfo;
	}
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String s){
		this.description=s;
	}

	@Override
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public String getElementId() {
		return elementId;
	}

	@Override
	public PlanItemInfo<Milestone> getPlanInfo() {
		return planInfo;
	}

	public PlanItemContainer getPlanItemContainer() {
		return planItemContainer;
	}

	public void setPlanItemContainer(PlanItemContainer planItemContainer) {
		this.planItemContainer = planItemContainer;
	}

}
