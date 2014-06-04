package org.pavanecce.cmmn.jbpm.event;

import java.util.Collection;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;

public interface CaseSubscriptionInfo<T extends PersistedCaseFileItemSubscriptionInfo> {

	Collection<? extends T> getCaseFileItemSubscriptions();

	void addCaseFileItemSubscription(T i);

	void removeCaseFileItemSubscription(T i);

	public abstract T findCaseFileItemSubscription(String itemName, CaseFileItemTransition transition);

	CaseSubscriptionKey getId();
}
