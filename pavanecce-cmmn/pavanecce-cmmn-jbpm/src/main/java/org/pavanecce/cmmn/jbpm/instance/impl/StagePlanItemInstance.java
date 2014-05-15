package org.pavanecce.cmmn.jbpm.instance.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.drools.core.WorkItemHandlerNotFoundException;
import org.drools.core.process.core.Work;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.core.context.exception.ExceptionScope;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.exception.ExceptionScopeInstance;
import org.jbpm.workflow.core.node.CompositeNode;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.jbpm.workflow.instance.node.CompositeNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.KnowledgeRuntime;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemContainer;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.flow.Stage;
import org.pavanecce.cmmn.jbpm.flow.StagePlanItem;
import org.pavanecce.cmmn.jbpm.instance.CaseElementLifecycleWithTask;
import org.pavanecce.cmmn.jbpm.instance.ControllablePlanItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceContainer;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceLifecycle;

public class StagePlanItemInstance extends CompositeNodeInstance implements PlanItemInstanceContainer, ControllablePlanItemInstanceLifecycle<Stage> {

	private static final long serialVersionUID = 112341234123L;
	private long workItemId;
	transient private WorkItem workItem;
	private PlanElementState planElementState = PlanElementState.AVAILABLE;
	private PlanElementState lastBusyState = PlanElementState.NONE;
	private Boolean isCompletionRequired;

	@Override
	protected CompositeNode getCompositeNode() {
		return super.getCompositeNode();
	}

	public WorkItem getWorkItem() {
		if (workItem == null && workItemId >= 0) {
			workItem = ((WorkItemManager) ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getWorkItemManager()).getWorkItem(workItemId);
		}
		return workItem;
	}

	public boolean isCompletionStillRequired() {
		return isCompletionRequired && !(planElementState.isTerminalState() || planElementState.isSemiTerminalState());
	}

	public long getWorkItemId() {
		return workItemId;
	}

	public void internalSetWorkItemId(long readLong) {
		this.workItemId = readLong;
	}

	public void internalTrigger(final NodeInstance from, String type) {
		this.isCompletionRequired=false;
		super.internalTrigger(from, type);
		StagePlanItem workItemNode = getStagePlanItem();
		createWorkItem(workItemNode);
		addWorkItemListener();
		String deploymentId = (String) getProcessInstance().getKnowledgeRuntime().getEnvironment().get("deploymentId");
		((WorkItem) workItem).setDeploymentId(deploymentId);
		if (isInversionOfControl()) {
			((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().update(((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getFactHandle(this), this);
		} else {
			try {
				((WorkItemManager) ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getWorkItemManager()).internalExecuteWorkItem((org.drools.core.process.instance.WorkItem) workItem);
			} catch (WorkItemHandlerNotFoundException wihnfe) {
				getProcessInstance().setState(ProcessInstance.STATE_ABORTED);
				throw wihnfe;
			} catch (Exception e) {
				String exceptionName = e.getClass().getName();
				ExceptionScopeInstance exceptionScopeInstance = (ExceptionScopeInstance) resolveContextInstance(ExceptionScope.EXCEPTION_SCOPE, exceptionName);
				if (exceptionScopeInstance == null) {
					throw new WorkflowRuntimeException(this, getProcessInstance(), "Unable to execute Action: " + e.getMessage(), e);
				}
				this.workItemId = workItem.getId();
				exceptionScopeInstance.handleException(exceptionName, e);
			}
		}
		this.workItemId = workItem.getId();
		this.enable();
	}

	protected StagePlanItem getStagePlanItem() {
		return (StagePlanItem) getNode();
	}

	protected WorkItem createWorkItem(StagePlanItem workItemNode) {
		workItem = new WorkItemImpl();
		Work work = getStagePlanItem().getWork();
		((WorkItem) workItem).setName(work.getName());
		((WorkItem) workItem).setProcessInstanceId(getProcessInstance().getId());
		((WorkItem) workItem).setParameters(new HashMap<String, Object>(work.getParameters()));
		((WorkItem) workItem).setParameter("planningTable", "");// TODO
		return workItem;
	}

	public void triggerCompleted(WorkItem workItem) {
		this.workItem = workItem;
		StagePlanItem workItemNode = getStagePlanItem();
		if (workItemNode != null && workItem.getState() == WorkItem.COMPLETED) {
			// TODO fire events
		}
		if (isInversionOfControl()) {
			KnowledgeRuntime kruntime = ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime();
			kruntime.update(kruntime.getFactHandle(this), this);
		} else {
			triggerCompleted();
		}
	}

	public void cancel() {
		WorkItem workItem = getWorkItem();
		if (workItem != null && workItem.getState() != WorkItem.COMPLETED && workItem.getState() != WorkItem.ABORTED) {
			try {
				((WorkItemManager) ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getWorkItemManager()).internalAbortWorkItem(workItemId);
			} catch (WorkItemHandlerNotFoundException wihnfe) {
				getProcessInstance().setState(ProcessInstance.STATE_ABORTED);
				throw wihnfe;
			}
		}
		super.cancel();
	}

	public void addEventListeners() {
		super.addEventListeners();
		addWorkItemListener();
	}

	private void addWorkItemListener() {
		getProcessInstance().addEventListener("workItemUpdated", this, false);
	}

	public void removeEventListeners() {
		super.removeEventListeners();
		getProcessInstance().removeEventListener("workItemUpdated", this, false);
	}

	@Override
	public void signalEvent(String type, Object event) {
		if (type.equals("workItemUpdated") && isMyWorkItem((WorkItem) event)) {
			this.workItem = (WorkItem) event;
			PlanItemTransition transition = (PlanItemTransition) workItem.getResult(CaseElementLifecycleWithTask.TRANSITION);
			transition.invokeOn(this);
		} else {
			super.signalEvent(type, event);
		}
	}

	protected boolean isMyWorkItem(WorkItem event) {
		return event.getId() == getWorkItemId() || (getWorkItemId() == -1 && getWorkItem().getId() == (event.getId()));
	}

	public String[] getEventTypes() {
		return new String[] { "workItemUpdated" };
	}

	@Override
	public PlanItem<Stage> getPlanItem() {
		return getStagePlanItem();
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
		planElementState.terminate(this);
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
	public Collection<PlanItemInstanceLifecycle> getChildren() {
		Set<PlanItemInstanceLifecycle> result = new HashSet<PlanItemInstanceLifecycle>();
		for (NodeInstance nodeInstance : getNodeInstances()) {
			if (nodeInstance instanceof PlanItemInstanceLifecycle) {
				result.add((PlanItemInstanceLifecycle) nodeInstance);
			}
		}
		return result;
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
			return (Task) getWorkItem().getResult(CaseElementLifecycleWithTask.TASK);
		}
		return null;
	}

	@Override
	public String getPlanItemName() {
		return getPlanItem().getName();
	}

	@Override
	public String getHumanTaskName() {
		return getPlanItemName();
	}

	@Override
	public boolean canComplete() {
		return false;
	}

	@Override
	public PlanItemContainer getPlanItemContainer() {
		return getStagePlanItem().getStage();
	}

	@Override
	public boolean isCompletionRequired() {
		return isCompletionRequired;
	}

	@Override
	public void internalSetCompletionRequired(boolean b) {
		this.isCompletionRequired = b;
	}

}
