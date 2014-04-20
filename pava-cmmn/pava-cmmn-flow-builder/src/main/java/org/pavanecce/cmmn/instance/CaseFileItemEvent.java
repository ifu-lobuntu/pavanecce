package org.pavanecce.cmmn.instance;

import org.pavanecce.cmmn.flow.CaseFileItemTransition;

public class CaseFileItemEvent {
	String caseFileItemName;
	CaseFileItemTransition transition;
	Object value;
	Object parentObject;
	public CaseFileItemEvent(String caseFileItemName, CaseFileItemTransition transition, Object parentObject, Object value) {
		super();
		this.parentObject=parentObject;
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
	public CaseFileItemTransition getTransition() {
		return transition;
	}
	public Object getValue() {
		return value;
	}
	
}
