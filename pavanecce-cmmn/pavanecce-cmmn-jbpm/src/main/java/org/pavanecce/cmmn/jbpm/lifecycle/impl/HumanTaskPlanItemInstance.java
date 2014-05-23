package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Collection;

import org.drools.core.process.instance.WorkItem;
import org.pavanecce.cmmn.jbpm.TaskParameters;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.PlanItemContainer;
import org.pavanecce.cmmn.jbpm.flow.PlanningTable;
import org.pavanecce.cmmn.jbpm.flow.TaskItemWithDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementWithPlanningTable;

public class HumanTaskPlanItemInstance extends TaskPlanItemInstance<HumanTask, TaskItemWithDefinition<HumanTask>> implements PlanElementWithPlanningTable {

	private static final long serialVersionUID = 8452936237272366757L;

	protected boolean isWaitForCompletion() {
		return super.getItem().getDefinition().isBlocking();
	}

	@Override
	protected String getIdealRoles() {
		TaskItemWithDefinition<HumanTask> item = getItem();
		return item.getDefinition().getPerformer().getName();
	}

	@Override
	protected String getIdealOwner() {
		if (PlanItemInstanceUtil.isActivatedManually(this)) {
			// Let the role do the assignment
			return null;
		} else { 
			// need to find someone
			Collection<String> roleAssignments = getCaseInstance().getRoleAssignments(getIdealRoles());
			if (roleAssignments.size() == 1) {
				return roleAssignments.iterator().next();
			}else{
				///TODO think this through
				return null;
			}
		}
	}

	@Override
	public PlanningTable getPlanningTable() {
		return super.getItem().getDefinition().getPlanningTable();
	}

	@Override
	public void signalEvent(String type, Object event) {
		if (type.equals(TaskParameters.WORK_ITEM_UPDATED) && isMyWorkItem((WorkItem) event)) {
			WorkItem wi = (WorkItem) event;
			String owner = (String) wi.getResult(TaskParameters.ACTUAL_OWNER);
			if (owner != null) {
				getCaseInstance().addRoleAssignment(getItem().getDefinition().getPerformer().getName(), owner);
			}
		}
		super.signalEvent(type, event);
	}

	@Override
	public PlanItemContainer getPlanItemContainer() {
		return getItem().getPlanItemContainer();
	}
}
