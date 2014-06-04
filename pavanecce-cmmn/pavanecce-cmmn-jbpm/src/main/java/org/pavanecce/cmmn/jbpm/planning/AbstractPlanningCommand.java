package org.pavanecce.cmmn.jbpm.planning;

import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.pavanecce.cmmn.jbpm.task.AbstractTaskCommand;

public abstract class AbstractPlanningCommand<T> extends AbstractTaskCommand<T> {

	private static final long serialVersionUID = -4217142163951701835L;

	public AbstractPlanningCommand(JbpmServicesPersistenceManager pm) {
		super(pm);
	}

	protected long getWorkItemId(long parentTaskId) {
		return ts.getTaskById(parentTaskId).getTaskData().getWorkItemId();
	}
}