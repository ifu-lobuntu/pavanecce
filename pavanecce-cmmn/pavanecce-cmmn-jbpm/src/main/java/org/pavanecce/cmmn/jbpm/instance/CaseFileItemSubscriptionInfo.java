package org.pavanecce.cmmn.jbpm.instance;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;

public interface CaseFileItemSubscriptionInfo {

	long getProcessId();

	CaseFileItemTransition getTransition();

	String getItemName();

	String getCaseKey();

	void setCaseKey(String caseKey);

	void setProcessId(long processId);

	void setTransition(CaseFileItemTransition transition);

	void setItemName(String itemName);

	String getRelatedItemName();

	void setRelatedItemName(String itemName);

	void setCaseSubscription(CaseSubscriptionInfo<?> caseSubscription);

	CaseSubscriptionInfo<?> getCaseSubscription();

}
