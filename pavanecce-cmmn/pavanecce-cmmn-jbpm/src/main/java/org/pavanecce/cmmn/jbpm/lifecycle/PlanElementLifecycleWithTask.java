package org.pavanecce.cmmn.jbpm.lifecycle;

import org.drools.core.process.instance.WorkItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;

public interface PlanElementLifecycleWithTask extends PlanElementLifecycle {
	WorkItem getWorkItem();

	void reactivate();

	void complete();

	void triggerTransitionOnTask(PlanItemTransition transition);

	void fault();

	Object getTask();

	long getWorkItemId();

}
