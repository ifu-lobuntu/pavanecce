package org.pavanecce.cmmn.jbpm.planning;

import org.kie.api.task.model.Task;


public interface PlannedTask extends Task{

	String getDiscretionaryItemId();


	PlanningStatus getPlanningStatus();


}