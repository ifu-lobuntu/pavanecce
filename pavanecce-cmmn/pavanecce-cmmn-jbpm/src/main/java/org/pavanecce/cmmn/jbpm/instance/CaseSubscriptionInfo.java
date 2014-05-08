package org.pavanecce.cmmn.jbpm.instance;

import java.util.Collection;

public interface CaseSubscriptionInfo <T extends PersistedCaseFileItemSubscriptionInfo>{

	Collection<? extends T> getCaseFileItemSubscriptions();
	void addCaseFileItemSubscription(T i);
	void removeCaseFileItemSubscription(T i);
}
