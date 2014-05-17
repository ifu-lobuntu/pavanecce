package org.pavanecce.cmmn.jbpm.instance.impl;

import java.util.Collection;

import org.pavanecce.cmmn.jbpm.instance.ControllablePlanItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceContainer;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceLifecycle;

public class PlanItemInstanceContainerUtil {
	public static boolean canComplete(PlanItemInstanceContainer container){
		Collection<? extends PlanItemInstanceLifecycle<?>> nodeInstances = container.getChildren();
		for (PlanItemInstanceLifecycle<?> nodeInstance : nodeInstances) {
			if (nodeInstance instanceof PlanItemInstanceFactoryNodeInstance && ((PlanItemInstanceFactoryNodeInstance<?>) nodeInstance).isPlanItemInstanceStillRequired()) {
				return false;
			} else if (nodeInstance instanceof MilestonePlanItemInstance && ((MilestonePlanItemInstance) nodeInstance).isCompletionStillRequired()) {
				return false;
			} else if (nodeInstance instanceof ControllablePlanItemInstanceLifecycle && ((ControllablePlanItemInstanceLifecycle<?>) nodeInstance).isCompletionStillRequired()) {
				return false;
			}

		}
		return true;

	}
}
