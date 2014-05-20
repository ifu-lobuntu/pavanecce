package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import org.jbpm.workflow.instance.node.StateBasedNodeInstance;
import org.pavanecce.cmmn.jbpm.flow.ItemWithDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItemControl;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.ItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;

public abstract class AbstractItemInstance<T extends PlanItemDefinition, X extends ItemWithDefinition<T>> extends StateBasedNodeInstance implements ItemInstanceLifecycle<T> {

	private static final long serialVersionUID = -6433456059496295872L;
	protected PlanElementState planElementState = PlanElementState.AVAILABLE;
	protected Boolean isCompletionRequired;

	public AbstractItemInstance() {
		super();
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
	public T getPlanItemDefinition() {
		return getItem().getDefinition();
	}
	@Override
	public String getItemName() {
		return getItem().getName();
	}
	@Override
	public PlanItemControl getItemControl() {
		if(getItem().getItemControl()==null){
			return getItem().getDefinition().getDefaultControl();
		}else{
			return getItem().getItemControl();
		}
	}
	@Override
	public CaseInstance getCaseInstance() {
		return (CaseInstance) getProcessInstance();
	}

}