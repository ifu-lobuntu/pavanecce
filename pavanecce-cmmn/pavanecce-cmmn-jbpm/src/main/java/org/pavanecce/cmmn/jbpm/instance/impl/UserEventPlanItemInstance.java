package org.pavanecce.cmmn.jbpm.instance.impl;

import org.jbpm.workflow.instance.node.EventNodeInstanceInterface;
import org.pavanecce.cmmn.jbpm.event.PlanItemEvent;
import org.pavanecce.cmmn.jbpm.flow.UserEventListener;

public class UserEventPlanItemInstance extends AbstractOccurrablePlanItemInstance<UserEventListener> implements EventNodeInstanceInterface{

	private static final long serialVersionUID = 3069593690659509023L;

	public void signalEvent(String type, Object event) {
		super.signalEvent(type, event);
		String name = getPlanItem().getPlanInfo().getDefinition().getName();
		if (type.equals(name) && !(event instanceof PlanItemEvent)) {
			occur();
		}
	}
	public void triggerCompleted() {
		((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).setCurrentLevel(getLevel());
		triggerCompleted(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE, false);

	}

}
