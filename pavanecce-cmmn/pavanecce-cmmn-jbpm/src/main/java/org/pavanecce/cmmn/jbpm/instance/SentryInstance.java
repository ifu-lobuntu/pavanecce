package org.pavanecce.cmmn.jbpm.instance;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.WorkItemManager;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.node.JoinInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.definition.process.Connection;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.event.CaseEvent;
import org.pavanecce.cmmn.jbpm.flow.JoiningSentry;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.Sentry;

public class SentryInstance extends JoinInstance {
	private static ThreadLocal<Deque<Collection<CaseEvent>>> currentEvents = new ThreadLocal<Deque<Collection<CaseEvent>>>();

	public static Collection<CaseEvent> getCurrentEvents() {
		return getEventQueue().peek();
	}

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

	protected Collection<CaseEvent> getEvents() {
		Collection<CaseEvent> result = new HashSet<CaseEvent>();
		Collection<Connection> values = getNode().getIncomingConnections(Node.CONNECTION_DEFAULT_TYPE);
		for (Connection connection : values) {
			if (connection.getFrom() instanceof OnPart) {
				NodeInstance ni = findNodeInstance((NodeInstanceContainer) getNodeInstanceContainer(), (OnPart) connection.getFrom());
				OnPartInstance opi = (OnPartInstance) ni;
				result.add(opi.getCaseEvent());
			}
		}
		return result;
	}

	@Override
	public void triggerCompleted() {
		Sentry sentry=(Sentry) getNode();
		Constraint c = sentry.getCondition();
		if(c instanceof ConstraintEvaluator ){
			Connection conn = getNode().getIncomingConnections(Node.CONNECTION_DEFAULT_TYPE).get(0);
			if(!((ConstraintEvaluator) c).evaluate(this, conn, c)){
				return;
			}
		}
		NodeInstanceContainer nic = (org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer();
		nic.setCurrentLevel(getLevel());
		maybeTriggerExit(nic);
		// Default behavior is to keep this SentryInstance active so that it can
		// continue to listen to transitions/standardEvents
		Deque<Collection<CaseEvent>> deque = getEventQueue();
		deque.push(getEvents());
		try {
			triggerCompleted(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE, false);
		} finally {
			deque.pop();
		}
		Collection<Connection> values = getNode().getIncomingConnections(Node.CONNECTION_DEFAULT_TYPE);
		for (Connection connection : values) {
			if (connection.getFrom() instanceof OnPart) {
				NodeInstance ni = findNodeInstance((NodeInstanceContainer) getNodeInstanceContainer(), (OnPart) connection.getFrom());
				OnPartInstance opi = (OnPartInstance) ni;
				opi.popEvent();
			}
		}

	}

	protected static Deque<Collection<CaseEvent>> getEventQueue() {
		Deque<Collection<CaseEvent>> deque = currentEvents.get();
		if (deque == null) {
			currentEvents.set(deque = new ArrayDeque<Collection<CaseEvent>>());
		}
		return deque;
	}

	private boolean maybeTriggerExit(NodeInstanceContainer nic) {
		boolean hasTriggered = false;
		Sentry sentry = (Sentry) getNode();
		Set<PlanItem> planItemsExiting = sentry.getPlanItemsExiting();
		for (PlanItem planItem : planItemsExiting) {
			NodeInstance found = findNodeInstance(nic, planItem);
			// TODO refine which PlannItemInstance to exit, e.g. look at the
			// output and see if the caseFileITem Instance associated matches
			if (found instanceof WorkItemNodeInstance) {
				// Task planItem
				WorkItemNodeInstance wini = (WorkItemNodeInstance) found;
				WorkItem workItem = wini.getWorkItem();
				workItem.setState(org.kie.api.runtime.process.WorkItem.COMPLETED);
				wini.signalEvent("workItemCompleted", workItem);
				((WorkItemManager) ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getWorkItemManager()).internalAbortWorkItem(workItem.getId());
				hasTriggered = true;
			} else {
				// TODO: SubProcessInstance? Exception?
			}
		}
		return hasTriggered;
	}

	protected NodeInstance findNodeInstance(NodeInstanceContainer nic, Node planItem) {
		NodeInstance found = null;
		for (NodeInstance nodeInstance : nic.getNodeInstances()) {
			if (nodeInstance.getNodeId() == planItem.getId()) {
				found = nodeInstance;
			}
		}
		return found;
	}

}
