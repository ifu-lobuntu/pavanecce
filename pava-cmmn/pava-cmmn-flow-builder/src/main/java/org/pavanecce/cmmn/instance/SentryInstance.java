package org.pavanecce.cmmn.instance;

import java.util.Set;

import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.WorkItemManager;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.node.JoinInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.flow.JoiningSentry;
import org.pavanecce.cmmn.flow.PlanItem;
import org.pavanecce.cmmn.flow.Sentry;

public class SentryInstance extends JoinInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4302504131617050844L;

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		if (getNode() instanceof JoiningSentry) {
			super.internalTrigger(from, type);
		} else {
			triggerCompleted();
		}
	}

	@Override
	public void triggerCompleted() {
		NodeInstanceContainer nic = (org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer();
		nic.setCurrentLevel(getLevel());
		maybeTriggerExit(nic);
		// Default behavior is to keep this SentryInstance active so that it can
		// continue to listen to transitions/standardEvents
		triggerCompleted(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE, false);
	}

	private boolean maybeTriggerExit(NodeInstanceContainer nic) {
		boolean hasTriggered = false;
		Sentry sentry = (Sentry) getNode();
		Set<PlanItem> planItemsExiting = sentry.getPlanItemsExiting();
		CaseInstance ci = (CaseInstance) getProcessInstance();
		for (PlanItem planItem : planItemsExiting) {
			NodeInstance found = findNodeInstance(nic, planItem);
			//TODO refine which PlannItemInstance to exit, e.g. look at the output and see if the caseFileITem Instance associated matches 
			if (found instanceof WorkItemNodeInstance) {
				// Task planItem
				WorkItemNodeInstance wini = (WorkItemNodeInstance) found;
				WorkItem workItem = wini.getWorkItem();
				workItem.setState(WorkItem.COMPLETED);
				wini.signalEvent("workItemCompleted", workItem);
                ((WorkItemManager) ((ProcessInstance) getProcessInstance())
                        .getKnowledgeRuntime().getWorkItemManager()).internalAbortWorkItem(workItem.getId());
				hasTriggered = true;
			} else {
				// TODO: SubProcessInstance? Exception?
			}
		}
		return hasTriggered;
	}

	protected NodeInstance findNodeInstance(NodeInstanceContainer nic, PlanItem planItem) {
		NodeInstance found=null;
		for (NodeInstance nodeInstance : nic.getNodeInstances()) {
			if(nodeInstance.getNodeId()==planItem.getId()){
				found=nodeInstance;
			}
		}
		return found;
	}

}
