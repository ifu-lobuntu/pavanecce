package org.pavanecce.cmmn.jbpm.lifecycle;

import org.drools.core.process.instance.WorkItemManager;

public class PlanItemLifecyleUtil {


	@Deprecated()//Use the UpdateTaskStatus workitemhandler
	public static void exitPlanItem(ItemInstanceLifecycle<?> pi) {
		if (pi instanceof PlanElementLifecycleWithTask) {
			WorkItemManager workItemManager = (WorkItemManager) pi.getCaseInstance().getKnowledgeRuntime().getWorkItemManager();
			workItemManager.internalAbortWorkItem(((PlanElementLifecycleWithTask) pi).getWorkItemId());
		}
	}
}
