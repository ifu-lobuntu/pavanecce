package org.pavanecce.cmmn.jbpm.instance;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

	public Case getCase() {
		return (Case) getProcess();
	}
	@SuppressWarnings("unchecked")
	@Override
	public void start() {
		super.start();
		SubscriptionManager subscriptionManager = (SubscriptionManager) getKnowledgeRuntime().getEnvironment().get(SubscriptionManager.ENV_NAME);
		if (subscriptionManager != null) {
			ObjectPersistence persistence = subscriptionManager.getObjectPersistence(this);
			Map<CaseFileItem, Collection<Object>> parentSubscriptions = new HashMap<CaseFileItem, Collection<Object>>();
			Collection<Object> subscriptions = new HashSet<Object>();
			for (CaseParameter caseParameter : getCase().getInputParameters()) {
				if (caseParameter.getBindingRefinementEvaluator() == null) {
					Object var = getVariable(caseParameter.getVariable().getName());
					if (var != null) {
						subscriptions.add(var);
					}
				} else {
					ProcessContext ctx = new ProcessContext(getKnowledgeRuntime());
					ctx.setProcessInstance(this);
					try {
						Object subscribeTo = caseParameter.getBindingRefinementEvaluator().evaluate(ctx);
						if ((subscribeTo instanceof Collection && ((Collection<?>) subscribeTo).isEmpty()) || subscribeTo == null) {
							//Nothing to subscribe to - subscribe to parent for CREATE and DELETE events
							Object parentToSubscribeTo = caseParameter.getBindingRefinementParentEvaluator().evaluate(ctx);
							if (parentToSubscribeTo != null) {
								Collection<Object> collection = parentSubscriptions.get(caseParameter.getVariable());
								if (collection == null) {
									parentSubscriptions.put(caseParameter.getVariable(), collection = new HashSet<Object>());
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
			subscriptionManager.subscribe(this, subscriptions, parentSubscriptions, persistence);
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
		return new HashSet<OnPartInstanceSubscription>( onCaseFileItemParts.values());

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
					if (onPart.getSourceCaseFileItem().getElementId().equals(parameter.getVariable().getElementId())) {
						OnPartInstanceSubscription subscription = target.get(onPartInstance);
						if (subscription == null) {
							target.put(onPartInstance, new OnPartInstanceSubscription(getCase().getCaseKey(), getId(), onPart, parameter));
						}else{
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
