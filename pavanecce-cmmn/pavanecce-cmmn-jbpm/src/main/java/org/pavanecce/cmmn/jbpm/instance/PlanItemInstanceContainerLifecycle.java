package org.pavanecce.cmmn.jbpm.instance;

import java.util.Collection;

import org.pavanecce.cmmn.jbpm.flow.PlanItemContainer;

public interface PlanItemInstanceContainerLifecycle extends PlanElementLifecycleWithTask {
	

	Collection<? extends PlanItemInstanceLifecycle<?>> getChildren();

	boolean canComplete();

	PlanItemContainer getPlanItemContainer();



}