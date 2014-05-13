package org.pavanecce.cmmn.jbpm.instance;

import org.drools.core.process.instance.WorkItemManager;
import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemControl;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInfo;

public class PlanItemInstanceUtil {

	public static void fulfillRequirementRule(PlanItem planItem, NodeInstance humanTaskPlanItemInstance) {
		for (NodeInstance nodeInstance : humanTaskPlanItemInstance.getNodeInstanceContainer().getNodeInstances()) {
			if (nodeInstance instanceof SentryInstance) {
				SentryInstance si = (SentryInstance) nodeInstance;
				if (si.getSentry().getPlanItemEntering() != null && planItem.getId() == si.getSentry().getId()) {
					si.setPlanItemInstanceRequired(false);
				}
			}
		}
	}
	public static boolean isActivatedAutomatically(PlanItemInstance pi) {
		boolean isActivatedAutomatically=false;
		PlanItemInfo planInfo = pi.getPlanItem().getPlanInfo();
		PlanItemControl itemControl = planInfo.getItemControl();
		if (itemControl != null && itemControl.getAutomaticActivationRule() instanceof ConstraintEvaluator) {
			ConstraintEvaluator ev = (ConstraintEvaluator) itemControl.getAutomaticActivationRule();
			if (ev.evaluate( (org.jbpm.workflow.instance.NodeInstance) pi, null, ev)) {
				isActivatedAutomatically=true;
			}
		}
		return isActivatedAutomatically;
	}


	public static void exitPlanItem(PlanItemInstance pi) {
		if(pi instanceof HumanControlledPlanItemInstance){
            WorkItemManager workItemManager = (WorkItemManager) pi.getCaseInstance()
                    .getKnowledgeRuntime().getWorkItemManager();
			workItemManager.internalAbortWorkItem(((HumanControlledPlanItemInstance) pi).getWorkItemId());
		}
	}
}
