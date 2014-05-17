package org.pavanecce.cmmn.jbpm.instance.impl;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;

import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.node.JoinInstance;
import org.kie.api.definition.process.Connection;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.event.CaseEvent;
import org.pavanecce.cmmn.jbpm.flow.MilestonePlanItem;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInstanceFactoryNode;
import org.pavanecce.cmmn.jbpm.flow.Sentry;
import org.pavanecce.cmmn.jbpm.instance.ControllablePlanItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.instance.OnPartInstance;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceLifecycle;

public class SentryInstance extends JoinInstance {
	private static ThreadLocal<Deque<Collection<CaseEvent>>> currentEvents = new ThreadLocal<Deque<Collection<CaseEvent>>>();

	public static Collection<CaseEvent> getCurrentEvents() {
		return getEventQueue().peek();
	}

	private static final long serialVersionUID = -4302504131617050844L;

	public Sentry getSentry() {
		return (Sentry) getNode();
	}

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		List<Connection> outgoingConnections = getNode().getOutgoingConnections(NodeImpl.CONNECTION_DEFAULT_TYPE);
		if (outgoingConnections.isEmpty()) {
//			System.out.println(getNodeName());
		} else {
			Connection next = outgoingConnections.get(0);
			System.out.println(next.getTo().getName() + ":" + next.getTo().getClass().getName());
			if (next.getTo() instanceof MilestonePlanItem) {
				org.jbpm.workflow.instance.NodeInstance ni = ((NodeInstanceContainer) getNodeInstanceContainer()).getNodeInstance(next.getTo());
				((MilestonePlanItemInstance) ni).calcIsRequired();
			} else if (next.getTo() instanceof PlanItemInstanceFactoryNode) {
				org.jbpm.workflow.instance.NodeInstance ni = ((NodeInstanceContainer) getNodeInstanceContainer()).getNodeInstance(next.getTo());
				((PlanItemInstanceFactoryNodeInstance) ni).calcIsRequired();
			}else{
				throw new IllegalStateException("Unknown to-node:"+next.getTo().getName() + ":" + next.getTo().getClass().getName() );
				
			}
		}
		super.internalTrigger(from, type);
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
		Sentry sentry = (Sentry) getNode();
		Constraint c = sentry.getCondition();
		if (c instanceof ConstraintEvaluator) {
			Connection conn = getNode().getIncomingConnections(Node.CONNECTION_DEFAULT_TYPE).get(0);
			if (!((ConstraintEvaluator) c).evaluate(this, conn, c)) {
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
				if (opi != null) {
					// coud be after process completion
					opi.popEvent();
				}
			}
		}
		for (Connection connection : values) {
			if (!(connection.getFrom() instanceof OnPart)) {
				// Once activated, we keep the originating "from" active to indicate an "Available" state
				super.getTriggers().put(connection.getFrom().getId(), 1);
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
		if (sentry.getPlanItemExiting() != null) {
			NodeInstance found = findNodeInstance(nic, sentry.getPlanItemExiting());
			// TODO refine which PlannItemInstance to exit, e.g. look at the
			// output and see if the caseFileITem Instance associated matches
			if (found instanceof ControllablePlanItemInstanceLifecycle) {
				// Task planItem
				ControllablePlanItemInstanceLifecycle<?> pii = (ControllablePlanItemInstanceLifecycle<?>) found;
				pii.exit();
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
