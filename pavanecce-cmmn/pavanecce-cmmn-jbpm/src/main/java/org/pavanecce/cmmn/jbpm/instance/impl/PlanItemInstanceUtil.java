package org.pavanecce.cmmn.jbpm.instance.impl;

import org.drools.core.process.instance.WorkItemManager;
import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.pavanecce.cmmn.jbpm.flow.PlanItemControl;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInfo;
import org.pavanecce.cmmn.jbpm.instance.CaseElementLifecycleWithTask;
import org.pavanecce.cmmn.jbpm.instance.ControllablePlanItemInstanceLifecycle;

public class PlanItemInstanceUtil {

	public static boolean isActivatedAutomatically(ControllablePlanItemInstanceLifecycle<?> pi) {
		boolean isActivatedAutomatically = false;
		PlanItemInfo<?> planInfo = pi.getPlanItem().getPlanInfo();
		PlanItemControl itemControl = planInfo.getItemControl();
		if (itemControl != null && itemControl.getAutomaticActivationRule() instanceof ConstraintEvaluator) {
			ConstraintEvaluator ev = (ConstraintEvaluator) itemControl.getAutomaticActivationRule();
			if (ev.evaluate((org.jbpm.workflow.instance.NodeInstance) pi, null, ev)) {
				isActivatedAutomatically = true;
			}
		}
		return isActivatedAutomatically;
	}

	public static void exitPlanItem(ControllablePlanItemInstanceLifecycle<?> pi) {
		if (pi instanceof CaseElementLifecycleWithTask) {
			WorkItemManager workItemManager = (WorkItemManager) pi.getCaseInstance().getKnowledgeRuntime().getWorkItemManager();
			workItemManager.internalAbortWorkItem(((CaseElementLifecycleWithTask) pi).getWorkItemId());
		}
	}
}
