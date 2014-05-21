package org.pavanecce.cmmn.jbpm.planning;

import java.util.Collection;

import javax.inject.Inject;

import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.InternalTaskService;

public class PlanningService {
	@Inject
	private JbpmServicesPersistenceManager pm;
	private InternalTaskService taskService;
	private RuntimeManager runtimeManager;

	public RuntimeManager getRuntimeManager() {
		return runtimeManager;
	}

	public void setRuntimeManager(RuntimeManager runtimeManager) {
		this.runtimeManager = runtimeManager;
	}

	public void setTaskService(InternalTaskService taskService) {
		this.taskService = taskService;
	}

	public void submitPlan(final long parentTaskId, final Collection<PlannedTask> plannedTasks) {
		taskService.execute(new SubmitPlanCommand(runtimeManager, plannedTasks, parentTaskId));
	}

	public PlanningTableInstance getPlanningTable(final long parentTaskId, String user) {
		return new PlanningTableInstance(getPlannedItemsForParentTask(parentTaskId, true), getApplicableDiscretionaryItems(parentTaskId, user));
	}

	private Collection<ApplicableDiscretionaryItem> getApplicableDiscretionaryItems(final long parentTaskId, final String user) {
		return taskService.execute(new GetApplicableDiscretionaryItemsCommand(runtimeManager, parentTaskId, user));

	}

	private Collection<PlannedTaskSummary> getPlannedItemsForParentTask(final long parentTaskId, final boolean createMissing) {
		return taskService.execute(new GetPlannedItemsForParentTaskCommand(pm, parentTaskId, createMissing));
	}

	public PlannedTask preparePlannedTask(final long parentTaskId, final String discretionaryItemId) {
		return taskService.execute(new PreparePlannedTaskCommand(runtimeManager, pm, discretionaryItemId, parentTaskId));
	}

	protected long getWorkItemId(long parentTaskId) {
		return taskService.getTaskById(parentTaskId).getTaskData().getWorkItemId();
	}

	public PlannedTask getPlannedTaskById(final long id) {
		return taskService.execute(new AbstractPlanningCommand<PlannedTask>() {

			@Override
			public PlannedTask execute(Context context) {
				init(((TaskContext) context).getTaskService());
				return pm.find(PlannedTaskImpl.class, id);
			}
		});
	}
}
