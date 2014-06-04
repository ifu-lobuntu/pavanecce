package org.pavanecce.cmmn.jbpm.lifecycle;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.PlanItemContainer;
import org.pavanecce.cmmn.jbpm.infra.OnPartInstanceSubscription;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.OnPartInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.SubscriptionContext;

public interface PlanItemInstanceContainer extends PlanningTableContainerInstance, NodeInstanceContainer {

	Collection<? extends PlanItemInstance<?>> getChildren();

	boolean canComplete();

	PlanItemContainer getPlanItemContainer();

	void populateSubscriptionsActivatedByParameters(SubscriptionContext sc);

	void addSubscribingCaseParameters(Set<CaseParameter> params);

	void addCaseFileItemOnPartsForParameters(Collection<CaseParameter> items, Map<OnPartInstance, OnPartInstanceSubscription> onCaseFileItemParts);

	ControllableItemInstance<?> findNodeForWorkItem(long id);

	PlanningTableContainerInstance findPlanningTableContainerInstance(long containerWorkItemId);
}