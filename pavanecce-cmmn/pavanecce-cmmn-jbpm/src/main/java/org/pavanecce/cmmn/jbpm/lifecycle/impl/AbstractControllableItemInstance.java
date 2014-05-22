package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.HashMap;
import java.util.Map;

import org.drools.core.WorkItemHandlerNotFoundException;
import org.drools.core.process.core.Work;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.core.context.exception.ExceptionScope;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.exception.ExceptionScopeInstance;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.DiscretionaryItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemControl;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.flow.TableItem;
import org.pavanecce.cmmn.jbpm.flow.TaskItemWithDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.ControllableItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;

public abstract class AbstractControllableItemInstance<T extends PlanItemDefinition, X extends TaskItemWithDefinition<T>> extends CompositeContextNodeInstance implements
		ControllableItemInstanceLifecycle<T> {

	private static final long serialVersionUID = 3200294767777991641L;

	private PlanElementState lastBusyState = PlanElementState.NONE;
	protected WorkItem workItem;
	private long workItemId;
	protected PlanElementState planElementState = PlanElementState.AVAILABLE;
	protected Boolean isCompletionRequired;

	public AbstractControllableItemInstance() {
		super();
	}

	protected abstract String getIdealRole();

	protected abstract String getIdealOwner();

	public final WorkItemImpl createWorkItem(Work work) {
		return PlanItemInstanceUtil.createWorkItem(work, this, this.getItem().getDefinition());
	}

	protected String getBusinessAdministrators() {
		TaskItemWithDefinition<T> item = getItem();
		if (item instanceof PlanItem) {
			return TableItem.getPlannerRoles((PlanItem<?>) item);
		} else {
			return TableItem.getPlannerRoles((DiscretionaryItem<?>) item);
		}
	}

	protected boolean isBlocking() {
		return true;
	}

	@Override
	public boolean isComplexLifecycle() {
		return true;
	}

	@Override
	public void parentTerminate() {
		throw new IllegalStateException("Complex planItemInstances do not support to parentTerminate");
	}

	private boolean isActivatedAutomatically() {
		return !PlanItemInstanceUtil.isActivatedManually(this);
	}

	@Override
	public void triggerTransitionOnTask(PlanItemTransition transition) {
		WorkItemImpl wi = new WorkItemImpl();
		wi.setName(UPDATE_TASK_STATUS);
		wi.setParameter(TASK_TRANSITION, transition);
		wi.setParameter(WORK_ITEM_ID, getWorkItemId());
		wi.setParameter(PeopleAssignmentHelper.ACTOR_ID, getIdealOwner());
		wi.setParameter(PeopleAssignmentHelper.GROUP_ID, getIdealRole());
		wi.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, getBusinessAdministrators());
		executeWorkItem(wi);
	}

	protected Map<String, Object> buildParametersFor(PlanItemTransition transition) {
		return new HashMap<String, Object>();
	}

	public void internalTriggerWithoutInstantiation(NodeInstance from, String type, WorkItem wi) {
		super.internalTrigger(from, type);
		this.workItem = wi;
		this.workItemId = wi.getId();
		this.planElementState = PlanElementState.INITIAL;
	}

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		super.internalTrigger(from, type);
		((CaseInstance) getProcessInstance()).markSubscriptionsForUpdate();
		workItem = createWorkItem(getItem().getWork());
		if (isBlocking()) {
			addWorkItemUpdatedListener();
		}
		executeWorkItem(workItem);
		this.workItemId = workItem.getId();
		noteInstantiation();
		if (!isBlocking()) {
			triggerCompleted();
		}
	}

	public void noteInstantiation() {
		if (isCompletionRequired == null) {
			isCompletionRequired = PlanItemInstanceUtil.isRequired(getItem(), this);
		}
		if (isActivatedAutomatically()) {
			triggerTransitionOnTask(PlanItemTransition.START);
		} else {
			triggerTransitionOnTask(PlanItemTransition.ENABLE);
		}
	}

	public void executeWorkItem(WorkItem wi) {
		if (isInversionOfControl()) {
			((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().update(((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getFactHandle(this), this);
		} else {
			try {
				((WorkItemManager) ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getWorkItemManager()).internalExecuteWorkItem(wi);
			} catch (WorkItemHandlerNotFoundException wihnfe) {
				getProcessInstance().setState(org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED);
				throw wihnfe;
			} catch (Exception e) {
				e.printStackTrace();
				String exceptionName = e.getClass().getName();
				ExceptionScopeInstance exceptionScopeInstance = (ExceptionScopeInstance) resolveContextInstance(ExceptionScope.EXCEPTION_SCOPE, exceptionName);
				if (exceptionScopeInstance == null) {
					throw new WorkflowRuntimeException(this, getProcessInstance(), "Unable to execute Action: " + e.getMessage(), e);
				}
				this.workItemId = wi.getId();
				exceptionScopeInstance.handleException(exceptionName, e);
			}
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
	public void start() {
		planElementState.start(this);
	}

	@Override
	public Object getTask() {
		if (getWorkItem() != null) {
			return (Object) getWorkItem().getResult(TASK);
		}
		return null;
	}

	public void internalSetWorkItemId(long readLong) {
		this.workItemId = readLong;
	}

	@Override
	public String getHumanTaskName() {
		return getItemName();
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
		if (getItem().getItemControl() == null) {
			return getItem().getDefinition().getDefaultControl();
		} else {
			return getItem().getItemControl();
		}
	}

	@Override
	public CaseInstance getCaseInstance() {
		return (CaseInstance) getProcessInstance();
	}

}