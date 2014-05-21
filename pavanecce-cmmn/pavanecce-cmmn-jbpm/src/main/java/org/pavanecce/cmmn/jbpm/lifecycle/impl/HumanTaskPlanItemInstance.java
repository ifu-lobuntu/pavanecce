package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Collection;
import java.util.HashSet;

import org.drools.core.process.instance.WorkItem;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.PlanningTable;
import org.pavanecce.cmmn.jbpm.flow.TaskItemWithDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementWithPlanningTable;

public class HumanTaskPlanItemInstance extends TaskPlanItemInstance<HumanTask, TaskItemWithDefinition<HumanTask>> implements PlanElementWithPlanningTable {

	private static final long serialVersionUID = 8452936237272366757L;

	protected boolean isWaitForCompletion() {
		return super.getItem().getDefinition().isBlocking();
	}

	protected boolean isLinkedIncomingNodeRequired() {
		return false;
	}

	@Override
	public PlanningTable getPlanningTable() {
		return super.getItem().getDefinition().getPlanningTable();
	}

	@Override
	public void signalEvent(String type, Object event) {
		if (type.equals(WORK_ITEM_UPDATED) && isMyWorkItem((WorkItem) event)) {
			WorkItem wi = (WorkItem) event;
			if (wi.getResult(ACTUAL_OWNER) != null) {
				String performer = getItem().getDefinition().getPerformer().getName();
				Collection<String> performers = (Collection<String>) getCaseInstance().getVariable(performer);
				if (performers == null) {
					getCaseInstance().setVariable(performer, performers = new HashSet<String>());
				}
				performers.add((String) wi.getResult(ACTUAL_OWNER));
			}
		}
		super.signalEvent(type, event);
	}
}
