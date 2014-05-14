package org.pavanecce.cmmn.jbpm.instance.impl;

import org.jbpm.workflow.instance.node.StateNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.MilestonePlanItem;
import org.pavanecce.cmmn.jbpm.instance.OccurrablePlanItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;

public class MilestonePlanItemInstance extends StateNodeInstance implements OccurrablePlanItemInstanceLifecycle{
	private static final long serialVersionUID = 3069593690659509023L;
	private Boolean isCompletionRequired;
	private PlanElementState planElementState=PlanElementState.AVAILABLE;
	@Override
	public void internalTrigger(NodeInstance from, String type) {
		super.internalTrigger(from, type);
		occur();
		triggerCompleted(false);
	}

	protected MilestonePlanItem getPlanItem() {
		return (MilestonePlanItem) getNode();
	}

	public void internalSetRequired(Boolean isPlanItemInstanceRequired) {
		this.isCompletionRequired=isPlanItemInstanceRequired;
	}
	public boolean isCompletionRequired(){
		return isCompletionRequired;
	}
	public boolean isCompletionStillRequired(){
		return isCompletionRequired && !(planElementState.isTerminalState() || planElementState.isSemiTerminalState());
	}

	@Override
	public String getPlanItemName() {
		return getPlanItem().getName();
	}

	@Override
	public void create() {
		planElementState.create(this);
	}

	@Override
	public void suspend() {
		planElementState.suspend(this);
	}

	@Override
	public void terminate() {
		planElementState.terminate(this);
	}

	@Override
	public void setPlanElementState(PlanElementState s) {
		this.planElementState=s;
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
	public void occur() {
		planElementState.occur(this);
	}

	@Override
	public void parentTerminate() {
		planElementState.parentTerminate(this);
		
	}

	@Override
	public void resume() {
		planElementState.resume(this);
	}


}
