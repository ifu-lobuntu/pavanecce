package org.pavanecce.cmmn.jbpm.instance.impl;

import org.drools.core.process.core.Work;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.HumanTaskPlanItem;

public class HumanTaskPlanItemInstance extends TaskPlanItemInstance<HumanTask> {

	private static final long serialVersionUID = 8452936237272366757L;

	protected boolean isWaitForCompletion() {
		return getPlanItem().isWaitForCompletion();
	}
	@Override
	public HumanTaskPlanItem getPlanItem() {
		return (HumanTaskPlanItem) super.getPlanItem();
	}

	@Override
	protected Work getWork() {
		return getPlanItem().getWork();
	}
}
