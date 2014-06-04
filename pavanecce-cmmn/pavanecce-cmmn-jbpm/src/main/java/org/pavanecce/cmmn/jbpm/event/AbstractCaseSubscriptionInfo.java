package org.pavanecce.cmmn.jbpm.event;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;

public abstract class AbstractCaseSubscriptionInfo<X extends PersistedCaseFileItemSubscriptionInfo> implements CaseSubscriptionInfo<X> {
	@Override
	public X findCaseFileItemSubscription(String itemName, CaseFileItemTransition transition) {
		for (X x : getCaseFileItemSubscriptions()) {
			if (x.getTransition() == transition && x.getItemName().equals(itemName)) {
				return x;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof CaseSubscriptionInfo) {
			return ((CaseSubscriptionInfo<?>) obj).getId().equals(getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}
}
