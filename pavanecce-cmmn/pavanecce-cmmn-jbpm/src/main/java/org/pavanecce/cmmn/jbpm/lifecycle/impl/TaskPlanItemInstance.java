package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Collection;
import java.util.HashSet;

import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;
import org.pavanecce.cmmn.jbpm.flow.TaskItemWithDefinition;

public abstract class TaskPlanItemInstance<T extends TaskDefinition, X extends TaskItemWithDefinition<T>> extends ControllableItemInstanceImpl<T, X> {

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

	@SuppressWarnings("unchecked")
	protected void writeToBinding(CaseParameter cp, Object val) {
		if (cp.getBindingRefinement().isValid()) {
			ExpressionUtil.writeToBindingRefinement(this, cp, val);
		} else {
			if (cp.getBoundVariable().isCollection()) {
				Collection<Object> coll = (Collection<Object>) getCaseInstance().getVariable(cp.getBoundVariable().getName());
				if (coll == null) {
					getCaseInstance().setVariable(cp.getBoundVariable().getName(), coll = new HashSet<Object>());
				}
				if (val instanceof Collection) {
					coll.addAll((Collection<Object>) val);
				} else {
					coll.add(val);
				}
			} else {
				getCaseInstance().setVariable(cp.getBoundVariable().getName(), val);
			}
		}
	}
}