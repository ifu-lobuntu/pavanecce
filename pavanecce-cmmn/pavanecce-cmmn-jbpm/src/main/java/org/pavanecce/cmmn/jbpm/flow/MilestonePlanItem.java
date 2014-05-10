package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.node.StateNode;

public class MilestonePlanItem extends StateNode implements PlanItem {

	private static final long serialVersionUID = -1183275791860455366L;
	private PlanItemInfo planInfo;
	private String elementId;
	
	public MilestonePlanItem(PlanItemInfo planInfo) {
		super();
		this.planInfo = planInfo;
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
	public PlanItemInfo getPlanInfo() {
		return planInfo;
	}

}
