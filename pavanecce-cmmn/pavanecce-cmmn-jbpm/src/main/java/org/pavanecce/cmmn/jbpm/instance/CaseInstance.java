package org.pavanecce.cmmn.jbpm.instance;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.core.WorkItemHandlerNotFoundException;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.drools.core.spi.ProcessContext;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.task.model.Task;
import org.pavanecce.cmmn.jbpm.event.SubscriptionManager;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;
import org.pavanecce.cmmn.jbpm.infra.OnPartInstanceSubscription;
import org.pavanecce.common.ObjectPersistence;

public class CaseInstance extends RuleFlowProcessInstance implements PlanItemInstanceContainer {
	private static final long serialVersionUID = 8715128915363796623L;
	private boolean shouldUpdateSubscriptions;
	private transient int signalCount = 0;
	private PlanItemState planItemState = PlanItemState.ACTIVE;
	private PlanItemState lastBusyState = PlanItemState.NONE;
	private long workItemId;
	transient private WorkItem workItem;

	public Case getCase() {
		return (Case) getProcess();
	}

	public void markSubscriptionsForUpdate() {
		this.shouldUpdateSubscriptions = true;
	}

	@Override
	public void signalEvent(String type, Object event) {
		signalCount++;
		if (type.equals("workItemUpdated") && isMyWorkItem((WorkItem) event)) {
			this.workItem = (WorkItem) event;
			PlanItemTransition transition = (PlanItemTransition) workItem.getResult(HumanControlledPlanItemInstance.TRANSITION);
			transition.invokeOn(this);
		} else {
			super.signalEvent(type, event);
		}
		signalCount--;
		if (shouldUpdateSubscriptions && signalCount == 0) {
			updateSubscriptions();
		}
	}

	protected boolean isMyWorkItem(WorkItem event) {
		return event.getId() == getWorkItemId() || (getWorkItemId() == -1 && getWorkItem().getId() == (event.getId()));
	}

	protected WorkItem createWorkItem() {
		workItem = new WorkItemImpl();
		((WorkItem) workItem).setName("Human Task");
		((WorkItem) workItem).setProcessInstanceId(getId());
		((WorkItem) workItem).setParameters(new HashMap<String, Object>());
		((WorkItem) workItem).setParameter("planningTable", "");// TODO
		((WorkItem) workItem).setParameter("nodeName", getCase().getName());// TODO
		return workItem;
	}

