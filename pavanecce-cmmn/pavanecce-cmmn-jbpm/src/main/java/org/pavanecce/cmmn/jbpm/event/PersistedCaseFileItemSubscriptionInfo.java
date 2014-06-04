package org.pavanecce.cmmn.jbpm.event;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;

public interface PersistedCaseFileItemSubscriptionInfo extends CaseFileItemSubscriptionInfo {
	String getIdentifier();

	void setCaseKey(String caseKey);

	void setTransition(CaseFileItemTransition transition);

	void setItemName(String itemName);

	void setProcessInstanceId(long id);

	void setRelatedItemName(String itemName);

	void setCaseSubscription(CaseSubscriptionInfo<?> caseSubscription);

	CaseSubscriptionInfo<?> getCaseSubscription();

}
