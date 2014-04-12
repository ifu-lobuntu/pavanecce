package org.pavanecce.cmmn.instance;

import org.pavanecce.cmmn.flow.CaseFileItemTransition;

public class CaseFileItemEvent {
	String caseFileItemName;
	CaseFileItemTransition transition;
	Object value;
	public CaseFileItemEvent(String caseFileItemName, CaseFileItemTransition transition, Object value) {
		super();
		this.caseFileItemName = caseFileItemName;
		this.transition = transition;
		this.value = value;
	}
	public String getCaseFileItemName() {
		return caseFileItemName;
	}
	public CaseFileItemTransition getTransition() {
		return transition;
	}
	public Object getValue() {
		return value;
	}
	
}
