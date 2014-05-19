package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.node.StateNode;

public class MilestonePlanItem extends StateNode implements PlanItem<Milestone>, MultiInstancePlanItem {

	private static final long serialVersionUID = -1183275791860455366L;
	private PlanItemInfo<Milestone> planInfo;
	private String elementId;
	private PlanItemContainer planItemContainer;
	private String description;
	private PlanItemInstanceFactoryNode factoryNode;
	public MilestonePlanItem() {
	}
	public MilestonePlanItem(PlanItemInfo<Milestone> planInfo, PlanItemInstanceFactoryNode planItemInstanceFactoryNode) {
		super();
		this.planInfo = planInfo;
		this.factoryNode = planItemInstanceFactoryNode;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String s) {
		this.description = s;
	}

	@Override
	public PlanItemInstanceFactoryNode getFactoryNode() {
		return factoryNode;
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
