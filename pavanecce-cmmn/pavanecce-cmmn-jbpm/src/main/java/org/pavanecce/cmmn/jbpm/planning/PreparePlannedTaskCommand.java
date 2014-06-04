package org.pavanecce.cmmn.jbpm.planning;

import org.drools.core.process.instance.WorkItem;
import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.command.Context;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public class PreparePlannedTaskCommand extends AbstractPlanningCommand<PlannedTask> {
	private final String discretionaryItemId;
	private final long parentTaskId;
	private RuntimeManager runtimeManager;

	private static final long serialVersionUID = -8445378L;

	public PreparePlannedTaskCommand(RuntimeManager rm, JbpmServicesPersistenceManager pm, String discretionaryItemId, long parentTaskId) {
		super(pm);
		this.discretionaryItemId = discretionaryItemId;
		this.parentTaskId = parentTaskId;
		this.runtimeManager = rm;
	}

	@Override
	public PlannedTask execute(Context context) {
		ts = ((TaskContext) context).getTaskService();
		long processInstanceId = ts.getTaskById(parentTaskId).getTaskData().getProcessInstanceId();
		RuntimeEngine runtime = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
		CaseInstance ci = (CaseInstance) runtime.getKieSession().getProcessInstance(processInstanceId);
		WorkItem wi = ci.createPlannedItem(getWorkItemId(parentTaskId), discretionaryItemId);
		init(ts);
		return pm.find(PlannedTaskImpl.class, ts.getTaskByWorkItemId(wi.getId()).getId());
	}
}