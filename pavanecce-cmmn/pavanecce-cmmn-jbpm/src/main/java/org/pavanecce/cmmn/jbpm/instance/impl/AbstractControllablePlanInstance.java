package org.pavanecce.cmmn.jbpm.instance.impl;

import org.drools.core.WorkItemHandlerNotFoundException;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.WorkItemManager;
import org.jbpm.process.core.context.exception.ExceptionScope;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.exception.ExceptionScopeInstance;
import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.jbpm.workflow.instance.node.EventBasedNodeInstanceInterface;
import org.jbpm.workflow.instance.node.StateBasedNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.task.model.Task;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.instance.CaseElementLifecycleWithTask;
import org.pavanecce.cmmn.jbpm.instance.ControllablePlanItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.instance.CustomVariableScopeInstance;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;

public abstract class AbstractControllablePlanInstance<T extends PlanItemDefinition> extends StateBasedNodeInstance implements ControllablePlanItemInstanceLifecycle<T>,
		EventBasedNodeInstanceInterface {

	private static final long serialVersionUID = 3200294767777991641L;

	protected abstract void createWorkItem();

	private PlanElementState planElementState = PlanElementState.AVAILABLE;
	private PlanElementState lastBusyState = PlanElementState.NONE;
	protected WorkItem workItem;
	private long workItemId;
	private Boolean isCompletionRequired;

	public AbstractControllablePlanInstance() {
		super();
	}

	public boolean isCompletionStillRequired() {
		return isCompletionRequired && !planElementState.isTerminalState() && !planElementState.isSemiTerminalState();
	}

	public boolean isCompletionRequired() {
		return isCompletionRequired;
	}

	public void internalSetCompletionRequired(boolean b) {
		this.isCompletionRequired = b;
	}

	public void calcIsRequired() {
		if (isCompletionRequired == null) {
			PlanItem<T> toEnter = getPlanItem();
			if (toEnter.getPlanInfo().getItemControl() != null && toEnter.getPlanInfo().getItemControl().getRequiredRule() instanceof ConstraintEvaluator) {
				ConstraintEvaluator constraintEvaluator = (ConstraintEvaluator) toEnter.getPlanInfo().getItemControl().getRequiredRule();
				isCompletionRequired = constraintEvaluator.evaluate(this, null, constraintEvaluator);
			} else {
				isCompletionRequired = Boolean.FALSE;
			}
		}
	}

	protected abstract boolean isWaitForCompletion();

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		super.internalTrigger(from, type);
		((CaseInstance) getProcessInstance()).markSubscriptionsForUpdate();
		createWorkItem();
		if (isWaitForCompletion()) {
			addWorkItemUpdatedListener();
		}
		String deploymentId = (String) getProcessInstance().getKnowledgeRuntime().getEnvironment().get("deploymentId");
		workItem.setDeploymentId(deploymentId);
		workItem.setParameter(COMMENT, getPlanItem().getDescription());
		if (getNodeInstanceContainer() instanceof CaseElementLifecycleWithTask) {
			long parentWorkItemId = ((CaseElementLifecycleWithTask) getNodeInstanceContainer()).getWorkItemId();
			if (parentWorkItemId >= 0) {
				workItem.setParameter(PARENT_WORK_ITEM_ID, parentWorkItemId);
			}
		}

		if (isInversionOfControl()) {
			((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().update(((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getFactHandle(this), this);
		} else {
			try {
				((WorkItemManager) ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getWorkItemManager()).internalExecuteWorkItem(workItem);
			} catch (WorkItemHandlerNotFoundException wihnfe) {
				getProcessInstance().setState(org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED);
				throw wihnfe;
			} catch (Exception e) {
				String exceptionName = e.getClass().getName();
				ExceptionScopeInstance exceptionScopeInstance = (ExceptionScopeInstance) resolveContextInstance(ExceptionScope.EXCEPTION_SCOPE, exceptionName);
				if (exceptionScopeInstance == null) {
					throw new WorkflowRuntimeException(this, getProcessInstance(), "Unable to execute Action: " + e.getMessage(), e);
				}
				// workItemId must be set otherwise cancel activity will not find the right work item
				this.workItemId = workItem.getId();
				exceptionScopeInstance.handleException(exceptionName, e);
			}
		}
		this.workItemId = workItem.getId();

		if (PlanItemInstanceUtil.isActivatedAutomatically(this)) {
			this.start();
		} else {
			this.enable();
		}
		if (!isWaitForCompletion()) {
			triggerCompleted();
		}
	}

	@Override
	public long getWorkItemId() {
		return workItemId;
	}

	@Override
	public ContextInstance resolveContextInstance(String contextId, Object param) {

		final ContextInstance result = super.resolveContextInstance(contextId, param);
		if (contextId.equals(VariableScope.VARIABLE_SCOPE)) {
			// TODO make caseParameters available??
			return new CustomVariableScopeInstance(result);
		}
		return result;
	}

	@Override
	public void addEventListeners() {
		super.addEventListeners();
		addWorkItemUpdatedListener();
	}

	private void addWorkItemUpdatedListener() {
		getProcessInstance().addEventListener(WORK_ITEM_UPDATED, this, false);
	}

	@Override
	public void removeEventListeners() {
		super.removeEventListeners();
		getProcessInstance().addEventListener(WORK_ITEM_UPDATED, this, false);
	}

	@Override
	public WorkItem getWorkItem() {
		if (this.workItem == null) {
			workItem = ((WorkItemManager) ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getWorkItemManager()).getWorkItem(workItemId);

		}
		return this.workItem;
	}

	@Override
	public String[] getEventTypes() {
		return new String[] { WORK_ITEM_UPDATED };
	}

	@Override
	public void signalEvent(String type, Object event) {
		if (type.equals(WORK_ITEM_UPDATED) && isMyWorkItem((WorkItem) event)) {
			this.workItem = (WorkItem) event;
			PlanItemTransition transition = (PlanItemTransition) workItem.getResult(HumanTaskPlanItemInstance.TRANSITION);
			if (transition == PlanItemTransition.TERMINATE && getPlanElementState().isTerminalState()) {
				// do nothing - triggered by TaskService in reaction to an event, e.g. exit or case closed from the
				// process
			} else {
				transition.invokeOn(this);
			}
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

	@SuppressWarnings("unchecked")
	@Override
	public PlanItem<T> getPlanItem() {
		return (PlanItem<T>) getNode();
	}

	@Override
	public void setPlanElementState(PlanElementState s) {
		this.planElementState = s;
	}

	@Override
	public void setLastBusyState(PlanElementState s) {
		this.lastBusyState = s;
	}

	@Override
	public void enable() {
		planElementState.enable(this);
	}

	@Override
	public void disable() {
		planElementState.disable(this);
	}

	@Override
	public void reenable() {
		planElementState.reenable(this);
	}

	@Override
	public void manualStart() {
		planElementState.manualStart(this);
	}

	@Override
	public void reactivate() {
		planElementState.reactivate(this);
	}

	@Override
	public void suspend() {
		planElementState.suspend(this);
	}

	@Override
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
	public void exit() {
		planElementState.exit(this);
	}

	@Override
	public void complete() {
		planElementState.complete(this);
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
	public void create() {
		planElementState.create(this);
	}

	@Override
	public void fault() {
		planElementState.fault(this);
	}

	@Override
	public PlanElementState getLastBusyState() {
		return lastBusyState;
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
	public void start() {
		planElementState.start(this);
	}

	@Override
	public Task getTask() {
		if (getWorkItem() != null) {
			return (Task) getWorkItem().getResult(HumanTaskPlanItemInstance.TASK);
		}
		return null;
	}

	@Override
	public String getPlanItemName() {
		return getPlanItem().getName();
	}

	public void internalSetWorkItemId(long readLong) {
		this.workItemId = readLong;
	}

	@Override
	public String getHumanTaskName() {
		return getPlanItemName();
	}

}