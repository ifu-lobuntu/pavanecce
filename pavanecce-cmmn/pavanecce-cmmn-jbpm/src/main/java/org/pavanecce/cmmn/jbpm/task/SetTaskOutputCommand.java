package org.pavanecce.cmmn.jbpm.task;

import java.util.Map;

import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.task.model.Task;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.model.InternalTaskData;

public class SetTaskOutputCommand extends AbstractTaskCommand<Long> {

	private static final long serialVersionUID = 5765007237796573932L;
	Map<String, Object> outputAsMap;

	public SetTaskOutputCommand(JbpmServicesPersistenceManager pm, long taskId, Map<String, Object> outputAsMap) {
		super(pm);
		this.taskId = taskId;
		this.outputAsMap = outputAsMap;
	}

	public Long execute(Context cntxt) {
		TaskContext context = (TaskContext) cntxt;
		init(context.getTaskService());
		Task task = ts.getTaskById(taskId);
		InternalTaskData itd = (InternalTaskData) task.getTaskData();
		itd.setOutputContentId(ensureContentPresent(task, itd.getOutputContentId(), outputAsMap, "Output"));
		return (Long) task.getId();
	}
}
