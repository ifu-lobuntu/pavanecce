package org.pavanecce.cmmn.jbpm.task;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.model.InternalTaskData;
import org.pavanecce.cmmn.jbpm.planning.PlannedTask;
import org.pavanecce.cmmn.jbpm.planning.PlannedTaskImpl;
import org.pavanecce.cmmn.jbpm.planning.PlanningStatus;

public class AddPlannedTaskCommand extends AbstractTaskCommand<PlannedTask> {
	private static final long serialVersionUID = 2919984132940815456L;
	private Map<String, Object> inputParameters;
	private Task task;
	private String discretionaryItemId;

	public AddPlannedTaskCommand(JbpmServicesPersistenceManager pm, Map<String, Object> inputParameters, Task task, String discretionaryItemId) {
		super(pm);
		this.inputParameters = inputParameters;
		this.task = task;
		this.discretionaryItemId = discretionaryItemId;
	}

	@Override
	public PlannedTask execute(Context context) {
		TaskContext tc = (TaskContext) context;
		init(tc.getTaskService());
		((InternalTaskData) task.getTaskData()).setStatus(Status.Created);
		taskId = tc.getTaskService().getTaskInstanceService().addTask(task, (Map<String, Object>) null);
		task = tc.getTaskService().getTaskById(taskId);
		((InternalTaskData) task.getTaskData()).setDocumentContentId(ensureContentPresent(task, -1, inputParameters, "Content"));
		((InternalTaskData) task.getTaskData()).setOutputContentId(ensureContentPresent(task, -1, new HashMap<String, Object>(), "Outpupt"));
		PlannedTaskImpl pt = new PlannedTaskImpl((TaskImpl) task);
		pt.setDiscretionaryItemId(discretionaryItemId);
		pt.setPlanningStatus(PlanningStatus.PLANNING_IN_PROGRESS);
		pm.persist(pt);
		return pt;
	}
}