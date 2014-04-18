package org.pavanecce.cmmn.instance;

import java.util.Collection;

public interface CaseSubscriptionInfo <T extends CaseFileItemSubscriptionInfo>{

	Collection<? extends T> getCaseFileItemSubscriptions();
	void addCaseFileItemSubscription(T i);
	void removeCaseFileItemSubscription(T i);
}
