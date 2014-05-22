package org.pavanecce.cmmn.jbpm.task;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.drools.core.process.instance.WorkItemHandler;
import org.jbpm.services.task.commands.ActivateTaskCommand;
import org.jbpm.services.task.commands.ClaimTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.FailTaskCommand;
import org.jbpm.services.task.commands.SuspendTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.User;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.InternalTaskService;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementLifecycleWithTask;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;

public class UpdateTaskStatusWorkItemHandler implements WorkItemHandler {
	private RuntimeManager runtimeManager;

	public RuntimeManager getRuntimeManager() {
		return runtimeManager;
	}

	public void setRuntimeManager(RuntimeManager runtimeManager) {
		this.runtimeManager = runtimeManager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void executeWorkItem(final WorkItem workItem, WorkItemManager manager) {
		final Long workItemId = (Long) workItem.getParameter(PlanElementLifecycleWithTask.WORK_ITEM_ID);
		final PlanItemTransition transition = (PlanItemTransition) workItem.getParameter(PlanElementLifecycleWithTask.TASK_TRANSITION);
		RuntimeEngine runtime = getRuntimeManager().getRuntimeEngine(ProcessInstanceIdContext.get(workItem.getProcessInstanceId()));
		InternalTaskService its = (InternalTaskService) runtime.getTaskService();
		Task task = its.getTaskByWorkItemId(workItemId);
		long taskId = task.getId();
		String currentUserId = null;
		if (task.getTaskData().getActualOwner() != null) {
			currentUserId = task.getTaskData().getActualOwner().getId();
		} else {
			currentUserId = (String) workItem.getParameter(PeopleAssignmentHelper.ACTOR_ID);
		}
		TaskCommand<?> cmd = null;
		String gidString = (String) workItem.getParameter(PeopleAssignmentHelper.GROUP_ID);
		gidString = gidString == null ? "" : gidString;
		String[] groupIds = gidString.split(System.getProperty("org.jbpm.ht.user.separator", ","));
		switch (transition) {
		case START:
			cmd = new AutomaticallyStartTaskCommand(taskId, (Collection<String>) workItem.getParameter(PeopleAssignmentHelper.ACTOR_ID), workItem.getParameters());
			break;
		case EXIT:
			cmd = new ExitCriteriaTaskCommand(taskId);
			break;
		case FAULT:
			cmd = new FailTaskCommand(taskId, currentUserId, new HashMap<String, Object>());
			break;
		case SUSPEND:
			cmd = new SuspendTaskCommand(taskId, currentUserId);
			break;
		case COMPLETE:
			cmd = new CompleteTaskCommand(taskId, currentUserId, workItem.getParameters());
			break;
		case ENABLE:
			if (currentUserId != null) {
				cmd = new ClaimTaskCommand(taskId, currentUserId);
			} else if(task.getTaskData().getStatus()!=Status.Ready){
				User user = findExactUser(runtime, gidString);
				String userId = user == null ? "Administrator" : user.getId();
				cmd = new ActivateTaskCommand(taskId, userId);
			}
			break;
		default:
		}
		if (cmd != null) {
			cmd.setGroupsIds(Arrays.asList(groupIds));
			its.execute(cmd);
		}
		PlanElementState state = StatusConverter.convertStatus(its.getTaskById(taskId).getTaskData().getStatus());
		if (state.isTerminalState()) {
			manager.abortWorkItem(workItemId);
		}
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// Nothing to abort
	}

	private User findExactUser(RuntimeEngine runtime, String entity) {
		InternalTaskService its = (InternalTaskService) runtime.getTaskService();
		User user = its.getUserById(entity);
		if (user == null) {
			// Not ideal, but let's see if there is a only one user implied
			String[] split = entity.split(System.getProperty("org.jbpm.ht.user.separator", ","));
			for (String groupName : split) {
				Group group = its.getGroupById(groupName);
				if (group != null) {
					Iterator<OrganizationalEntity> m = its.getUserInfo().getMembersForGroup(group);
					if (m.hasNext()) {
						while (m.hasNext()) {
							OrganizationalEntity oe = m.next();
							if (oe instanceof User) {
								if (user != null) {
									// More than one user
									return null;
								} else {
									user = (User) oe;
								}
							}
						}
					}
				}
			}
		}
		return user;
	}
}
