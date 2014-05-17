package org.pavanecce.cmmn.jbpm.instance.impl;

import org.jbpm.workflow.core.impl.NodeImpl;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.Milestone;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;

public class MilestonePlanItemInstance extends AbstractOccurrablePlanItemInstance<Milestone> {
	private static final long serialVersionUID = 3069593690659509023L;

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		if (getPlanElementState() == PlanElementState.AVAILABLE || (getPlanElementState() == PlanElementState.COMPLETED && canRepeat())) {
			super.internalTrigger(from, type);
			triggerCompleted(NodeImpl.CONNECTION_DEFAULT_TYPE, false);
		}
	}
}
