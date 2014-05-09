package org.pavanecce.cmmn.jbpm.instance;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;

public interface CaseFileItemSubscriptionInfo {

	String getItemName();

	CaseFileItemTransition getTransition();

	String getRelatedItemName();

	boolean isActive();

	void activate();

	void deactivate();

	boolean isEquivalent(CaseFileItemSubscriptionInfo other);

	String getCaseKey();

	long getProcessInstanceId();
}
