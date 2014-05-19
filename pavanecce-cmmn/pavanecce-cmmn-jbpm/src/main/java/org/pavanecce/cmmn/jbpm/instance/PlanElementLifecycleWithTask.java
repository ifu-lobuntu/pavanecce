package org.pavanecce.cmmn.jbpm.instance;

import org.drools.core.process.instance.WorkItem;

public interface PlanElementLifecycleWithTask extends PlanElementLifecycle {
	String TRANSITION = "Transition";
	String TASK = "Task";
	String TASK_STATUS = "TaskStatus";
	String WORK_ITEM_ID = "WorkItemId";
	String WORK_ITEM_UPDATED = "workItemUpdated";
	String TASK_NODE_NAME = "NodeName";
	String COMMENT = "Comment";
	String PARENT_WORK_ITEM_ID = "ParentId";
	String UPDATE_TASK_STATUS = "UpdateTaskStatusHandler";
	

	WorkItem getWorkItem();

	void reactivate();

	void complete();
	
	void internalComplete();

	void fault();

	Object getTask();

	long getWorkItemId();

	String getHumanTaskName();
}
