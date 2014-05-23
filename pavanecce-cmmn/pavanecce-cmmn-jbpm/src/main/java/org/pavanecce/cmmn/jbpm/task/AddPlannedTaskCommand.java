package org.pavanecce.cmmn.jbpm.task;

import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.model.ContentData;
import org.kie.internal.task.api.model.InternalTaskData;
import org.pavanecce.cmmn.jbpm.planning.PlannedTask;
import org.pavanecce.cmmn.jbpm.planning.PlannedTaskImpl;
import org.pavanecce.cmmn.jbpm.planning.PlanningStatus;

public class AddPlannedTaskCommand extends AbstractTaskCommand<PlannedTask> {
	private static final long serialVersionUID = 2919984132940815456L;
	private final ContentData content;
	private final Task task;
	private String discretionaryItemId;
	
	public AddPlannedTaskCommand(JbpmServicesPersistenceManager pm, ContentData content, Task task, String discretionaryItemId) {
		super(pm);
		this.content = content;
		this.task = task;
		this.discretionaryItemId=discretionaryItemId;
	}
	
	@Override
	public PlannedTask execute(Context context) {
		TaskContext tc = (TaskContext) context;
		init(tc.getTaskService());
		((InternalTaskData) task.getTaskData()).setStatus(Status.Created);
		tc.getTaskService().getTaskInstanceService().addTask(task, content);
		PlannedTaskImpl pt = new PlannedTaskImpl((TaskImpl) task);
		pt.setDiscretionaryItemId(discretionaryItemId);
		pt.setPlanningStatus(PlanningStatus.PLANNING_IN_PROGRESS);
		pm.persist(pt);
		return pt;
	}
}