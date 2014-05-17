package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.node.StateNode;

public class PlanItemInstanceFactoryNode extends StateNode {

	private static final long serialVersionUID = -3811996856528514976L;
	private PlanItem<?> planItem;
	public PlanItemInstanceFactoryNode() {
	}
	public PlanItem<?> getPlanItem() {
		return planItem;
	}
	public void setPlanItem(PlanItem<?> planItem) {
		this.planItem = planItem;
	}
}
