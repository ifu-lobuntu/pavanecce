package org.pavanecce.cmmn.jbpm.task;

import java.util.Map;

import javax.enterprise.util.AnnotationLiteral;

import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.events.AfterTaskCompletedEvent;
import org.jbpm.services.task.events.BeforeTaskCompletedEvent;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.User;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.model.InternalTaskData;

/**
 * 
 * Unlike the default implementation in jBPM, this one uses the Object strategies in the environment
 * 
 */
public class CompleteTaskCommand extends SetTaskOutputCommand {

	private static final long serialVersionUID = -1817334359933358605L;

	public CompleteTaskCommand(JbpmServicesPersistenceManager pm, long taskId, String userId, Map<String, Object> data) {
		super(pm, taskId, data);
		this.taskId = taskId;
		this.userId = userId;
	}

	@SuppressWarnings("serial")
	public Long execute(Context cntxt) {
		TaskContext c = (TaskContext) cntxt;
		init(c.getTaskService());
		Task task = ts.getTaskQueryService().getTaskInstanceById(taskId);
		if (task == null) {
			throw new IllegalStateException("There is no Task with the provided Id = " + taskId);
		}
		User user = ts.getTaskIdentityService().getUserById(userId);
		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<BeforeTaskCompletedEvent>() {
		}).fire(task);
		boolean operationAllowed = (task.getTaskData().getActualOwner() != null && task.getTaskData().getActualOwner().equals(user));
		if (!operationAllowed) {
			String errorMessage = "The user" + user + "is not allowed to Complete the task " + task.getId();
			throw new PermissionDeniedException(errorMessage);
		}
		if (task.getTaskData().getStatus().equals(Status.InProgress)) {
			// CHeck for potential Owner allowed (decorator?)
			((InternalTaskData) task.getTaskData()).setStatus(Status.Completed);
		}
		super.execute(cntxt);

		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<AfterTaskCompletedEvent>() {
		}).fire(task);

		return task.getId();
	}
}
