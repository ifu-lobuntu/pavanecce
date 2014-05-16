package org.pavanecce.cmmn.jbpm.instance;

import java.util.Collection;

import org.pavanecce.cmmn.jbpm.flow.PlanItemContainer;

public interface PlanItemInstanceContainer extends PlanItemInstanceContainerLifecycle{
	Collection<? extends PlanItemInstanceLifecycle<?>> getChildren();
	boolean canComplete();
	PlanItemContainer getPlanItemContainer();
}
