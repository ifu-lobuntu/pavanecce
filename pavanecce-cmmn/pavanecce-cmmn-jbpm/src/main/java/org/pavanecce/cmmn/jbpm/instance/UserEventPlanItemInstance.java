package org.pavanecce.cmmn.jbpm.instance;

import org.jbpm.workflow.instance.node.EventNodeInstance;
import org.jbpm.workflow.instance.node.StateNodeInstance;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.flow.UserEventPlanItem;

public class UserEventPlanItemInstance extends EventNodeInstance {

	private static final long serialVersionUID = 3069593690659509023L;

	public void signalEvent(String type, Object event) {
		super.signalEvent(type, event);
		String name = getPlanItem().getPlanInfo().getDefinition().getName();
		if (type.equals(name) && !(event instanceof PlanItemEvent)) {
			getProcessInstance().signalEvent(OnPart.getType(getPlanItem().getName(), PlanItemTransition.OCCUR), new PlanItemEvent(getPlanItem().getName(), PlanItemTransition.OCCUR, event));
		}
	}

	public void triggerCompleted() {
		((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).setCurrentLevel(getLevel());
		triggerCompleted(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE, false);

	}

	public UserEventPlanItem getPlanItem() {
		return (UserEventPlanItem) getNode();
	}
}
