package org.pavanecce.cmmn.jbpm.planning;

import java.util.Map;

import org.kie.api.task.model.Task;

public interface PlannedTask extends Task {

	String getDiscretionaryItemId();

	PlanningStatus getPlanningStatus();

	public abstract String getPlanItemName();

	Map<String, Object> getParameterOverrides();

}