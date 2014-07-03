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
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstanceContainer;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.command.Context;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.model.InternalTaskData;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanningTableContainerInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.HumanTaskInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.StageInstance;

public class SubmitPlanCommand extends AbstractPlanningCommand<Void> {
	private static final long serialVersionUID = 7907971723514784829L;
	private final Collection<PlannedTask> plannedTasks;
	private final long parentTaskId;
	private RuntimeManager runtimeManager;
	private boolean resume;

	public SubmitPlanCommand(RuntimeManager runtimeManager, JbpmServicesPersistenceManager pm, Collection<PlannedTask> plannedTasks, long parentTaskId,
			boolean resume) {
		super(pm);
		this.plannedTasks = plannedTasks;
		this.parentTaskId = parentTaskId;
		this.runtimeManager = runtimeManager;
		this.resume = resume;
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
			Task currentTask = ts.getTaskById(plannedTask.getId());
			InternalTaskData td = (InternalTaskData) currentTask.getTaskData();
			td.setActualOwner(plannedTask.getTaskData().getActualOwner());
			if (!currentTask.getPeopleAssignments().getPotentialOwners().contains(td.getActualOwner())) {
				currentTask.getPeopleAssignments().getPotentialOwners().add(td.getActualOwner());
			}
			WorkItemManager wim = (WorkItemManager) runtime.getKieSession().getWorkItemManager();
			WorkItem wi = wim.getWorkItem(plannedTask.getTaskData().getWorkItemId());
			Environment env = runtime.getKieSession().getEnvironment();
			PersistenceContext pc = ((PersistenceContextManager) env.get(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER)).getCommandScopedPersistenceContext();
			WorkItemInfo wii = pc.findWorkItemInfo(wi.getId());
			InternalRuleBase irb = (InternalRuleBase) ((KnowledgeBaseImpl) runtime.getKieSession().getKieBase()).getRuleBase();
			wii.getWorkItem(env, irb).getParameters().putAll(plannedTask.getParameterOverrides());
			wii.setId(wi.getId());
			pc.merge(wii);
			ci.ensurePlanItemCreated(workItemId, plannedTask.getDiscretionaryItemId(), wi);
			if (td.getStatus() == Status.Created) {
				// // td.setStatus(Status.Ready);
			}
		}
		if (resume) {
			PlanningTableContainerInstance p = ci.findPlanningTableContainerInstance(workItemId);
			NodeInstanceContainer nic = null;
			if (p instanceof HumanTaskInstance) {
				nic = ((HumanTaskInstance) p).getNodeInstanceContainer();
			} else {
				nic = (NodeInstanceContainer) p;
			}
			if (nic instanceof CaseInstance) {
				((CaseInstance) nic).reactivate();
			} else {
				((StageInstance) nic).resume();
			}
		}
		return null;
	}
}