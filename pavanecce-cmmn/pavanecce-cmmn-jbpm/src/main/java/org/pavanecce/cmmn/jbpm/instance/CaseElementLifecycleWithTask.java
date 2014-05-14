package org.pavanecce.cmmn.jbpm.instance;

import org.drools.core.process.instance.WorkItem;
import org.kie.api.task.model.Task;

public interface CaseElementLifecycleWithTask extends CaseElementLifecycle {
	String TRANSITION = "Transition";
	String TASK = "Task";
	String WORK_ITEM_UPDATED = "workItemUpdated";

	WorkItem getWorkItem();

	Task getTask();

	long getWorkItemId();

	String getHumanTaskName();
}
