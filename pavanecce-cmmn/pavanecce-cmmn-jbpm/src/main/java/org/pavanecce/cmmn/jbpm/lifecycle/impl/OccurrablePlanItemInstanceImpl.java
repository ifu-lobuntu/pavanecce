package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import org.jbpm.workflow.instance.node.StateBasedNodeInstance;
import org.pavanecce.cmmn.jbpm.flow.ItemWithDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.OccurrablePlanItemInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;

public abstract class OccurrablePlanItemInstanceImpl<T extends PlanItemDefinition, X extends ItemWithDefinition<T>> extends StateBasedNodeInstance implements
		OccurrablePlanItemInstance<T> {

	private static final long serialVersionUID = -3451322686745364562L;
	protected PlanElementState planElementState = PlanElementState.AVAILABLE;
	protected Boolean isCompletionRequired;

	public OccurrablePlanItemInstanceImpl() {
		super();
	}

	@Override
	public void internalSetRequired(boolean readBoolean) {
		this.isCompletionRequired = readBoolean;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PlanItem<T> getPlanItem() {
		return (PlanItem<T>) getNode();
	}

	public boolean canOccur() {
		return getPlanElementState() == PlanElementState.AVAILABLE || (getPlanElementState() == PlanElementState.COMPLETED);
	}

	public void internalSetRequired(Boolean isPlanItemInstanceRequired) {
		this.isCompletionRequired = isPlanItemInstanceRequired;
	}

	@Override
	public void create() {
		planElementState.create(this);
	}

	@Override
	public void occur() {
		planElementState.occur(this);
	}

	@Override
	public void parentTerminate() {
		planElementState.parentTerminate(this);

	}

	@SuppressWarnings("unchecked")
	public X getItem() {
		return (X) getNode();
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

}