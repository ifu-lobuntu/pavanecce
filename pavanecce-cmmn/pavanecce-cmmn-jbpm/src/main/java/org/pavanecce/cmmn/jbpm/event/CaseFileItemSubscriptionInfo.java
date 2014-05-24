package org.pavanecce.cmmn.jbpm.event;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;

public interface CaseFileItemSubscriptionInfo {

	String getItemName();

	CaseFileItemTransition getTransition();

	String getRelatedItemName();

	boolean isValid();

	void validate();

	void invalidate();

	boolean isEquivalent(CaseFileItemSubscriptionInfo other);

	String getCaseKey();

	long getProcessInstanceId();
}
