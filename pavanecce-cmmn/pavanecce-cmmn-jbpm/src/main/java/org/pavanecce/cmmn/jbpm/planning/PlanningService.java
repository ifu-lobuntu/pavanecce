package org.pavanecce.cmmn.jbpm.planning;

import java.util.Collection;
import java.util.HashSet;

import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.WorkItemManager;
import org.jbpm.services.task.impl.TaskServiceEntryPointImpl;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.pavanecce.cmmn.jbpm.lifecycle.ControllableItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public class PlanningService {
	TaskServiceEntryPointImpl taskService;
	private RuntimeManager runtimeManager;

	public RuntimeManager getRuntimeManager() {
		return runtimeManager;
	}

	public void setRuntimeManager(RuntimeManager runtimeManager) {
		this.runtimeManager = runtimeManager;
	}
	public void submitPlan(long processInstanceId, long parentTaskId, Collection<PlannedTask> plannedTasks){
		RuntimeEngine runtime = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
		long workItemId = getWorkItemId(parentTaskId);
		CaseInstance ci = (CaseInstance) runtime.getKieSession().getProcessInstance(processInstanceId);
		for (PlannedTask plannedTask : plannedTasks) {
			WorkItemManager wim = (WorkItemManager)runtime.getKieSession().getWorkItemManager();
			WorkItem wi=wim.getWorkItem(plannedTask.getTaskData().getWorkItemId());
			ControllableItemInstanceLifecycle<?> pi= ci.ensurePlanItemCreated(workItemId,  plannedTask.getDiscretionaryItemId(),wi);
		}
	}
	public Collection<PlannedTask> getPlannedItemsForParentTask(long taskId) {
		RuntimeEngine runtime = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(taskService.getTaskById(taskId).getTaskData().getProcessInstanceId()));
		Collection<PlannedTask> result = new HashSet<PlannedTask>();
		for (TaskSummary ts : taskService.getSubTasksByParent(taskId)) {
			//lazily find or create the plannedTasks once you found out where the persistenceManager is.
			throw new RuntimeException();
		}
		return result;
	}

	public PlannedTask preparePlannedTask(long processInstanceId, long parentTaskId, String discretionaryItemId) {
		RuntimeEngine runtime = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
		CaseInstance ci = (CaseInstance) runtime.getKieSession().getProcessInstance(processInstanceId);
		WorkItem wi = ci.createPlannedItem(getWorkItemId(parentTaskId), discretionaryItemId);
		return (PlannedTask) taskService.getTaskByWorkItemId(wi.getId());
	}

	protected long getWorkItemId(long parentTaskId) {
		return taskService.getTaskById(parentTaskId).getTaskData().getWorkItemId();
	}
}
