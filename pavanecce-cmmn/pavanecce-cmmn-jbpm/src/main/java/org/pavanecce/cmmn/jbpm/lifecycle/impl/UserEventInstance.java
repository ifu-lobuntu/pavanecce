package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import org.jbpm.workflow.instance.node.EventNodeInstanceInterface;
import org.pavanecce.cmmn.jbpm.event.PlanItemEvent;
import org.pavanecce.cmmn.jbpm.flow.UserEvent;
import org.pavanecce.cmmn.jbpm.flow.UserEventPlanItem;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;

public class UserEventInstance extends OccurrablePlanItemInstanceImpl<UserEvent, UserEventPlanItem> implements EventNodeInstanceInterface, Creatable {

	private static final long serialVersionUID = 3069593690659509023L;

	public UserEventInstance() {
		super.internalSetCompletionRequired(false);
		planElementState = PlanElementState.INITIAL;
	}

	public void signalEvent(String type, Object event) {
		super.signalEvent(type, event);
		String name = getPlanItem().getPlanInfo().getDefinition().getName();
		if (type.equals(name) && !(event instanceof PlanItemEvent) && canOccur()) {
			setPlanElementState(PlanElementState.AVAILABLE);
			occur();
		}
	}

	public void triggerCompleted() {
		((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).setCurrentLevel(getLevel());
		triggerCompleted(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE, false);
	}

	@Override
	public void ensureCreationIsTriggered() {
		if (super.getPlanElementState() == PlanElementState.INITIAL) {
			super.create();
		}
	}

}
