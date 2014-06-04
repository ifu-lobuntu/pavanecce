package org.pavanecce.cmmn.jbpm.planning;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.impl.TaskServiceEntryPointImpl;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.TaskData;
import org.kie.internal.command.Context;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.pavanecce.cmmn.jbpm.ApplicableDiscretionaryItem;
import org.pavanecce.cmmn.jbpm.flow.Role;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public class GetApplicableDiscretionaryItemsCommand extends TaskCommand<Collection<ApplicableDiscretionaryItem>> {
	private final long parentTaskId;
	private RuntimeManager runtimeManager;
	private boolean suspend;
	private static final long serialVersionUID = -8445370954335088878L;

	public GetApplicableDiscretionaryItemsCommand(RuntimeManager rm, long parentTaskId, String user, boolean suspend) {
		this.parentTaskId = parentTaskId;
		this.userId = user;
		this.runtimeManager = rm;
		this.suspend = suspend;
	}

	@Override
	public Collection<ApplicableDiscretionaryItem> execute(Context context) {
		TaskServiceEntryPointImpl ts = ((TaskContext) context).getTaskService();
		TaskData td = ts.getTaskById(parentTaskId).getTaskData();
		RuntimeEngine runtime = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(td.getProcessInstanceId()));
		CaseInstance ci = (CaseInstance) runtime.getKieSession().getProcessInstance(td.getProcessInstanceId());
		if (suspend) {
			ts.suspend(parentTaskId, userId);
		}
		Collection<Role> roles = ci.getCase().getRoles();
		Set<String> usersRoles = new HashSet<String>();
		for (Role role : roles) {
			Collection<String> roleAssignments = ci.getRoleAssignments(role.getName());
			if (roleAssignments.contains(userId)) {
				usersRoles.add(role.getName());
			}
		}
		// TODO optionally lookup from some RoleService
		return ci.getApplicableDiscretionaryItems(td.getWorkItemId(), usersRoles);
	}
}