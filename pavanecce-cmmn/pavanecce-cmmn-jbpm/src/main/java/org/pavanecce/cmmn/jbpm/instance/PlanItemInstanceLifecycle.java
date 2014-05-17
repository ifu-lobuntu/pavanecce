package org.pavanecce.cmmn.jbpm.instance;

import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;

public interface PlanItemInstanceLifecycle<T extends PlanItemDefinition> extends PlanElementLifecycle {


	String getPlanItemName();

	PlanItem<T> getPlanItem();


}
