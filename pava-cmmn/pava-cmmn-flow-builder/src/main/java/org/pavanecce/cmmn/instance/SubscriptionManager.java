package org.pavanecce.cmmn.instance;

import org.pavanecce.cmmn.flow.CaseFileItem;

public interface SubscriptionManager {
	String ENV_NAME = SubscriptionManager.class.getName();

	void subscribe(CaseInstance process, CaseFileItem caseFileItem, Object target);

	void unsubscribe(CaseInstance process, CaseFileItem caseFileItem, Object target);
}
