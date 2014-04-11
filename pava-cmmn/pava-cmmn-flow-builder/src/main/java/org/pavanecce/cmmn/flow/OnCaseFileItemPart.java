package org.pavanecce.cmmn.flow;

import java.io.Serializable;

import org.jbpm.process.core.context.variable.Variable;

public class OnCaseFileItemPart extends OnPart implements Serializable {
	private static final long serialVersionUID = -9167236068103073693L;
	private CaseFileItemTransition standardEvent;
	private Variable caseFileItem;
	private String sourceRef;

	public CaseFileItemTransition getStandardEvent() {
		return standardEvent;
	}

	public void setStandardEvent(CaseFileItemTransition type) {
		this.standardEvent = type;
	}

	public Variable getCaseFileItem() {
		return caseFileItem;
	}

	public void setCaseFileItem(Variable caseFileItem) {
		this.caseFileItem = caseFileItem;
	}

	@Override
	public boolean acceptsEvent(String type, Object event) {
		return type.equals(caseFileItem.getName() + standardEvent.name());
	}

	public void setSourceRef(String value) {
		this.sourceRef = value;

	}
	@Override
	public String getType() {
		return sourceRef+standardEvent.name();
	}

	public String getSourceRef() {
		return sourceRef;
	};

}
