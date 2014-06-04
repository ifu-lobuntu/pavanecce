package org.pavanecce.cmmn.jbpm.task;

import java.util.Map;

import javax.enterprise.util.AnnotationLiteral;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.jbpm.services.task.impl.TaskServiceEntryPointImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.User;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.model.InternalTaskData;

/**
 * This is not the same as the 'claim' transition, as it is not initiated by a human. It is triggered by the
 * Process/Case Instance based on the absence or negative result of the ManualActivationRule
 */
public class AutomaticallyStartTaskCommand extends TaskCommand<Void> {
	private static final long serialVersionUID = -8257771889718694139L;
	private Map<String, Object> updatedParameters;

	public AutomaticallyStartTaskCommand(long taskId, String user, Map<String, Object> map) {
		super.taskId = taskId;
		this.updatedParameters = map;
		this.userId = user;
	}

	@SuppressWarnings("serial")
	public Void execute(Context cntxt) {
		TaskServiceEntryPointImpl ts = ((TaskContext) cntxt).getTaskService();
		Task task = ts.getTaskInstanceById(taskId);
		InternalTaskData td = (InternalTaskData) task.getTaskData();
		if (task.getTaskData().getStatus() != Status.Created && task.getTaskData().getStatus() != Status.Ready
				&& task.getTaskData().getStatus() != Status.Reserved) {
			String errorMessage = "Only tasks in the Created/Ready or Reserved status can be auomatically started. Task" + task.getId() + " is "
					+ task.getTaskData().getStatus();
			throw new PermissionDeniedException(errorMessage);
		}
		User user = new UserImpl(userId);
		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<BeforeTaskStartedAutomaticallyEvent>() {
		}).fire(task);
		ts.addContent(task.getId(), updatedParameters);
		td.setStatus(Status.InProgress);
		td.setActualOwner(user);
		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<AfterTaskStartedAutomaticallyEvent>() {
		}).fire(task);

		return null;
	}

	public String toString() {
		return "taskService.reenable(" + taskId + ", " + userId + ");";
	}

}
