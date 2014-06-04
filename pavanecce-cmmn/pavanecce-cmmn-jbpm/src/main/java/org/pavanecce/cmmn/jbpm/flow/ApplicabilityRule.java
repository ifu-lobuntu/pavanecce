package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.Constraint;

public class ApplicabilityRule implements CMMNElement {
	private static final long serialVersionUID = 2760086481660795186L;
	private String elementId;
	private String contextRef;
	private CaseFileItem context;
	private Constraint condition;

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public String getContextRef() {
		return contextRef;
	}

	public void setContextRef(String contextRef) {
		this.contextRef = contextRef;
	}

	public CaseFileItem getContext() {
		return context;
	}

	public void setContext(CaseFileItem context) {
		this.context = context;
	}

	public Constraint getCondition() {
		return condition;
	}

	public void setCondition(Constraint condition) {
		this.condition = condition;
	}

}
