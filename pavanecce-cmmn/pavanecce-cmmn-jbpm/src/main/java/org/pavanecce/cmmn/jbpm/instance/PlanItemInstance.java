package org.pavanecce.cmmn.jbpm.instance;

import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.runtime.process.NodeInstance;

public class PlanItemInstance extends WorkItemNodeInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3200294767777991641L;

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		// TODO Auto-generated method stub
		super.internalTrigger(from, type);
	}
	// @Override
	// public void addEventListeners() {
	// PlanItem planItem=(PlanItem) getNode();
	// Collection<Sentry> values = planItem.getExitCriteria().values();
	// for (Sentry sentry : values) {
	// ((WorkflowProcessInstance)
	// getProcessInstance()).addEventListener("timerTriggered", this, false);
	// }
	// // skip workITem listeners because CMMN takes care of exit criteria
	// if (getTimerInstances() != null && getTimerInstances().size() > 0) {
	// addTimerListener();
	// }
	// }
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
