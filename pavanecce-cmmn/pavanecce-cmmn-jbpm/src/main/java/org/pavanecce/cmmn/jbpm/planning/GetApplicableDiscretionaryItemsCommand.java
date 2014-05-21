package org.pavanecce.cmmn.jbpm.planning;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.impl.TaskServiceEntryPointImpl;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.TaskData;
import org.kie.internal.command.Context;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public class GetApplicableDiscretionaryItemsCommand extends TaskCommand<Collection<ApplicableDiscretionaryItem>> {
	private final long parentTaskId;
	private final String user;
	private RuntimeManager runtimeManager;
	private static final long serialVersionUID = -8445370954335088878L;

	public GetApplicableDiscretionaryItemsCommand(RuntimeManager rm, long parentTaskId, String user) {
		this.parentTaskId = parentTaskId;
		this.user = user;
		this.runtimeManager=rm;
	}

	@Override
	public Collection<ApplicableDiscretionaryItem> execute(Context context) {
		TaskServiceEntryPointImpl ts = ((TaskContext) context).getTaskService();
		TaskData td = ts.getTaskById(parentTaskId).getTaskData();
		RuntimeEngine runtime = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(td.getProcessInstanceId()));
		CaseInstance ci = (CaseInstance) runtime.getKieSession().getProcessInstance(td.getProcessInstanceId());
		Map<String, String> a = ci.getApplicableDiscretionaryItems(td.getWorkItemId(), user);
		Set<Entry<String, String>> entrySet = a.entrySet();
		Collection<ApplicableDiscretionaryItem> result = new HashSet<ApplicableDiscretionaryItem>();
		for (Entry<String, String> entry : entrySet) {
			result.add(new ApplicableDiscretionaryItem(entry.getKey(), entry.getValue()));
		}
		return result;
	}
}