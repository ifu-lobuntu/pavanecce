package org.pavanecce.cmmn.jbpm.event;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;

public class CaseFileItemEvent extends CaseEvent {
	String caseFileItemName;
	CaseFileItemTransition transition;
	Object value;
	Object parentObject;

	public CaseFileItemEvent(String caseFileItemName, CaseFileItemTransition transition, Object parentObject, Object value) {
		super();
		this.parentObject = parentObject;
		this.caseFileItemName = caseFileItemName;
		this.transition = transition;
		this.value = value;
	}

	public String getCaseFileItemName() {
		return caseFileItemName;
	}

	public Object getParentObject() {
		return parentObject;
	}

	@Override
	public CaseFileItemTransition getTransition() {
		return transition;
	}

	@Override
	public Object getValue() {
		return value;
	}

}
