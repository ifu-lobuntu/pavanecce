package org.pavanecce.cmmn.jbpm.task;

import org.drools.core.process.instance.WorkItemHandler;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.model.Status;
import org.kie.internal.command.Context;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.InternalTaskData;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementLifecycleWithTask;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateTaskStatusWorkItemHandler implements WorkItemHandler {
	private static final Logger logger = LoggerFactory.getLogger(UpdateTaskStatusWorkItemHandler.class);
	private RuntimeManager runtimeManager;

	public RuntimeManager getRuntimeManager() {
		return runtimeManager;
	}

	public void setRuntimeManager(RuntimeManager runtimeManager) {
		this.runtimeManager = runtimeManager;
	}

	@SuppressWarnings("serial")
	@Override
	public void executeWorkItem(final WorkItem workItem, WorkItemManager manager) {
		final Long workItemId = (Long) workItem.getParameter(PlanElementLifecycleWithTask.WORK_ITEM_ID);
		final PlanElementState stat = (PlanElementState) workItem.getParameter(PlanElementLifecycleWithTask.TASK_STATUS);
		RuntimeEngine runtime = getRuntimeManager().getRuntimeEngine(ProcessInstanceIdContext.get(workItem.getProcessInstanceId()));
		((InternalTaskService) runtime.getTaskService()).execute(new TaskCommand<Void>() {
			@Override
			public Void execute(Context context) {
				TaskContext tc = (TaskContext) context;
				InternalTask t = (InternalTask) tc.getTaskService().getTaskByWorkItemId(workItemId);
				String actor = (String) workItem.getParameter(PeopleAssignmentHelper.ACTOR_ID);
				if (actor != null) {
					((InternalTaskData) t.getTaskData()).setActualOwner(tc.getTaskService().getUserById(actor));
				}
				((InternalTaskData) t.getTaskData()).setStatus(convertState(stat));
				return null;
			}

			private Status convertState(PlanElementState stat) {
				switch (stat) {
				case ACTIVE:
					return Status.InProgress;
				case AVAILABLE:
					return Status.Ready;
				case CLOSED:
					return Status.Completed;
				case COMPLETED:
					return Status.Completed;
				case DISABLED:
					return Status.Obsolete;
				case ENABLED:
					return Status.Ready;
				case FAILED:
					return Status.Failed;
				case INITIAL:
					return Status.Ready;
				case NONE:
					return Status.Ready;
				case SUSPENDED:
					return Status.Suspended;
				case TERMINATED:
					return Status.Exited;
				}
				return Status.Ready;
			}
		});
		if (stat.isTerminalState()) {
			manager.abortWorkItem(workItemId);
		}
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

}
