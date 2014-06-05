package org.pavanecce.cmmn.jbpm.planning;

import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.command.Context;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanningTableContainerInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public class MakeDiscretionaryItemAvailableCommand extends AbstractPlanningCommand<Void> {
	private final String discretionaryItemId;
	private final long parentTaskId;
	private RuntimeManager runtimeManager;

	private static final long serialVersionUID = -8445378L;

	public MakeDiscretionaryItemAvailableCommand(RuntimeManager rm, JbpmServicesPersistenceManager pm, String discretionaryItemId, long parentTaskId) {
		super(pm);
		this.discretionaryItemId = discretionaryItemId;
		this.parentTaskId = parentTaskId;
		this.runtimeManager = rm;
	}

	@Override
	public Void execute(Context context) {
		ts = ((TaskContext) context).getTaskService();
		long processInstanceId = ts.getTaskById(parentTaskId).getTaskData().getProcessInstanceId();
		RuntimeEngine runtime = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
		CaseInstance ci = (CaseInstance) runtime.getKieSession().getProcessInstance(processInstanceId);
		PlanningTableContainerInstance ptci = ci.findPlanningTableContainerInstance(ts.getTaskById(parentTaskId).getTaskData().getWorkItemId());
		ptci.makeDiscretionaryItemAvailable(discretionaryItemId);
		return null;
	}
}