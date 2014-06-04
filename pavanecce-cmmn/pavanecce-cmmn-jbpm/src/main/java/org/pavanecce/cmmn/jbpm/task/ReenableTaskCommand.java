package org.pavanecce.cmmn.jbpm.task;

import java.util.List;

import javax.enterprise.util.AnnotationLiteral;

import org.jbpm.services.task.commands.CommandsUtil;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.jbpm.services.task.impl.TaskServiceEntryPointImpl;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.User;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.model.InternalTaskData;

public class ReenableTaskCommand extends TaskCommand<Void> {
	private static final long serialVersionUID = -8257771889718694139L;

	public ReenableTaskCommand(long taskId, String userId) {
		super.taskId = taskId;
		super.userId = userId;
	}

	@SuppressWarnings("serial")
	public Void execute(Context cntxt) {
		TaskServiceEntryPointImpl ts = ((TaskContext) cntxt).getTaskService();
		Task task = ts.getTaskInstanceById(taskId);
		InternalTaskData td = (InternalTaskData) task.getTaskData();
		if (task.getTaskData().getStatus() != Status.Obsolete) {
			String errorMessage = "Only tasks in the Obselete/Disabled status can be reenabled. Task" + task.getId() + " is " + task.getTaskData().getStatus();
			throw new PermissionDeniedException(errorMessage);
		}
		User user = ts.getTaskIdentityService().getUserById(userId);
		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<BeforeTaskReenabledEvent>() {
		}).fire(task);
		boolean adminAllowed = CommandsUtil.isAllowed(user, getGroupsIds(), (List<OrganizationalEntity>) task.getPeopleAssignments()
				.getBusinessAdministrators());
		boolean ownerAllowed = (task.getTaskData().getActualOwner() != null && task.getTaskData().getActualOwner().equals(user));
		if (!adminAllowed && !ownerAllowed) {
			String errorMessage = "The user" + user + "is not allowed to Reenable the task " + task.getId();
			throw new PermissionDeniedException(errorMessage);
		}
		if (td.getActualOwner() == null) {
			td.setStatus(Status.Ready);
		} else {
			td.setStatus(Status.Reserved);
		}
		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<AfterTaskReenabledEvent>() {
		}).fire(task);

		return null;
	}

	public String toString() {
		return "taskService.reenable(" + taskId + ", " + userId + ");";
	}

}
