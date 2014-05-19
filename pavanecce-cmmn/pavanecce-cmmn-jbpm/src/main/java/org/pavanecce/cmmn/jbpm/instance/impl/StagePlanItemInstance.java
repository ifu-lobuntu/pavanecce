package org.pavanecce.cmmn.jbpm.instance.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.core.process.core.Work;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.core.Context;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.ContextInstanceContainer;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.impl.ContextInstanceFactory;
import org.jbpm.process.instance.impl.ContextInstanceFactoryRegistry;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.EventNode;
import org.jbpm.workflow.core.node.EventNodeInterface;
import org.jbpm.workflow.core.node.EventSubProcessNode;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.impl.NodeInstanceFactory;
import org.jbpm.workflow.instance.impl.NodeInstanceFactoryRegistry;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.node.EventBasedNodeInstanceInterface;
import org.jbpm.workflow.instance.node.EventNodeInstanceInterface;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.PlanItemContainer;
import org.pavanecce.cmmn.jbpm.flow.Stage;
import org.pavanecce.cmmn.jbpm.flow.StagePlanItem;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceContainerLifecycle;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceLifecycle;

public class StagePlanItemInstance extends AbstractControllablePlanInstance<Stage> implements PlanItemInstanceContainerLifecycle, NodeInstanceContainer, EventNodeInstanceInterface,
		EventBasedNodeInstanceInterface, ContextInstanceContainer {

	private static final long serialVersionUID = 112341234123L;

	private final List<NodeInstance> nodeInstances = new ArrayList<NodeInstance>();
	private long nodeInstanceCounter = 0;
	private int state = ProcessInstance.STATE_ACTIVE;
	private Map<String, ContextInstance> contextInstances = new HashMap<String, ContextInstance>();
	private Map<String, List<ContextInstance>> subContextInstances = new HashMap<String, List<ContextInstance>>();
	private int currentLevel;

	protected StagePlanItem getStagePlanItem() {
		return (StagePlanItem) getNode();
	}

	protected void createWorkItem() {
		workItem = new WorkItemImpl();
		Work work = getStagePlanItem().getWork();
		((WorkItem) workItem).setName(work.getName());
		((WorkItem) workItem).setProcessInstanceId(getProcessInstance().getId());
		((WorkItem) workItem).setParameters(new HashMap<String, Object>(work.getParameters()));
		((WorkItem) workItem).setParameter("planningTable", "");// TODO
	}

	@Override
	public Collection<PlanItemInstanceLifecycle<?>> getChildren() {
		Set<PlanItemInstanceLifecycle<?>> result = new HashSet<PlanItemInstanceLifecycle<?>>();
		for (NodeInstance nodeInstance : getNodeInstances()) {
			if (nodeInstance instanceof PlanItemInstanceLifecycle) {
				result.add((PlanItemInstanceLifecycle<?>) nodeInstance);
			}
		}
		return result;
	}

	@Override
	public boolean canComplete() {
		return PlanItemInstanceContainerUtil.canComplete(this);
	}

	@Override
	public PlanItemContainer getPlanItemContainer() {
		return getStagePlanItem().getStage();
	}

	@Override
	public int getLevelForNode(String uniqueID) {
		return 1;
	}

	public NodeContainer getNodeContainer() {
		return getStagePlanItem();
	}

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
		StartNode defaultStart = getStagePlanItem().getPlanInfo().getDefinition().getDefaultStart();
		NodeInstance nodeInstance = getNodeInstance(defaultStart);
		((org.jbpm.workflow.instance.NodeInstance) nodeInstance).trigger(null, null);
		getCaseInstance().markSubscriptionsForUpdate();
	}

	public void cancel() {
		while (!nodeInstances.isEmpty()) {
			NodeInstance nodeInstance = (NodeInstance) nodeInstances.get(0);
			((org.jbpm.workflow.instance.NodeInstance) nodeInstance).cancel();
		}
		super.cancel();
	}

	public void addNodeInstance(final org.jbpm.workflow.instance.NodeInstance nodeInstance) {
		((NodeInstanceImpl) nodeInstance).setId(nodeInstanceCounter++);
		this.nodeInstances.add((NodeInstanceImpl) nodeInstance);
	}

	public void removeNodeInstance(final org.jbpm.workflow.instance.NodeInstance nodeInstance) {
		this.nodeInstances.remove(nodeInstance);
	}

	public Collection<org.kie.api.runtime.process.NodeInstance> getNodeInstances() {
		return new ArrayList<org.kie.api.runtime.process.NodeInstance>(getNodeInstances(false));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<org.jbpm.workflow.instance.NodeInstance> getNodeInstances(boolean recursive) {
		Collection result = nodeInstances;
		if (recursive) {
			result = new ArrayList(result);
			for (Iterator<NodeInstance> iterator = nodeInstances.iterator(); iterator.hasNext();) {
				NodeInstance nodeInstance = iterator.next();
				if (nodeInstance instanceof NodeInstanceContainer) {
					result.addAll(((NodeInstanceContainer) nodeInstance).getNodeInstances(true));
				}
			}
		}
		return Collections.unmodifiableCollection(result);
	}

	public NodeInstance getNodeInstance(long nodeInstanceId) {
		for (NodeInstance nodeInstance : nodeInstances) {
			if (nodeInstance.getId() == nodeInstanceId) {
				return nodeInstance;
			}
		}
		return null;
	}

	public org.jbpm.workflow.instance.NodeInstance getFirstNodeInstance(final long nodeId) {
		// level logic not relevant.
		for (NodeInstance ni : this.getNodeInstances()) {
			if (ni.getNodeId() == nodeId) {
				return (org.jbpm.workflow.instance.NodeInstance) ni;
			}
		}
		return null;
	}


	public org.jbpm.workflow.instance.NodeInstance getNodeInstance(final Node node) {
		NodeInstanceFactory conf = NodeInstanceFactoryRegistry.getInstance(getProcessInstance().getKnowledgeRuntime().getEnvironment()).getProcessNodeInstanceFactory(node);
		if (conf == null) {
			throw new IllegalArgumentException("Illegal node type: " + node.getClass());
		}
		NodeInstanceImpl nodeInstance = (NodeInstanceImpl) conf.getNodeInstance(node, getProcessInstance(), this);
		if (nodeInstance == null) {
			throw new IllegalArgumentException("Illegal node type: " + node.getClass());
		}
		return nodeInstance;
	}

	public void signalEvent(String type, Object event) {
		super.signalEvent(type, event);
		for (Node node : getStagePlanItem().internalGetNodes()) {
			if (node instanceof EventNodeInterface) {
				if (((EventNodeInterface) node).acceptsEvent(type, event)) {
					if (node instanceof EventNode && ((EventNode) node).getFrom() == null) {
						EventNodeInstanceInterface eventNodeInstance = (EventNodeInstanceInterface) getNodeInstance(node);
						eventNodeInstance.signalEvent(type, event);
					} else if (node instanceof EventSubProcessNode) {
						EventNodeInstanceInterface eventNodeInstance = (EventNodeInstanceInterface) getNodeInstance(node);
						eventNodeInstance.signalEvent(type, event);
					} else {
						List<NodeInstance> nodeInstances = getNodeInstances(node.getId());
						if (nodeInstances != null && !nodeInstances.isEmpty()) {
							for (NodeInstance nodeInstance : nodeInstances) {
								((EventNodeInstanceInterface) nodeInstance).signalEvent(type, event);
							}
						}
					}
				}
			}
		}
	}

	public List<NodeInstance> getNodeInstances(final long nodeId) {
		List<NodeInstance> result = new ArrayList<NodeInstance>();
		for (final Iterator<NodeInstance> iterator = this.nodeInstances.iterator(); iterator.hasNext();) {
			final NodeInstance nodeInstance = iterator.next();
			if (nodeInstance.getNodeId() == nodeId) {
				result.add(nodeInstance);
			}
		}
		return result;
	}

	public void addEventListeners() {
		super.addEventListeners();
		for (NodeInstance nodeInstance : nodeInstances) {
			if (nodeInstance instanceof EventBasedNodeInstanceInterface) {
				((EventBasedNodeInstanceInterface) nodeInstance).addEventListeners();
			}
		}
	}

	public void removeEventListeners() {
		super.removeEventListeners();
		for (NodeInstance nodeInstance : nodeInstances) {
			if (nodeInstance instanceof EventBasedNodeInstanceInterface) {
				((EventBasedNodeInstanceInterface) nodeInstance).removeEventListeners();
			}
		}
	}

	public void nodeInstanceCompleted(org.jbpm.workflow.instance.NodeInstance nodeInstance, String outType) {
	}

	public void setState(final int state) {
		this.state = state;
		if (state == ProcessInstance.STATE_ABORTED) {
			cancel();
		}
	}

	public int getState() {
		return this.state;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	@Override
	protected boolean isWaitForCompletion() {
		return true;
	}

	public ContextContainer getContextContainer() {
		return getStagePlanItem();
	}

	public void setContextInstance(String contextId, ContextInstance contextInstance) {
		this.contextInstances.put(contextId, contextInstance);
	}

	public ContextInstance getContextInstance(String contextId) {
		ContextInstance contextInstance = this.contextInstances.get(contextId);
		if (contextInstance != null) {
			return contextInstance;
		}
		Context context = getContextContainer().getDefaultContext(contextId);
		if (context != null) {
			contextInstance = getContextInstance(context);
			return contextInstance;
		}
		return null;
	}

	public List<ContextInstance> getContextInstances(String contextId) {
		return this.subContextInstances.get(contextId);
	}

	public void addContextInstance(String contextId, ContextInstance contextInstance) {
		List<ContextInstance> list = this.subContextInstances.get(contextId);
		if (list == null) {
			list = new ArrayList<ContextInstance>();
			this.subContextInstances.put(contextId, list);
		}
		list.add(contextInstance);
	}

	public void removeContextInstance(String contextId, ContextInstance contextInstance) {
		List<ContextInstance> list = this.subContextInstances.get(contextId);
		if (list != null) {
			list.remove(contextInstance);
		}
	}

	public ContextInstance getContextInstance(String contextId, long id) {
		List<ContextInstance> contextInstances = subContextInstances.get(contextId);
		if (contextInstances != null) {
			for (ContextInstance contextInstance : contextInstances) {
				if (contextInstance.getContextId() == id) {
					return contextInstance;
				}
			}
		}
		return null;
	}

	public ContextInstance getContextInstance(final Context context) {
		ContextInstanceFactory conf = ContextInstanceFactoryRegistry.INSTANCE.getContextInstanceFactory(context);
		if (conf == null) {
			throw new IllegalArgumentException("Illegal context type (registry not found): " + context.getClass());
		}
		ContextInstance contextInstance = (ContextInstance) conf.getContextInstance(context, this, (ProcessInstance) getProcessInstance());
		if (contextInstance == null) {
			throw new IllegalArgumentException("Illegal context type (instance not found): " + context.getClass());
		}
		return contextInstance;
	}

}
