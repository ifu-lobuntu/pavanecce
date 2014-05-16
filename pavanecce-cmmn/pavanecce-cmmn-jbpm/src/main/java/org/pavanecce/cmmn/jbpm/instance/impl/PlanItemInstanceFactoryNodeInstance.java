package org.pavanecce.cmmn.jbpm.instance.impl;

import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.node.StateNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.MilestonePlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInstanceFactoryNode;

public class PlanItemInstanceFactoryNodeInstance extends StateNodeInstance {

	private static final long serialVersionUID = -5291618101988431033L;
	private Boolean isPlanItemInstanceRequired;
	private Boolean isPlanItemInstanceStillRequired;

	@Override
	public PlanItemInstanceFactoryNode getNode() {
		return (PlanItemInstanceFactoryNode) super.getNode();
	}

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		super.internalTrigger(from, type);
		isPlanItemInstanceStillRequired = false;
		triggerCompleted(NodeImpl.CONNECTION_DEFAULT_TYPE,false);
	}

	public void calcIsRequired() {
		PlanItem<?> toEnter = getNode().getPlanItem();
		if (isPlanItemInstanceRequired == null) {
			if (toEnter.getPlanInfo().getItemControl() != null && toEnter.getPlanInfo().getItemControl().getRequiredRule() instanceof ConstraintEvaluator) {
				ConstraintEvaluator constraintEvaluator = (ConstraintEvaluator) toEnter.getPlanInfo().getItemControl().getRequiredRule();
				isPlanItemInstanceRequired = constraintEvaluator.evaluate(this, null, constraintEvaluator);
			} else {
				isPlanItemInstanceRequired = Boolean.FALSE;
			}
			if (toEnter instanceof MilestonePlanItem) {
				MilestonePlanItemInstance ni = (MilestonePlanItemInstance) ((NodeInstanceContainer) getNodeInstanceContainer()).getNodeInstance(toEnter);
				ni.internalSetRequired(isPlanItemInstanceRequired);
			}
		}
		if (isPlanItemInstanceStillRequired == null) {
			isPlanItemInstanceStillRequired = isPlanItemInstanceRequired;
		}
	}
	@Override
	protected void triggerNodeInstance(org.jbpm.workflow.instance.NodeInstance nodeInstance, String type) {
		((AbstractControllablePlanInstance<?>)nodeInstance).internalSetCompletionRequired(isPlanItemInstanceRequired);
		super.triggerNodeInstance(nodeInstance, type);
	}

	public boolean shouldRepeat() {
		PlanItem<?> toEnter = getNode().getPlanItem();
		boolean val=false;
		if (toEnter.getPlanInfo().getItemControl() != null && toEnter.getPlanInfo().getItemControl().getRepetitionRule() instanceof ConstraintEvaluator) {
			ConstraintEvaluator constraintEvaluator = (ConstraintEvaluator) toEnter.getPlanInfo().getItemControl().getRepetitionRule();
			val = constraintEvaluator.evaluate(this, null, constraintEvaluator);
		}
		return val;
	}

	public boolean isPlanItemInstanceRequired() {
		return isPlanItemInstanceRequired;
	}

	public void internalSetPlanItemInstanceRequired(boolean isPlanItemInstanceRequired) {
		this.isPlanItemInstanceRequired = isPlanItemInstanceRequired;
	}

	public boolean isPlanItemInstanceStillRequired() {
		if (isPlanItemInstanceStillRequired == null) {
			// still initializing
			return true;
		}
		return isPlanItemInstanceStillRequired;
	}

	public void internalSetPlanItemInstanceStillRequired(boolean val) {
		this.isPlanItemInstanceStillRequired = val;
	}

	public CaseInstance getCaseInstance() {
		return (CaseInstance) getProcessInstance();
	}
}
