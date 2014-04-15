package org.pavanecce.cmmn.instance;

import java.util.Collection;

import org.drools.core.process.instance.WorkItem;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.pavanecce.cmmn.flow.PlanItem;
import org.pavanecce.cmmn.flow.Sentry;

public class PlanItemInstance extends WorkItemNodeInstance {
//	@Override
//	public void addEventListeners() {
//		PlanItem planItem=(PlanItem) getNode();
//		Collection<Sentry> values = planItem.getExitCriteria().values();
//		for (Sentry sentry : values) {
//	    	((WorkflowProcessInstance) getProcessInstance()).addEventListener("timerTriggered", this, false);
//		}
//		// skip workITem listeners because CMMN takes care of exit criteria
//		if (getTimerInstances() != null && getTimerInstances().size() > 0) {
//			addTimerListener();
//		}
//	}
	// @Override
	// public void signalEvent(String type, Object event) {
	// PlanItem planItem = (PlanItem) getNode();
	// if (planItem.getExitCriteria().isEmpty()) {
	// if ("workItemCompleted".equals(type)) {
	// workItemCompleted((WorkItem) event);
	// } else if ("workItemAborted".equals(type)) {
	// workItemAborted((WorkItem) event);
	// } else {
	// super.signalEvent(type, event);
	// }
	// }
	// }
}
