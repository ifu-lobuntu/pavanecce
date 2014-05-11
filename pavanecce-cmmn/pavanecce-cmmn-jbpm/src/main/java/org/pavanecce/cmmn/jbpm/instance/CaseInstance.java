package org.pavanecce.cmmn.jbpm.instance;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.core.spi.ProcessContext;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;
import org.pavanecce.common.ObjectPersistence;

public class CaseInstance extends RuleFlowProcessInstance {
	private static final long serialVersionUID = 8715128915363796623L;
	private boolean shouldUpdateSubscriptions;
	private transient int signalCount=0;
	public Case getCase() {
		return (Case) getProcess();
	}

	public void markSubscriptionsForUpdate() {
		this.shouldUpdateSubscriptions = true;
	}

	@Override
	public void signalEvent(String type, Object event) {
		signalCount++;
		super.signalEvent(type, event);
		signalCount--;
		if (shouldUpdateSubscriptions && signalCount==0) {
			updateSubscriptions();
		}
	}

	@Override
	public void start() {
		super.start();
		updateSubscriptions();
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
		for (NodeInstance nodeInstance : nodeInstances) {
			if (nodeInstance.getNode() instanceof PlanItem) {
				PlanItem pi = (PlanItem) nodeInstance.getNode();
				if (pi.getPlanInfo().getDefinition() instanceof TaskDefinition) {
					TaskDefinition td = (TaskDefinition) pi.getPlanInfo().getDefinition();
					populateSubscriptionsActivatedByParameters(parentSubscriptions, subscriptions, td.getOutputs());
				}
			}
			if(nodeInstance instanceof StagePlanItemInstance){
				populateSubscriptionsActivatedByParameters((NodeInstanceContainer) nodeInstance, parentSubscriptions, subscriptions);
			}
		}
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

}
