package org.pavanecce.cmmn.jbpm.instance;

import org.drools.core.process.instance.WorkItem;
import org.drools.core.spi.ProcessContext;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.ReturnValueEvaluator;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.task.model.Task;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.HumanTaskPlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;

public class HumanTaskPlanItemInstance extends WorkItemNodeInstance implements PlanItemInstance, HumanControlledPlanItemInstance {
	private static final long serialVersionUID = 3200294767777991641L;
	private PlanItemState planItemState = PlanItemState.AVAILABLE;
	private PlanItemState lastBusyState = PlanItemState.NONE;
	private WorkItem workItem;

	@Override
	public ContextInstance resolveContextInstance(String contextId, Object param) {

		final ContextInstance result = super.resolveContextInstance(contextId, param);
		if (contextId.equals(VariableScope.VARIABLE_SCOPE)) {
			// TODO make caseParameters available??
			return new CustomVariableScopeInstance(result);
		}
		return result;
	}

	public void addEventListeners() {
		super.addEventListeners();
		addWorkItemUpdatedListener();
	}

	private void addWorkItemUpdatedListener() {
		getProcessInstance().addEventListener("workItemUpdated", this, false);
	}

	public void removeEventListeners() {
		super.removeEventListeners();
		getProcessInstance().addEventListener("workItemUpdated", this, false);
	}

	@Override
	protected WorkItem createWorkItem(WorkItemNode workItemNode) {
		WorkItem result = super.createWorkItem(workItemNode);
		PlanItem pi = (PlanItem) getNode();
		for (CaseParameter cp : ((TaskDefinition) pi.getPlanInfo().getDefinition()).getInputs()) {
			ReturnValueEvaluator el = cp.getBindingRefinementEvaluator();
			if (el != null) {
				try {
					ProcessContext ctx = new ProcessContext(getProcessInstance().getKnowledgeRuntime());
					ctx.setNodeInstance(this);
					result.setParameter(cp.getName(), el.evaluate(ctx));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				CaseFileItem variable = cp.getBoundVariable();
				VariableScopeInstance varContext = (VariableScopeInstance) resolveContextInstance(VariableScope.VARIABLE_SCOPE, variable.getName());
				result.setParameter(cp.getName(), varContext.getVariable(variable.getName()));
			}
		}
		return result;
	}

	@Override
	public WorkItem getWorkItem() {
		if (this.workItem == null) {
			this.workItem = super.getWorkItem();
		}
		return this.workItem;
	}

	@Override
	public void signalEvent(String type, Object event) {
		if (type.equals("workItemCompleted") || type.equals("workItemAborted")) {
			// ignore
		} else if (type.equals("workItemUpdated") && isMyWorkItem((WorkItem) event)) {
			this.workItem = (WorkItem) event;
			PlanItemTransition transition = (PlanItemTransition) workItem.getResult(HumanControlledPlanItemInstance.TRANSITION);
			transition.invokeOn(this);
		} else {
			super.signalEvent(type, event);
		}
	}

	protected boolean isMyWorkItem(WorkItem event) {
		return event.getId() == getWorkItemId() || (getWorkItemId() == -1 && getWorkItem().getId() == (event.getId()));
	}

	@Override
	public void triggerCompleted() {
		super.triggerCompleted();
		((CaseInstance) getProcessInstance()).markSubscriptionsForUpdate();
	}

	@Override
	public void cancel() {
		super.cancel();
		((CaseInstance) getProcessInstance()).markSubscriptionsForUpdate();
	}

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		super.internalTrigger(from, type);
		((CaseInstance) getProcessInstance()).markSubscriptionsForUpdate();
		if (getWorkItemNode().isWaitForCompletion()) {
			addWorkItemUpdatedListener();
		}
		PlanItemInstanceUtil.fulfillRequirementRule(getPlanItem(), this);
		if (PlanItemInstanceUtil.isActivatedAutomatically(this)) {
			this.start();
		} else {
			this.enable();
		}
	}

	@Override
	public HumanTaskPlanItem getPlanItem() {
		return (HumanTaskPlanItem) getNode();
	}

	@Override
	public void setPlanItemState(PlanItemState s) {
		this.planItemState = s;
	}

	@Override
	public void setLastBusyState(PlanItemState s) {
		this.lastBusyState = s;
	}

	@Override
	public void enable() {
		planItemState.enable(this);
	}

	@Override
	public void disable() {
		planItemState.disable(this);
	}

	@Override
	public void reenable() {
		planItemState.reenable(this);
	}

	@Override
	public void manualStart() {
		planItemState.manualStart(this);
	}

	@Override
	public void reactivate() {
		planItemState.reactivate(this);
	}

	@Override
	public void suspend() {
		planItemState.suspend(this);
	}

	@Override
	public void resume() {
		planItemState.resume(this);
	}

	@Override
	public void terminate() {
		if (planItemState != PlanItemState.TERMINATED) {
			// Could have been fired by exiting event
			planItemState.terminate(this);
		}
	}

	@Override
	public void exit() {
		planItemState.exit(this);

	}

	@Override
	public void complete() {
		planItemState.complete(this);
	}

	@Override
	public void parentSuspend() {
		planItemState.parentSuspend(this);
	}

	@Override
	public void parentResume() {
		planItemState.parentResume(this);
	}

	@Override
	public void parentTerminate() {
		planItemState.parentTerminate(this);
	}

	@Override
	public void create() {
		planItemState.create(this);
	}

	@Override
	public void fault() {
		planItemState.fault(this);
	}

	@Override
	public void occur() {
		planItemState.occur(this);
	}

	@Override
	public void close() {
		planItemState.close(this);
	}

	@Override
	public PlanItemState getLastBusyState() {
		return lastBusyState;
	}

	@Override
	public PlanItemState getPlanItemState() {
		return planItemState;
	}

	@Override
	public CaseInstance getCaseInstance() {
		return (CaseInstance) getProcessInstance();
	}

	@Override
	public void start() {
		planItemState.start(this);
	}

	@Override
	public Task getTask() {
		if (getWorkItem() != null) {
			return (Task) getWorkItem().getResult(HumanControlledPlanItemInstance.TASK);
		}
		return null;
	}

	@Override
	public String getPlanItemName() {
		return getPlanItem().getName();
	}

}
