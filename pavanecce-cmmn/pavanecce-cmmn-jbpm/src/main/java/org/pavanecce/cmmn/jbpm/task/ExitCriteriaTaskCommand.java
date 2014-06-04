package org.pavanecce.cmmn.jbpm.task;

import javax.enterprise.util.AnnotationLiteral;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.jbpm.services.task.impl.TaskServiceEntryPointImpl;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.model.InternalTaskData;

/**
 * This is not the same as the 'exit' transition in WS Human Task, as it is not initiated by a human. It is triggered by
 * the Process/Case Instance based on the exitCriteria becoming true;
 */
public class ExitCriteriaTaskCommand extends TaskCommand<Void> {
	private static final long serialVersionUID = -8257771889718694139L;

	public ExitCriteriaTaskCommand(long taskId) {
		super.taskId = taskId;
	}

	@SuppressWarnings("serial")
	public Void execute(Context cntxt) {
		TaskServiceEntryPointImpl ts = ((TaskContext) cntxt).getTaskService();
		Task task = ts.getTaskInstanceById(taskId);
		InternalTaskData td = (InternalTaskData) task.getTaskData();
		if (task.getTaskData().getStatus() == Status.Exited || task.getTaskData().getStatus() == Status.Completed) {
			String errorMessage = "Tasks in the Exited or Completed status can be exited started. Task" + task.getId() + " is "
					+ task.getTaskData().getStatus();
			throw new PermissionDeniedException(errorMessage);
		}
		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<BeforeExitCriteriaEvent>() {
		}).fire(task);
		td.setStatus(Status.Exited);
		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<AfterExitCriteriaEvent>() {
		}).fire(task);

		return null;
	}

	public String toString() {
		return "taskService.exit(" + taskId + ", " + userId + ");";
	}

}
