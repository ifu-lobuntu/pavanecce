package org.pavanecce.cmmn.jbpm.planning;

import org.kie.api.task.model.TaskSummary;

public interface PlannedTaskSummary extends TaskSummary {
	public String getDiscretionaryItemId();

	String getPlanItemName();

	public PlanningStatus getPlanningStatus();
}
