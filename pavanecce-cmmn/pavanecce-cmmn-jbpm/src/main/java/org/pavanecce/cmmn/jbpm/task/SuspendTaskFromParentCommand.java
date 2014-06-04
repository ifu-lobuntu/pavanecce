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

public class SuspendTaskFromParentCommand extends TaskCommand<Void> {
	private static final long serialVersionUID = -8257771889718694139L;

	public SuspendTaskFromParentCommand(long taskId, String user) {
		super.taskId = taskId;
		this.userId = user;
	}

	@SuppressWarnings("serial")
	public Void execute(Context cntxt) {
		TaskServiceEntryPointImpl ts = ((TaskContext) cntxt).getTaskService();
		Task task = ts.getTaskInstanceById(taskId);
		InternalTaskData td = (InternalTaskData) task.getTaskData();
		if (!(task.getTaskData().getStatus() == Status.Ready || task.getTaskData().getStatus() == Status.Reserved || task.getTaskData().getStatus() == Status.InProgress)) {
			String errorMessage = "Only tasks in the Ready, Reserved or InProgress status can be suspended from parent. Task" + task.getId() + " is "
					+ task.getTaskData().getStatus();
			throw new PermissionDeniedException(errorMessage);
		}
		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<BeforeTaskSuspendedFromParentEvent>() {
		}).fire(task);
		td.setStatus(Status.Suspended);
		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<AfterTaskSuspendedFromParentEvent>() {
		}).fire(task);

		return null;
	}

	public String toString() {
		return "taskService.parentSuspend(" + taskId + ", " + userId + ");";
	}

}
