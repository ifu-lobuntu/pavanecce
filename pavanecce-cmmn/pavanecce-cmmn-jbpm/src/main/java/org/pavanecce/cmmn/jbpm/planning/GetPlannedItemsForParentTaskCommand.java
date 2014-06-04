package org.pavanecce.cmmn.jbpm.planning;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.impl.TaskServiceEntryPointImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.command.Context;

public class GetPlannedItemsForParentTaskCommand extends AbstractPlanningCommand<Collection<PlannedTaskSummary>> {
	private final long parentTaskId;
	private final boolean createMissing;
	private static final long serialVersionUID = -8445370954335088878L;

	public GetPlannedItemsForParentTaskCommand(JbpmServicesPersistenceManager pm, long parentTaskId, boolean createMissing) {
		super(pm);
		this.parentTaskId = parentTaskId;
		this.createMissing = createMissing;
	}

	@Override
	public Collection<PlannedTaskSummary> execute(Context context) {
		Collection<PlannedTaskSummary> result = new HashSet<PlannedTaskSummary>();
		TaskServiceEntryPointImpl ts = ((TaskContext) context).getTaskService();
		List<TaskSummary> sts = ts.getTaskQueryService().getSubTasksByParent(parentTaskId);
		init(ts);
		for (TaskSummary taskSummary : sts) {
			PlannedTaskImpl pt = pm.find(PlannedTaskImpl.class, taskSummary.getId());
			if (pt == null) {
				pt = new PlannedTaskImpl(pm.find(TaskImpl.class, taskSummary.getId()));
				if (createMissing) {
					pm.persist(pt);
				}
			}
			result.add(new PlannedTaskSummaryImpl(pt));
		}
		return result;
	}
}