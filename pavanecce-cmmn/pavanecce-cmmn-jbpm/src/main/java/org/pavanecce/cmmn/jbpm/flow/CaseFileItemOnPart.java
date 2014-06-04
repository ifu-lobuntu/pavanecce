package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;

import org.pavanecce.cmmn.jbpm.event.CaseEvent;
import org.pavanecce.cmmn.jbpm.event.CaseFileItemEvent;

public class CaseFileItemOnPart extends OnPart implements Serializable {
	private static final long serialVersionUID = -9167236068103073693L;
	private CaseFileItemTransition standardEvent;
	private CaseFileItem sourceCaseFileItem;
	private CaseFileItem relatedCaseFileItem;
	private String sourceRef;
	private String relationRef;

	public CaseFileItemTransition getStandardEvent() {
		return standardEvent;
	}

	public void setStandardEvent(CaseFileItemTransition type) {
		this.standardEvent = type;
	}

	public CaseFileItem getSourceCaseFileItem() {
		return sourceCaseFileItem;
	}

	public void setSourceCaseFileItem(CaseFileItem caseFileItem) {
		this.sourceCaseFileItem = caseFileItem;
	}

	public void setSourceRef(String value) {
		this.sourceRef = value;
	}

	@Override
	public String getType() {
		return getType(this.sourceCaseFileItem.getName(), standardEvent);
	}

	public String getIdentifier() {
		if (this.relatedCaseFileItem != null) {
			return getType(this.sourceCaseFileItem.getName() + "." + relatedCaseFileItem.getName(), standardEvent);
		} else {
			return getType(this.sourceCaseFileItem.getName(), standardEvent);
		}
	}

	public String getSourceRef() {
		return sourceRef;
	}

	public String getRelationRef() {
		return relationRef;
	}

	public void setRelationRef(String relationRef) {
		this.relationRef = relationRef;
	}

	public CaseFileItem getRelatedCaseFileItem() {
		return relatedCaseFileItem;
	}

	public void setRelatedCaseFileItem(CaseFileItem relatedCaseFileItem) {
		this.relatedCaseFileItem = relatedCaseFileItem;
	}

	@Override
	public CaseEvent createEvent(Object peek) {
		return new CaseFileItemEvent(sourceCaseFileItem.getName(), getStandardEvent(), null, peek);
	}

}
