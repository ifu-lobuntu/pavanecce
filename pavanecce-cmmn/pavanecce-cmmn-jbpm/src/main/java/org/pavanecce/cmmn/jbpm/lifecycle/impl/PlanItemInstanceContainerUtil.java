package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;
import org.pavanecce.cmmn.jbpm.infra.OnPartInstanceSubscription;
import org.pavanecce.cmmn.jbpm.lifecycle.ControllableItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstanceContainer;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanningTableContainer;

/**
 * Defines all the logic common to CaseInstances and StagePlanItemInstances. This is required as it is not possible to
 * give them a common superclass
 */
public class PlanItemInstanceContainerUtil {

	public static boolean canComplete(PlanItemInstanceContainer container) {
		if (container.getPlanElementState() != PlanElementState.ACTIVE) {
			return false;
		}
		Collection<? extends PlanItemInstanceLifecycle<?>> nodeInstances = container.getChildren();
		for (PlanItemInstanceLifecycle<?> nodeInstance : nodeInstances) {
			if (nodeInstance instanceof PlanItemInstanceFactoryNodeInstance && ((PlanItemInstanceFactoryNodeInstance<?>) nodeInstance).isPlanItemInstanceStillRequired()) {
				return false;
			} else if (nodeInstance instanceof MilestonePlanItemInstance && ((MilestonePlanItemInstance) nodeInstance).isCompletionStillRequired()) {
				return false;
			} else if (nodeInstance instanceof ControllableItemInstanceLifecycle && ((ControllableItemInstanceLifecycle<?>) nodeInstance).isCompletionStillRequired()) {
				return false;
			}

		}
		return true;

	}

	public static boolean isSubscribing(ControllableItemInstanceLifecycle<?> ni) {
		return ni.getPlanElementState() == PlanElementState.ACTIVE || ni.getPlanElementState() == PlanElementState.ENABLED;
	}

	public static void populateSubscriptionsActivatedByParametersOfContainedTasks(PlanItemInstanceContainer caseInstance, SubscriptionContext sc) {
		Collection<NodeInstance> nodeInstances = caseInstance.getNodeInstances();
		for (NodeInstance ni : nodeInstances) {
			if (ni.getNode() instanceof PlanItem) {
				PlanItem<?> pi = (PlanItem<?>) ni.getNode();
				if (pi.getPlanInfo().getDefinition() instanceof TaskDefinition && ni instanceof ControllableItemInstanceLifecycle && isSubscribing((ControllableItemInstanceLifecycle<?>) ni)) {
					TaskDefinition td = (TaskDefinition) pi.getPlanInfo().getDefinition();
					ExpressionUtil.populateSubscriptionsActivatedByParameters(sc, td.getOutputs());
				}
			}
			if (ni instanceof StagePlanItemInstance) {
				((StagePlanItemInstance) ni).populateSubscriptionsActivatedByParameters(sc);
			}
		}
	}

	public static void addSubscribingCaseParameters(Set<CaseParameter> params, PlanItemInstanceContainer caseInstance) {
		for (NodeInstance ni : caseInstance.getNodeInstances()) {
			if (ni.getNode() instanceof PlanItem) {
				PlanItem<?> pi = (PlanItem<?>) ni.getNode();
				PlanItemDefinition def = pi.getPlanInfo().getDefinition();
				if (def instanceof TaskDefinition) {
					params.addAll(((TaskDefinition) def).getOutputs());
				}
			} else if (ni instanceof StagePlanItemInstance) {
				((StagePlanItemInstance) ni).addSubscribingCaseParameters(params);
			}
		}
	}

	public static void addCaseFileItemOnPartsForParameters(Collection<CaseParameter> items, PlanItemInstanceContainer container, Map<OnPartInstance, OnPartInstanceSubscription> target) {
		for (CaseParameter parameter : items) {
			for (NodeInstance node : container.getNodeInstances()) {
				if (node instanceof OnPartInstance) {
					OnPartInstance onPartInstance = (OnPartInstance) node;
					if (onPartInstance.getOnPart() instanceof CaseFileItemOnPart) {
						CaseFileItemOnPart onPart = (CaseFileItemOnPart) onPartInstance.getOnPart();
						if (onPart.getSourceCaseFileItem().getElementId().equals(parameter.getBoundVariable().getElementId())) {
							OnPartInstanceSubscription subscription = target.get(onPartInstance);
							if (subscription == null) {
								target.put(onPartInstance, new OnPartInstanceSubscription(onPartInstance, parameter));
							} else {
								subscription.addParameter(parameter);
							}
						}
					}
				} else if (node instanceof StagePlanItemInstance) {
					((StagePlanItemInstance) node).addCaseFileItemOnPartsForParameters(items, target);
				}
			}
		}
	}

	public static ControllableItemInstanceLifecycle<?> findNodeForWorkItem(PlanItemInstanceContainer container, long id) {
		for (NodeInstance ni : container.getNodeInstances()) {
			if (ni instanceof ControllableItemInstanceLifecycle && ((ControllableItemInstanceLifecycle<?>) ni).getWorkItemId() == id) {
				return (ControllableItemInstanceLifecycle<?>) ni;
			} else if (ni instanceof PlanItemInstanceContainer) {
				ControllableItemInstanceLifecycle<?> found = ((PlanItemInstanceContainer) ni).findNodeForWorkItem(id);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	public static PlanningTableContainer findPlanElementWithPlanningTable(PlanItemInstanceContainer container, long containerWorkItemId) {
		PlanningTableContainer pewpt = null;
		if (containerWorkItemId == container.getWorkItemId()) {
			//Coz we have to check for the current element first
			pewpt = container;
		} else {
			for (NodeInstance ni : container.getNodeInstances()) {
				if (ni instanceof PlanItemInstanceContainer) {
					pewpt = ((PlanItemInstanceContainer) ni).findPlanElementWithPlanningTable(containerWorkItemId);
					if (pewpt != null) {
						break;
					}
				}
			}
		}
		return pewpt;
	}

	public static Collection<? extends PlanItemInstanceLifecycle<?>> getChildren(PlanItemInstanceContainer container) {
		Set<PlanItemInstanceLifecycle<?>> result = new HashSet<PlanItemInstanceLifecycle<?>>();
		for (NodeInstance nodeInstance :container.getNodeInstances()) {
			if (nodeInstance instanceof PlanItemInstanceLifecycle) {
				result.add((PlanItemInstanceLifecycle<?>) nodeInstance);
			}
		}
		return result;
	}

}
