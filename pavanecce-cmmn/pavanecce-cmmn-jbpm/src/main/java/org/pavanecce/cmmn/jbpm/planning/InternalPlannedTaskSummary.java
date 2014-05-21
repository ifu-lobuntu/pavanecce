package org.pavanecce.cmmn.jbpm.planning;

import org.kie.internal.task.api.model.InternalTaskSummary;

public interface InternalPlannedTaskSummary extends InternalTaskSummary, PlannedTaskSummary {
	public void setDiscretionaryItemId(String tableItemId);

	void setPlanItemName(String name);

	public void setPlanningStatus(PlanningStatus planningStatus);

}
