package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.HashSet;
import java.util.Set;

import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.node.SplitInstance;
import org.kie.api.definition.process.Connection;
import org.kie.api.runtime.process.NodeInstance;

public class DefaultSplitInstance extends SplitInstance {

	private static final long serialVersionUID = 124124123411L;

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		// super.internalTrigger(from, type);
		Set<NodeInstance> toNodeInstances = new HashSet<NodeInstance>();
		for (Connection connection : getNode().getOutgoingConnections(NodeImpl.CONNECTION_DEFAULT_TYPE)) {
			toNodeInstances.add(((NodeInstanceContainer) getNodeInstanceContainer()).getNodeInstance(connection.getTo()));
		}
		DefaultJoinInstance defaultJoinInstance = null;
		for (NodeInstance nodeInstance : toNodeInstances) {
			if (nodeInstance instanceof DefaultJoinInstance) {
				defaultJoinInstance = ((DefaultJoinInstance) nodeInstance);
			}
		}
		defaultJoinInstance.setInitializing(true);
		for (NodeInstance nodeInstance : toNodeInstances) {
			if (isSentry(nodeInstance)) {
				triggerNodeInstance((org.jbpm.workflow.instance.NodeInstance) nodeInstance, NodeImpl.CONNECTION_DEFAULT_TYPE);
			}
		}
		for (NodeInstance nodeInstance : toNodeInstances) {
			if (!isSentry(nodeInstance) && nodeInstance != defaultJoinInstance) {
				if (nodeInstance instanceof Creatable) {
					((Creatable) nodeInstance).ensureCreationIsTriggered();
				}
				triggerNodeInstance((org.jbpm.workflow.instance.NodeInstance) nodeInstance, NodeImpl.CONNECTION_DEFAULT_TYPE);
			}
		}
		defaultJoinInstance.setInitializing(false);
		triggerNodeInstance(defaultJoinInstance, NodeImpl.CONNECTION_DEFAULT_TYPE);
		((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).removeNodeInstance(this);
	}

	public boolean isSentry(NodeInstance nodeInstance) {
		return nodeInstance instanceof SentryInstance;
	}
}
