package org.pavanecce.cmmn.jbpm.lifecycle;

import java.util.Collection;

import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.pavanecce.cmmn.jbpm.flow.PlanItemContainer;

public interface PlanItemInstanceContainerLifecycle extends PlanElementWithPlanningTable, NodeInstanceContainer {
	

	Collection<? extends ItemInstanceLifecycle<?>> getChildren();

	boolean canComplete();

	PlanItemContainer getPlanItemContainer();

}