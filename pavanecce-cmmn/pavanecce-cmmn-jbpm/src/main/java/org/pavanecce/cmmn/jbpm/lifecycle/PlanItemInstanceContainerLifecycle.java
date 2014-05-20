package org.pavanecce.cmmn.jbpm.lifecycle;

import java.util.Collection;

import org.pavanecce.cmmn.jbpm.flow.PlanItemContainer;

public interface PlanItemInstanceContainerLifecycle extends PlanElementWithPlanningTable {
	

	Collection<? extends ItemInstanceLifecycle<?>> getChildren();

	boolean canComplete();

	PlanItemContainer getPlanItemContainer();



}