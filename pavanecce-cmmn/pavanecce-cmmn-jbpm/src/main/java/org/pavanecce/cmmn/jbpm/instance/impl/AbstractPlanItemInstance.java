package org.pavanecce.cmmn.jbpm.instance.impl;

import org.jbpm.workflow.instance.node.StateBasedNodeInstance;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceLifecycle;

public abstract class AbstractPlanItemInstance<T extends PlanItemDefinition> extends StateBasedNodeInstance implements PlanItemInstanceLifecycle<T> {

	private static final long serialVersionUID = -6433456059496295872L;
	protected PlanElementState planElementState = PlanElementState.AVAILABLE;
	protected Boolean isCompletionRequired;

	public AbstractPlanItemInstance() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public PlanItem<T> getPlanItem() {
		return (PlanItem<T>) getNode();
	}

	@Override
	public void setPlanElementState(PlanElementState s) {
		this.planElementState = s;
	}

	public boolean isCompletionRequired() {
		return isCompletionRequired;
	}

	public boolean isCompletionStillRequired() {
		return isCompletionRequired && !planElementState.isTerminalState() && !planElementState.isSemiTerminalState(this);
	}

	public void internalSetCompletionRequired(boolean b) {
		this.isCompletionRequired = b;
	}

	@Override
	public void suspend() {
		planElementState.suspend(this);
	}

	public void resume() {
		planElementState.resume(this);
	}

	@Override
	public void terminate() {
		if (planElementState != PlanElementState.TERMINATED) {
			// Could have been fired by exiting event
			planElementState.terminate(this);
		}
	}

	@Override
	public PlanElementState getPlanElementState() {
		return planElementState;
	}

	@Override
	public CaseInstance getCaseInstance() {
		return (CaseInstance) getProcessInstance();
	}

	@Override
	public String getPlanItemName() {
		return getPlanItem().getName();
	}

}