	@Override
	public void start() {
		super.start();
		updateSubscriptions();
		createWorkItem();
		String deploymentId = (String) getKnowledgeRuntime().getEnvironment().get("deploymentId");
		((WorkItem) workItem).setDeploymentId(deploymentId);
		try {
			((WorkItemManager) getKnowledgeRuntime().getWorkItemManager()).internalExecuteWorkItem((org.drools.core.process.instance.WorkItem) workItem);
		} catch (WorkItemHandlerNotFoundException wihnfe) {
			setState(ProcessInstance.STATE_ABORTED);
			throw wihnfe;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.workItemId = workItem.getId();
	}

	@Override
	public String[] getEventTypes() {
		return super.getEventTypes();
	}

	protected void updateSubscriptions() {
		SubscriptionManager subscriptionManager = (SubscriptionManager) getKnowledgeRuntime().getEnvironment().get(SubscriptionManager.ENV_NAME);
		if (subscriptionManager != null) {
			CaseInstance caseInstance = this;
			ObjectPersistence persistence = subscriptionManager.getObjectPersistence(caseInstance);
			Map<CaseFileItem, Collection<Object>> parentSubscriptions = new HashMap<CaseFileItem, Collection<Object>>();
			Collection<Object> subscriptions = new HashSet<Object>();
			populateSubscriptionsActivatedByParameters(parentSubscriptions, subscriptions, getCase().getInputParameters());
			populateSubscriptionsActivatedByParameters(caseInstance, parentSubscriptions, subscriptions);
			subscriptionManager.updateSubscriptions(caseInstance, subscriptions, parentSubscriptions, persistence);
			subscriptionManager.flush(persistence);
		}
		shouldUpdateSubscriptions = false;

	}

	protected void populateSubscriptionsActivatedByParameters(NodeInstanceContainer caseInstance, Map<CaseFileItem, Collection<Object>> parentSubscriptions, Collection<Object> subscriptions) {
		Collection<NodeInstance> nodeInstances = caseInstance.getNodeInstances();
		for (NodeInstance ni : nodeInstances) {
			if (ni.getNode() instanceof PlanItem) {
				PlanItem pi = (PlanItem) ni.getNode();
				if (pi.getPlanInfo().getDefinition() instanceof TaskDefinition && ni instanceof PlanItemInstance && isSubscribing((PlanItemInstance) ni)) {
					TaskDefinition td = (TaskDefinition) pi.getPlanInfo().getDefinition();
					populateSubscriptionsActivatedByParameters(parentSubscriptions, subscriptions, td.getOutputs());
				}
			}
			if (ni instanceof StagePlanItemInstance) {
				populateSubscriptionsActivatedByParameters((NodeInstanceContainer) ni, parentSubscriptions, subscriptions);
			}
		}
	}

	protected boolean isSubscribing(PlanItemInstance ni) {
		return ni.getPlanItemState()==PlanItemState.ACTIVE || ni.getPlanItemState()==PlanItemState.ENABLED;
	}

	@SuppressWarnings("unchecked")
	protected void populateSubscriptionsActivatedByParameters(Map<CaseFileItem, Collection<Object>> parentSubscriptions, Collection<Object> subscriptions, List<CaseParameter> subscribingParameters) {
		for (CaseParameter caseParameter : subscribingParameters) {
			if (caseParameter.getBindingRefinementEvaluator() == null) {
				Object var = getVariable(caseParameter.getBoundVariable().getName());
				if (var != null) {
					subscriptions.add(var);
				}
			} else {
				ProcessContext ctx = new ProcessContext(getKnowledgeRuntime());
				ctx.setProcessInstance(this);
				try {
					Object subscribeTo = caseParameter.getBindingRefinementEvaluator().evaluate(ctx);
					if ((subscribeTo instanceof Collection && ((Collection<?>) subscribeTo).isEmpty()) || subscribeTo == null) {
						// Nothing to subscribe to - subscribe to parent for CREATE and DELETE events
						Object parentToSubscribeTo = caseParameter.getBindingRefinementParentEvaluator().evaluate(ctx);
						if (parentToSubscribeTo != null) {
							Collection<Object> collection = parentSubscriptions.get(caseParameter.getBoundVariable());
							if (collection == null) {
								parentSubscriptions.put(caseParameter.getBoundVariable(), collection = new HashSet<Object>());
							}
							if (parentToSubscribeTo instanceof Collection) {
								collection.addAll((Collection<? extends Object>) parentToSubscribeTo);
							} else {
								collection.add(parentToSubscribeTo);
							}
						}
					} else if (subscribeTo instanceof Collection) {
						subscriptions.addAll((Collection<?>) subscribeTo);
					} else if (subscribeTo != null) {
						subscriptions.add(subscribeTo);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public Set<OnPartInstanceSubscription> findOnPartInstanceSubscriptions() {
		Set<CaseParameter> params = new HashSet<CaseParameter>();
		params.addAll(getCase().getInputParameters());
		addSubscribingCaseParameters(params, this);
		Collection<NodeInstance> nodes = getNodeInstances();
		Map<OnPartInstance, OnPartInstanceSubscription> onCaseFileItemParts = new HashMap<OnPartInstance, OnPartInstanceSubscription>();
		for (CaseParameter item : params) {
			findCaseFileItemOnPartsFor(item, nodes, onCaseFileItemParts);
		}
		return new HashSet<OnPartInstanceSubscription>(onCaseFileItemParts.values());

	}

	private void addSubscribingCaseParameters(Set<CaseParameter> params, NodeInstanceContainer caseInstance) {
		for (NodeInstance ni : caseInstance.getNodeInstances()) {
			if (ni.getNode() instanceof PlanItem) {
				PlanItem pi = (PlanItem) ni.getNode();
				PlanItemDefinition def = pi.getPlanInfo().getDefinition();
				if (def instanceof TaskDefinition) {
					params.addAll(((TaskDefinition) def).getOutputs());
				}
			} else if (ni instanceof StagePlanItemInstance) {
				addSubscribingCaseParameters(params, (NodeInstanceContainer) ni);
			}
		}
	}

	private void findCaseFileItemOnPartsFor(CaseParameter parameter, Collection<NodeInstance> nodes, Map<OnPartInstance, OnPartInstanceSubscription> target) {
		for (NodeInstance node : nodes) {
			if (node instanceof OnPartInstance) {
				OnPartInstance onPartInstance = (OnPartInstance) node;
				if (onPartInstance.getOnPart() instanceof CaseFileItemOnPart) {
					CaseFileItemOnPart onPart = (CaseFileItemOnPart) onPartInstance.getOnPart();
					if (onPart.getSourceCaseFileItem().getElementId().equals(parameter.getBoundVariable().getElementId())) {
						OnPartInstanceSubscription subscription = target.get(onPartInstance);
						if (subscription == null) {
							target.put(onPartInstance, new OnPartInstanceSubscription(getCase().getCaseKey(), getId(), onPart, parameter));
						} else {
							subscription.addParameter(parameter);
						}
					}
				}
			} else if (node instanceof StagePlanItemInstance) {
				findCaseFileItemOnPartsFor(parameter, ((StagePlanItemInstance) node).getNodeInstances(), target);
			}
		}
	}

	@Override
	public PlanItem getPlanItem() {
		return null;
	}

	@Override
	public void setPlanItemState(PlanItemState s) {
		this.planItemState = s;
	}

	@Override
	public void setLastBusyState(PlanItemState s) {
		this.lastBusyState = s;
	}

	@Override
	public void enable() {
		planItemState.enable(this);
	}

	@Override
	public void disable() {
		planItemState.disable(this);
	}

	@Override
	public void reenable() {
		planItemState.reenable(this);
	}

	@Override
	public void manualStart() {
		planItemState.manualStart(this);
	}

	@Override
	public void reactivate() {
		planItemState.reactivate(this);
	}

	@Override
	public void suspend() {
		planItemState.suspend(this);
	}

	@Override
	public void resume() {
		planItemState.resume(this);
	}

	@Override
	public void terminate() {
		planItemState.terminate(this);
	}

	@Override
	public void exit() {
		planItemState.exit(this);
	}

	@Override
	public void complete() {
		planItemState.complete(this);
	}

	@Override
	public void parentSuspend() {
		planItemState.parentSuspend(this);
	}

	@Override
	public void parentResume() {
		planItemState.parentResume(this);
	}

	@Override
	public void parentTerminate() {
		planItemState.parentTerminate(this);
	}

	@Override
	public void create() {
		planItemState.create(this);
	}

	@Override
	public void fault() {
		planItemState.fault(this);
	}

	@Override
	public void occur() {
		planItemState.occur(this);
	}

	@Override
	public void close() {
		planItemState.close(this);
	}

	@Override
	public PlanItemState getLastBusyState() {
		return lastBusyState;
	}

	@Override
	public PlanItemState getPlanItemState() {
		return planItemState;
	}

	@Override
	public Collection<PlanItemInstance> getChildren() {
		Set<PlanItemInstance> result = new HashSet<PlanItemInstance>();
		for (NodeInstance nodeInstance : getNodeInstances()) {
			if (nodeInstance instanceof PlanItemInstance) {
				result.add((PlanItemInstance) nodeInstance);
			}
		}
		return result;
	}

	@Override
	public CaseInstance getCaseInstance() {
		return this;
	}

	public WorkItem getWorkItem() {
		if (workItem == null && workItemId >= 0) {
			workItem = ((WorkItemManager) getKnowledgeRuntime().getWorkItemManager()).getWorkItem(workItemId);
		}
		return workItem;
	}

	@Override
	public Task getTask() {
		if (getWorkItem() != null) {
			return (Task) getWorkItem().getResult(HumanControlledPlanItemInstance.TASK);
		}
		return null;
	}

	@Override
	public long getWorkItemId() {
		return workItemId;
	}
	@Override
	public String getPlanItemName() {
		return getCase().getName();
	}
}
