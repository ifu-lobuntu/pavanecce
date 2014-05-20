package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Collection;

import org.pavanecce.cmmn.jbpm.lifecycle.ControllableItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.ItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstanceContainerLifecycle;

public class PlanItemInstanceContainerUtil {
	public static boolean canComplete(PlanItemInstanceContainerLifecycle container){
		Collection<? extends ItemInstanceLifecycle<?>> nodeInstances = container.getChildren();
		for (ItemInstanceLifecycle<?> nodeInstance : nodeInstances) {
			if (nodeInstance instanceof PlanItemInstanceFactoryNodeInstance && ((PlanItemInstanceFactoryNodeInstance<?>) nodeInstance).isPlanItemInstanceStillRequired()) {
				return false;
			} else if (nodeInstance instanceof MilestonePlanItemInstance && ((MilestonePlanItemInstance) nodeInstance).isCompletionStillRequired()) {
				return false;
			} else if (nodeInstance instanceof ControllableItemInstanceLifecycle && ((ControllableItemInstanceLifecycle<?>) nodeInstance).isCompletionStillRequired()) {
				return false;
			}

		}
		return true;

	}
}
