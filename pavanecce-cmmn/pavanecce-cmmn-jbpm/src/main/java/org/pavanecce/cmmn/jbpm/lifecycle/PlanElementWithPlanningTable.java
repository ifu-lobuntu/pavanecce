package org.pavanecce.cmmn.jbpm.lifecycle;

import org.pavanecce.cmmn.jbpm.flow.PlanningTable;

public interface PlanElementWithPlanningTable extends PlanElementLifecycleWithTask {
	PlanningTable getPlanningTable();
}
