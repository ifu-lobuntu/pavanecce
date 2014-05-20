package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import org.drools.core.process.core.Work;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.PlanningTable;
import org.pavanecce.cmmn.jbpm.flow.TaskItemWithDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementWithPlanningTable;

public class HumanTaskPlanItemInstance extends TaskPlanItemInstance<HumanTask,TaskItemWithDefinition<HumanTask>> implements PlanElementWithPlanningTable {

	private static final long serialVersionUID = 8452936237272366757L;

	protected boolean isWaitForCompletion() {
		return super.getItem().getDefinition().isBlocking();
	}
	@Override
	protected Work getWork() {
		return super.getItem().getWork();
	}
	@Override
	public PlanningTable getPlanningTable() {
		return super.getItem().getDefinition().getPlanningTable();
	}
}
