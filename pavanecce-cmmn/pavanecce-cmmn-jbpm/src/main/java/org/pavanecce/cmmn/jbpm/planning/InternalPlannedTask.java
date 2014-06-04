package org.pavanecce.cmmn.jbpm.planning;

import org.kie.internal.task.api.model.InternalTask;

public interface InternalPlannedTask extends PlannedTask, InternalTask {

	void setPlanningStatus(PlanningStatus planningStatus);

	void setDiscretionaryItemId(String tableItemId);

	public abstract void setPlanItemName(String planItemName);

}
