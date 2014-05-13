package org.pavanecce.cmmn.jbpm.instance;

import java.util.Collection;

public interface PlanItemInstanceContainer extends PlanItemInstance,HumanControlledPlanItemInstance {
	Collection<PlanItemInstance> getChildren();
}
