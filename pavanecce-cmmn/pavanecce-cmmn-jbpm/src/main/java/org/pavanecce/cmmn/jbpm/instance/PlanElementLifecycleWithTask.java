package org.pavanecce.cmmn.jbpm.instance;

import org.drools.core.process.instance.WorkItem;
import org.kie.api.task.model.Task;

public interface PlanElementLifecycleWithTask extends PlanElementLifecycle {
	String TRANSITION = "Transition";
	String TASK = "Task";
	String WORK_ITEM_UPDATED = "workItemUpdated";
	String TASK_NODE_NAME = "NodeName";
	String COMMENT = "Comment";
	String PARENT_WORK_ITEM_ID = "ParentId";

	WorkItem getWorkItem();

	Task getTask();

	long getWorkItemId();

	String getHumanTaskName();
}
