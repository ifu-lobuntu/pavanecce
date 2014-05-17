package org.pavanecce.cmmn.jbpm.instance.impl;

import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.Milestone;

public class MilestonePlanItemInstance extends AbstractOccurrablePlanItemInstance<Milestone> {
	private static final long serialVersionUID = 3069593690659509023L;
	public MilestonePlanItemInstance(){
	}
	@Override
	public void internalTrigger(NodeInstance from, String type) {
		occur();
		triggerCompleted(false);
	}
}
