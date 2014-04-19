package org.pavanecce.cmmn.instance;

import org.pavanecce.cmmn.flow.CaseFileItemTransition;

public interface CaseFileItemSubscriptionInfo {

	public abstract long getProcessId();

	public abstract CaseFileItemTransition getTransition();

	public abstract String getItemName();

	public abstract String getCaseKey();

	public abstract void setCaseKey(String caseKey);

	public abstract void setProcessId(long processId);

	public abstract void setTransition(CaseFileItemTransition transition);

	public abstract void setItemName(String itemName);

	public abstract void setCaseSubscription(CaseSubscriptionInfo<?> caseSubscription);

	public abstract CaseSubscriptionInfo<?> getCaseSubscription();

}
