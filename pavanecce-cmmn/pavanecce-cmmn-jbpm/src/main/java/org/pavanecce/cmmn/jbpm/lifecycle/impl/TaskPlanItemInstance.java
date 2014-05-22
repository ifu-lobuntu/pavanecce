package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;
import org.pavanecce.cmmn.jbpm.flow.TaskItemWithDefinition;

public abstract class TaskPlanItemInstance<T extends TaskDefinition, X extends TaskItemWithDefinition<T>> extends AbstractControllableItemInstance<T, X> {

	private static final long serialVersionUID = -2759757105782259528L;

	public TaskPlanItemInstance() {
		super();
	}

	@Override
	protected final boolean isBlocking() {
		return getItem().getDefinition().isBlocking();
	}

	@Override
	protected final boolean isLinkedIncomingNodeRequired() {
		return false;
	}
}