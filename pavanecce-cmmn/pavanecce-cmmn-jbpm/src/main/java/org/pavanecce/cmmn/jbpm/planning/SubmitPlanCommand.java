package org.pavanecce.cmmn.jbpm.planning;

import java.util.Collection;

import org.drools.core.common.InternalRuleBase;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.WorkItemManager;
import org.drools.persistence.PersistenceContext;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.info.WorkItemInfo;
import org.jbpm.services.task.commands.TaskContext;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.Task;
import org.kie.internal.command.Context;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.model.InternalTaskData;
import org.pavanecce.cmmn.jbpm.lifecycle.ControllableItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementLifecycleWithTask;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public class SubmitPlanCommand extends AbstractPlanningCommand<Void> {
	private final Collection<PlannedTask> plannedTasks;
	private final long parentTaskId;
	private RuntimeManager runtimeManager;

	public SubmitPlanCommand(RuntimeManager runtimeManager, Collection<PlannedTask> plannedTasks, long parentTaskId) {
		this.plannedTasks = plannedTasks;
		this.parentTaskId = parentTaskId;
		this.runtimeManager=runtimeManager;
	}

	@Override
	public Void execute(Context context) {
		init(((TaskContext) context).getTaskService());
		Task parentTask = ts.getTaskById(parentTaskId);
		long workItemId = parentTask.getTaskData().getWorkItemId();
		RuntimeEngine runtime = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(parentTask.getTaskData().getProcessInstanceId()));
		CaseInstance ci = (CaseInstance) runtime.getKieSession().getProcessInstance(parentTask.getTaskData().getProcessInstanceId());
		for (PlannedTask plannedTask : plannedTasks) {
			pm.merge(plannedTask);
			ts.addContent(plannedTask.getId(), plannedTask.getParameterOverrides());
			((InternalTaskData) ts.getTaskById(plannedTask.getId()).getTaskData()).setActualOwner(plannedTask.getTaskData().getActualOwner());
			WorkItemManager wim = (WorkItemManager) runtime.getKieSession().getWorkItemManager();
			WorkItem wi = wim.getWorkItem(plannedTask.getTaskData().getWorkItemId());
			Environment env = runtime.getKieSession().getEnvironment();
			PersistenceContext pc= ((PersistenceContextManager) env.get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getCommandScopedPersistenceContext();
			WorkItemInfo wii = pc.findWorkItemInfo(wi.getId());
			InternalRuleBase irb=(InternalRuleBase) ((KnowledgeBaseImpl) runtime.getKieSession().getKieBase()).getRuleBase();
			wii.getWorkItem(env, irb).getParameters().putAll(ts.getTaskContent(plannedTask.getId()));
			wii.setId(wi.getId());
			pc.merge(wii);
			ControllableItemInstanceLifecycle<?> pi = ci.ensurePlanItemCreated(workItemId, plannedTask.getDiscretionaryItemId(), wi);
		}
		return null;
	}
}