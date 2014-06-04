package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.drools.core.process.instance.WorkItem;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.ApplicableDiscretionaryItem;
import org.pavanecce.cmmn.jbpm.TaskParameters;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.flow.PlanningTable;
import org.pavanecce.cmmn.jbpm.flow.TaskItemWithDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.ControllableItemInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstanceContainer;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanningTableContainerInstance;

public class HumanTaskInstance extends TaskPlanItemInstance<HumanTask, TaskItemWithDefinition<HumanTask>> implements PlanningTableContainerInstance {

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
		if (isActivatedManually()) {
			// Let the role do the assignment
			return null;
		} else {
			// need to find someone
			// TODO think this through - should be done in the WorkItemHandler rather
			Collection<String> roleAssignments = getCaseInstance().getRoleAssignments(getIdealRoles());
			if (roleAssignments.size() == 1) {
				return roleAssignments.iterator().next();
			} else {
				return null;
			}
		}
	}

	@Override
	public void signalEvent(String type, Object event) {
		if (type.equals(TaskParameters.WORK_ITEM_UPDATED) && isMyWorkItem((WorkItem) event)) {
			WorkItem wi = (WorkItem) event;
			String owner = (String) wi.getResult(TaskParameters.ACTUAL_OWNER);
			if (owner != null) {
				getCaseInstance().addRoleAssignment(getItem().getDefinition().getPerformer().getName(), owner);
			}
			PlanItemTransition transition = (PlanItemTransition) wi.getResult(TaskParameters.TRANSITION);
			if (isCompletionTransition(transition)) {
				for (CaseParameter cp : getItem().getDefinition().getOutputs()) {
					Object val = wi.getResult(cp.getName());
					if (val != null) {
						writeToBinding(cp, val);
					}
				}

			}
		}
		super.signalEvent(type, event);
	}

	private boolean isCompletionTransition(PlanItemTransition transition) {
		boolean isCompletionTransition1 = transition == PlanItemTransition.COMPLETE || transition == PlanItemTransition.EXIT
				|| transition == PlanItemTransition.FAULT;
		return isCompletionTransition1;
	}

	/********* PlanningTableContainner implementation *******/

	@Override
	public PlanningTable getPlanningTable() {
		return super.getItem().getDefinition().getPlanningTable();
	}

	@Override
	public PlanItemInstanceContainer getPlanItemInstanceCreator() {
		return (PlanItemInstanceContainer) getNodeInstanceContainer();
	}

	@Override
	public ControllableItemInstance<?> ensurePlanItemCreated(String discretionaryItemId, WorkItem wi) {
		return PlanningTableContainerInstanceUtil.ensurePlanItemCreated(this, discretionaryItemId, wi);
	}

	@Override
	public void addApplicableItems(Map<String, ApplicableDiscretionaryItem> result, Set<String> usersRoles) {
		PlanningTableContainerInstanceUtil.addApplicableItems(this, result, usersRoles);
	}

	@Override
	public NodeInstance getPlanningContextNodeInstance() {
		return this;
	}

	@Override
	public WorkItem createPlannedItem(String tableItemId) {
		return PlanningTableContainerInstanceUtil.createPlannedTask(this, tableItemId);
	}

	@Override
	public void makeDiscretionaryItemAvailable(String discretionaryItemId) {
		PlanningTableContainerInstanceUtil.makeDiscretionaryItemAvailable(this, discretionaryItemId);
	}

}
