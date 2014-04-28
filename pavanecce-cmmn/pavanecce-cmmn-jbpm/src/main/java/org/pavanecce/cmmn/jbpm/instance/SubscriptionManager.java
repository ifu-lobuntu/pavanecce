package org.pavanecce.cmmn.jbpm.instance;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;

public interface SubscriptionManager {
	String ENV_NAME = SubscriptionManager.class.getName();

	void subscribe(CaseInstance process, CaseFileItem caseFileItem, Object target);

	void unsubscribe(CaseInstance process, CaseFileItem caseFileItem, Object target);
}
