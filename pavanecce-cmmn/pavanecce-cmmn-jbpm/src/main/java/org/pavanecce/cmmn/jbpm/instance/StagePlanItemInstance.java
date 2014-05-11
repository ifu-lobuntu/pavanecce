package org.pavanecce.cmmn.jbpm.instance;

import java.util.HashMap;

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
import org.kie.internal.runtime.KnowledgeRuntime;
import org.pavanecce.cmmn.jbpm.flow.StagePlanItem;

public class StagePlanItemInstance extends CompositeNodeInstance {

	private static final long serialVersionUID = 112341234123L;
	private long workItemId;
	private WorkItem workItem;

	@Override
	protected CompositeNode getCompositeNode() {
		return super.getCompositeNode();
	}

	public void exit() {
		// TODO
		cancel();
	}

	public void suspend() {
		// TODO
	}

	public void resume() {
		// TODO
	}

	public void applyPlanning(WorkItem workItem) {
		// TODO

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
		getProcessInstance().addEventListener("workItemCompleted", this, false);
		getProcessInstance().addEventListener("workItemAborted", this, false);
	}

	public void removeEventListeners() {
		super.removeEventListeners();
		getProcessInstance().removeEventListener("workItemCompleted", this, false);
		getProcessInstance().removeEventListener("workItemAborted", this, false);
	}

	public void signalEvent(String type, Object event) {
		if ("workItemCompleted".equals(type)) {
			workItemCompleted((WorkItem) event);
		} else if ("workItemAborted".equals(type)) {
			workItemAborted((WorkItem) event);
		} else {
			super.signalEvent(type, event);
		}
	}

	public String[] getEventTypes() {
		return new String[] { "workItemCompleted" };
	}

	public WorkItem getWorkItem() {
		if (workItem == null && workItemId >= 0) {
			workItem = ((WorkItemManager) ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getWorkItemManager()).getWorkItem(workItemId);
		}
		return workItem;
	}

	public long getWorkItemId() {
		return workItemId;
	}

	public void workItemAborted(WorkItem workItem) {
		if (workItemId == workItem.getId() || (workItemId == -1 && getWorkItem().getId() == workItem.getId())) {
			removeEventListeners();
			triggerCompleted(workItem);
		}
	}

	public void workItemCompleted(WorkItem workItem) {
		if (workItemId == workItem.getId() || (workItemId == -1 && getWorkItem().getId() == workItem.getId())) {
			removeEventListeners();
			triggerCompleted(workItem);
		}
	}

	public void internalSetWorkItemId(long readLong) {
		this.workItemId = readLong;
	}

	public void internalTrigger(final NodeInstance from, String type) {
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
		((WorkItem) workItem).setParameter("planningTable", "");// TODO
		return workItem;
	}

}
