package org.pavanecce.cmmn.jbpm.instance;

import org.drools.core.process.instance.WorkItem;
import org.kie.api.task.model.Task;

public interface HumanControlledPlanItemInstance extends PlanItemInstance{
	String TRANSITION = "Transition";
	String TASK = "Task";
	WorkItem getWorkItem();
	Task getTask();
	long getWorkItemId();
}
