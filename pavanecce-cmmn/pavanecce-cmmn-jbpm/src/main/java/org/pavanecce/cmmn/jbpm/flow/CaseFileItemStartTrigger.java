package org.pavanecce.cmmn.jbpm.flow;

public class CaseFileItemStartTrigger extends CaseFileItemOnPart {
	private static final long serialVersionUID = 1602328317980746322L;

	@Override
	public OnPart copy() {
		CaseFileItemStartTrigger result = new CaseFileItemStartTrigger();
		result.setId(getId());
		result.setName(getName());
		result.setStandardEvent(getStandardEvent());
		result.setSourceCaseFileItem(getSourceCaseFileItem());
		result.setSourceRef(getSourceRef());
		return result;
	}
}
