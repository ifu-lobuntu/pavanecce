package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.instance.node.StateNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.ItemWithDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemControl;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInstanceFactoryNode;
import org.pavanecce.cmmn.jbpm.flow.Stage;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.ItemInstanceLifecycleWithHistory;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;

/**
 * This class represents the lifecycle of controllablePlanInstances prior to instantiation of the PlanItem in question
 * 
 * @author ampie
 * 
 */
public class PlanItemInstanceFactoryNodeInstance<T extends PlanItemDefinition> extends StateNodeInstance implements ItemInstanceLifecycleWithHistory<T>, Creatable  {

	private static final long serialVersionUID = -5291618101988431033L;
	private Boolean isPlanItemInstanceRequired;
	private boolean hasPlanItemBeenInstantiated=false;
	private Boolean isRepeating;
	private PlanElementState planElementState = PlanElementState.INITIAL;
	private PlanElementState lastBusyState = PlanElementState.NONE;

	public PlanItemInstanceFactoryNodeInstance() {
	}

	public PlanElementState getPlanElementState() {
		return planElementState;
	}

	public void setPlanElementState(PlanElementState planElementState) {
		this.planElementState = planElementState;
	}

	@Override
	public void ensureCreationIsTriggered() {
		if (planElementState == PlanElementState.INITIAL) {
			create();
			setLastBusyState(getPlanElementState());
		}
	}

	@Override
	public boolean isComplexLifecycle() {
		PlanItemDefinition def = getPlanItem().getPlanInfo().getDefinition();
		return def instanceof TaskDefinition || def instanceof Stage;
	}

	@Override
	public PlanItemInstanceFactoryNode getNode() {
		return (PlanItemInstanceFactoryNode) super.getNode();
	}

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		if (planElementState == PlanElementState.SUSPENDED || planElementState == PlanElementState.TERMINATED) {
			// do nothing
		} else if (!isHasPlanItemBeenInstantiated() || isRepeating()) {
			super.internalTrigger(from, type);
			hasPlanItemBeenInstantiated = true;
			triggerCompleted(NodeImpl.CONNECTION_DEFAULT_TYPE, false);
			setLastBusyState(getPlanElementState());
		}
	}

	@Override
	protected void triggerNodeInstance(org.jbpm.workflow.instance.NodeInstance nodeInstance, String type) {
		((AbstractItemInstance<?,?>) nodeInstance).internalSetCompletionRequired(isPlanItemInstanceRequired);
		super.triggerNodeInstance(nodeInstance, type);
	}

	public boolean isPlanItemInstanceRequired() {
		return isPlanItemInstanceRequired;
	}

	public void internalSetPlanItemInstanceRequired(boolean isPlanItemInstanceRequired) {
		this.isPlanItemInstanceRequired = isPlanItemInstanceRequired;
	}

	public boolean isPlanItemInstanceStillRequired() {
		if(hasPlanItemBeenInstantiated){
			return false;
		}else if(isPlanItemInstanceRequired == null) {
			// still initializing
			return true;
		}else{
			return isPlanItemInstanceRequired;
			
		}
	}
	public boolean isHasPlanItemBeenInstantiated() {
		return hasPlanItemBeenInstantiated;
	}
	public void internalSetHasPlanItemInstanceBeenInstantiated(boolean val) {
		this.hasPlanItemBeenInstantiated = val;
	}

	public CaseInstance getCaseInstance() {
		return (CaseInstance) getProcessInstance();
	}

	public void suspend() {
		planElementState.suspend(this);
	}

	@Override
	public void create() {
		ItemWithDefinition<T> planItem = getPlanItem();
		PlanItemInstanceFactoryNodeInstance<T> contextNodeInstance = this;
		this.isPlanItemInstanceRequired=PlanItemInstanceUtil.isRequired(planItem, contextNodeInstance);
		if (planItem.getItemControl() != null && planItem.getItemControl().getRepetitionRule() instanceof ConstraintEvaluator) {
			ConstraintEvaluator constraintEvaluator = (ConstraintEvaluator) planItem.getItemControl().getRepetitionRule();
			isRepeating = constraintEvaluator.evaluate(contextNodeInstance, null, constraintEvaluator);
		} else {
			isRepeating = false;
		}
		hasPlanItemBeenInstantiated=false;
		planElementState.create(contextNodeInstance);
	}

	public boolean isRepeating() {
		return isRepeating;
	}

	@Override
	public void resume() {
		planElementState.resume(this);

	}

	@Override
	public void terminate() {
		planElementState.terminate(this);
	}

	@Override
	public String getItemName() {
		return getPlanItem().getName();
	}

	@SuppressWarnings("unchecked")
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

	public void internalSetRepeating(boolean readBoolean) {
		this.isRepeating = readBoolean;
	}

	@Override
	public void parentTerminate() {
		if(isComplexLifecycle()){
			throw new IllegalStateException("Complex planItemInstances do not suppoer to parentTerminate");
		}else{
			planElementState.parentTerminate(this);
		}
	}

	@Override
	public void exit() {
		if(isComplexLifecycle()){
			planElementState.exit(this);
		}else{
			throw new IllegalStateException("Occurrable planItemInstances do not suppoer to exit");
		}
	}

	@Override
	public T getPlanItemDefinition() {
		return getPlanItemDefinition();
	}

	@Override
	public PlanItemControl getItemControl() {
		if(getPlanItem().getPlanInfo().getItemControl()==null){
			return getPlanItemDefinition().getDefaultControl();
		}else{
			return getPlanItem().getPlanInfo().getItemControl();
		}
	}
}
