package org.pavanecce.cmmn.jbpm.instance.impl;

import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.node.StateNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.MilestonePlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInstanceFactoryNode;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceLifecycleWithHistory;

/**
 * This class represents the lifecycle of controllablePlanInstances prior to instantiation of the PlanItem in question
 * 
 * @author ampie
 * 
 */
public class PlanItemInstanceFactoryNodeInstance<T extends PlanItemDefinition> extends StateNodeInstance implements PlanItemInstanceLifecycleWithHistory<T> {

	private static final long serialVersionUID = -5291618101988431033L;
	private Boolean isPlanItemInstanceRequired;
	private Boolean isPlanItemInstanceStillRequired;
	private PlanElementState planElementState = PlanElementState.INITIAL;
	private PlanElementState lastBusyState = PlanElementState.NONE;

	public PlanElementState getPlanElementState() {
		return planElementState;
	}

	public void setPlanElementState(PlanElementState planElementState) {
		this.planElementState = planElementState;
	}

	@Override
	public PlanItemInstanceFactoryNode getNode() {
		return (PlanItemInstanceFactoryNode) super.getNode();
	}

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		if (planElementState == PlanElementState.SUSPENDED || planElementState == PlanElementState.TERMINATED) {
			// do nothing
		} else {
			super.internalTrigger(from, type);
			isPlanItemInstanceStillRequired = false;
			triggerCompleted(NodeImpl.CONNECTION_DEFAULT_TYPE, false);
		}
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
		((AbstractControllablePlanInstance<?>) nodeInstance).internalSetCompletionRequired(isPlanItemInstanceRequired);
		super.triggerNodeInstance(nodeInstance, type);
	}

	public boolean shouldRepeat() {
		PlanItem<?> toEnter = getNode().getPlanItem();
		boolean val = false;
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

	public void suspend() {
		planElementState.suspend(this);
	}

	@Override
	public void create() {
		planElementState.create(this);
	}

	@Override
	public void terminate() {
		planElementState.terminate(this);
	}

	@Override
	public String getPlanItemName() {
		return getPlanItem().getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public PlanItem<T> getPlanItem() {
		return (PlanItem<T>) getNode().getPlanItem();
	}

	@Override
	public void parentSuspend() {
		planElementState.parentSuspend(this);
	}

	@Override
	public void parentResume() {
		planElementState.parentResume(this);
	}

	@Override
	public void setLastBusyState(PlanElementState s) {
		this.lastBusyState = s;
	}

	@Override
	public PlanElementState getLastBusyState() {
		return this.lastBusyState;
	}
}
