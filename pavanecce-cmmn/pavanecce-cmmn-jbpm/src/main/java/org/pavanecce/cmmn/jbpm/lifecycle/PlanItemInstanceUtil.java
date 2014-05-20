package org.pavanecce.cmmn.jbpm.lifecycle;

import org.drools.core.process.instance.WorkItemManager;
import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.pavanecce.cmmn.jbpm.flow.PlanItemControl;

public class PlanItemInstanceUtil {

	public static boolean isActivatedAutomatically(ControllableItemInstanceLifecycle<?> pi) {
		boolean isActivatedAutomatically = false;
		PlanItemControl itemControl = pi.getItemControl();
		if (itemControl != null && itemControl.getAutomaticActivationRule() instanceof ConstraintEvaluator) {
			ConstraintEvaluator ev = (ConstraintEvaluator) itemControl.getAutomaticActivationRule();
			if (ev.evaluate((org.jbpm.workflow.instance.NodeInstance) pi, null, ev)) {
				isActivatedAutomatically = true;
			}
		}
		return isActivatedAutomatically;
	}

	public static void exitPlanItem(ItemInstanceLifecycle<?> pi) {
		if (pi instanceof PlanElementLifecycleWithTask) {
			WorkItemManager workItemManager = (WorkItemManager) pi.getCaseInstance().getKnowledgeRuntime().getWorkItemManager();
			workItemManager.internalAbortWorkItem(((PlanElementLifecycleWithTask) pi).getWorkItemId());
		}
	}
}
