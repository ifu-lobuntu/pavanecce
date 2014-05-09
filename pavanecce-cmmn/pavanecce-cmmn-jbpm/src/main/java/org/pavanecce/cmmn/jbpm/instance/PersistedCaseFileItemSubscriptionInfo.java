package org.pavanecce.cmmn.jbpm.instance;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;

public interface PersistedCaseFileItemSubscriptionInfo extends CaseFileItemSubscriptionInfo {

	CaseFileItemTransition getTransition();

	String getItemName();

	String getCaseKey();

	void setCaseKey(String caseKey);

	void setTransition(CaseFileItemTransition transition);

	void setItemName(String itemName);

	void setProcessInstanceId(long id);

	String getRelatedItemName();

	void setRelatedItemName(String itemName);

	void setCaseSubscription(CaseSubscriptionInfo<?> caseSubscription);

}
