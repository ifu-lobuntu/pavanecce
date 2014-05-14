package org.pavanecce.cmmn.jbpm.instance.impl;

import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.workflow.instance.node.TimerNodeInstance;
import org.pavanecce.cmmn.jbpm.event.PlanItemEvent;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.flow.TimerEventPlanItem;

public class TimerEventPlanItemInstance extends TimerNodeInstance {

	private static final long serialVersionUID = 3069593690659509023L;

	public void signalEvent(String type, Object event) {
		if ("timerTriggered".equals(type)) {
			TimerInstance timer = (TimerInstance) event;
			if (timer.getId() == getTimerId()) {
				triggerCompleted(false);
			}
			getProcessInstance().signalEvent(OnPart.getType(getPlanItem().getName(), PlanItemTransition.OCCUR), new PlanItemEvent(getPlanItem().getName(), PlanItemTransition.OCCUR, event));
		}
	}

	public void triggerCompleted() {
		((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).setCurrentLevel(getLevel());
		triggerCompleted(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE, false);

	}

	public TimerEventPlanItem getPlanItem() {
		return (TimerEventPlanItem) getNode();
	}

	@Override
	public void triggerCompleted(boolean remove) {
		super.triggerCompleted(false);
	}

}
