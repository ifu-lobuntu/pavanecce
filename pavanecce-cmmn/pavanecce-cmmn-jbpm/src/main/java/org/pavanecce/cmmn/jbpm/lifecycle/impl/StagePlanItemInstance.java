package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jbpm.process.instance.ContextInstanceContainer;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.node.EventBasedNodeInstanceInterface;
import org.jbpm.workflow.instance.node.EventNodeInstanceInterface;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.PlanItemContainer;
import org.pavanecce.cmmn.jbpm.flow.PlanningTable;
import org.pavanecce.cmmn.jbpm.flow.Stage;
import org.pavanecce.cmmn.jbpm.flow.TaskItemWithDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.ItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstanceContainerLifecycle;

public class StagePlanItemInstance extends AbstractControllableItemInstance<Stage, TaskItemWithDefinition<Stage>> implements PlanItemInstanceContainerLifecycle, NodeInstanceContainer,
		EventNodeInstanceInterface, EventBasedNodeInstanceInterface, ContextInstanceContainer {

	private static final long serialVersionUID = 112341234123L;

	@Override
	public void start() {
		super.start();
		triggerDefaultStart();
	}

	@Override
	public void manualStart() {
		super.manualStart();
		triggerDefaultStart();
	}

	private void triggerDefaultStart() {
		StartNode defaultStart = getItem().getDefinition().getDefaultStart();
		NodeInstance nodeInstance = getNodeInstance(defaultStart);
		((org.jbpm.workflow.instance.NodeInstance) nodeInstance).trigger(null, null);
		getCaseInstance().markSubscriptionsForUpdate();
	}

	@Override
	public Collection<ItemInstanceLifecycle<?>> getChildren() {
		Set<ItemInstanceLifecycle<?>> result = new HashSet<ItemInstanceLifecycle<?>>();
		for (NodeInstance nodeInstance : getNodeInstances()) {
			if (nodeInstance instanceof ItemInstanceLifecycle) {
				result.add((ItemInstanceLifecycle<?>) nodeInstance);
			}
		}
		return result;
	}

	@Override
	public boolean canComplete() {
		return PlanItemInstanceUtil.canComplete(this);
	}

	@Override
	public PlanItemContainer getPlanItemContainer() {
		return getPlanItemDefinition();
	}

	@Override
	public org.jbpm.workflow.instance.NodeInstance getFirstNodeInstance(long nodeId) {
		Collection<NodeInstance> nodeInstances = getNodeInstances();
		for (NodeInstance nodeInstance : nodeInstances) {
			if (nodeInstance.getNodeId() == nodeId) {// ignore level
				return (org.jbpm.workflow.instance.NodeInstance) nodeInstance;
			}
		}
		return null;
	}

	@Override
	public PlanningTable getPlanningTable() {
		return getPlanItemDefinition().getPlanningTable();
	}

	@Override
	protected String getIdealRoles() {
		return getBusinessAdministrators();
	}

}
