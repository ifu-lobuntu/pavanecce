package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.ItemWithDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;
import org.pavanecce.cmmn.jbpm.infra.OnPartInstanceSubscription;
import org.pavanecce.cmmn.jbpm.lifecycle.ControllableItemInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstanceContainer;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanningTableContainerInstance;

/**
 * Defines all the logic common to CaseInstances and StagePlanItemInstances. This is required as it is not possible to
 * give them a common superclass
 */
public class PlanItemInstanceContainerUtil {

	public static boolean canComplete(PlanItemInstanceContainer container) {
		if (container.getPlanElementState() != PlanElementState.ACTIVE) {
			return false;
		}
		Collection<? extends PlanItemInstance<?>> nodeInstances = container.getChildren();
		for (PlanItemInstance<?> nodeInstance : nodeInstances) {
			if (nodeInstance instanceof PlanItemInstanceFactoryNodeInstance
					&& ((PlanItemInstanceFactoryNodeInstance<?>) nodeInstance).isPlanItemInstanceStillRequired()) {
				return false;
			} else if (nodeInstance instanceof MilestoneInstance && ((MilestoneInstance) nodeInstance).isCompletionStillRequired()) {
				return false;
			} else if (nodeInstance instanceof ControllableItemInstance && ((ControllableItemInstance<?>) nodeInstance).isCompletionStillRequired()) {
				return false;
			}

		}
		return true;

	}

	private static boolean isSubscribing(ControllableItemInstance<?> ni) {
		return ni.getPlanElementState() == PlanElementState.ACTIVE || ni.getPlanElementState() == PlanElementState.ENABLED;
	}

	public static void populateSubscriptionsActivatedByParametersOfContainedTasks(PlanItemInstanceContainer caseInstance, SubscriptionContext sc) {
		Collection<NodeInstance> nodeInstances = caseInstance.getNodeInstances();
		for (NodeInstance ni : nodeInstances) {
			if (ni.getNode() instanceof ItemWithDefinition<?>) {
				ItemWithDefinition<?> pi = (ItemWithDefinition<?>) ni.getNode();
				if (pi.getDefinition() instanceof TaskDefinition && ni instanceof ControllableItemInstance && isSubscribing((ControllableItemInstance<?>) ni)) {
					TaskDefinition td = (TaskDefinition) pi.getDefinition();
					ExpressionUtil.populateSubscriptionsActivatedByParameters(sc, td.getOutputs());
				}
			}
			if (ni instanceof StageInstance) {
				((StageInstance) ni).populateSubscriptionsActivatedByParameters(sc);
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
			} else if (ni instanceof StageInstance) {
				((StageInstance) ni).addSubscribingCaseParameters(params);
			}
		}
	}

	public static void addCaseFileItemOnPartsForParameters(Collection<CaseParameter> items, PlanItemInstanceContainer container,
			Map<OnPartInstance, OnPartInstanceSubscription> target) {
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
				} else if (node instanceof StageInstance) {
					((StageInstance) node).addCaseFileItemOnPartsForParameters(items, target);
				}
			}
		}
	}

	public static ControllableItemInstance<?> findNodeForWorkItem(PlanItemInstanceContainer container, long id) {
		for (NodeInstance ni : container.getNodeInstances()) {
			if (ni instanceof ControllableItemInstance && ((ControllableItemInstance<?>) ni).getWorkItemId() == id) {
				return (ControllableItemInstance<?>) ni;
			} else if (ni instanceof PlanItemInstanceContainer) {
				ControllableItemInstance<?> found = ((PlanItemInstanceContainer) ni).findNodeForWorkItem(id);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	public static PlanningTableContainerInstance findPlanElementWithPlanningTable(PlanItemInstanceContainer container, long containerWorkItemId) {
		PlanningTableContainerInstance pewpt = null;
		if (containerWorkItemId == container.getWorkItemId()) {
			// Coz we have to check for the current element first
			pewpt = container;
		} else {
			for (NodeInstance ni : container.getNodeInstances()) {
				if (ni instanceof PlanItemInstanceContainer) {
					pewpt = ((PlanItemInstanceContainer) ni).findPlanningTableContainerInstance(containerWorkItemId);
					if (pewpt != null) {
						break;
					}
				}
			}
		}
		return pewpt;
	}

	public static Collection<? extends PlanItemInstance<?>> getChildren(PlanItemInstanceContainer container) {
		Set<PlanItemInstance<?>> result = new HashSet<PlanItemInstance<?>>();
		for (NodeInstance nodeInstance : container.getNodeInstances()) {
			if (nodeInstance instanceof PlanItemInstance) {
				result.add((PlanItemInstance<?>) nodeInstance);
			}
		}
		return result;
	}

}
