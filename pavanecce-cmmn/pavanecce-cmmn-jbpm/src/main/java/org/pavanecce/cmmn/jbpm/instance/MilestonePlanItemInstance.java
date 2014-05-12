package org.pavanecce.cmmn.jbpm.instance;

import org.jbpm.workflow.instance.node.StateNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.event.PlanItemEvent;
import org.pavanecce.cmmn.jbpm.flow.MilestonePlanItem;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;

public class MilestonePlanItemInstance extends StateNodeInstance {

	private static final long serialVersionUID = 3069593690659509023L;
	@Override
	public void internalTrigger(NodeInstance from, String type) {
		super.internalTrigger(from, type);
		String eventToTrigger = OnPart.getType(getPlanItem().getName(), PlanItemTransition.OCCUR);
		PlanItemEvent event = new PlanItemEvent(getPlanItem().getName(), PlanItemTransition.OCCUR, new Object());
		getProcessInstance().signalEvent(eventToTrigger, event);
	}
	protected MilestonePlanItem getPlanItem() {
		return (MilestonePlanItem)getNode();
	}



}
