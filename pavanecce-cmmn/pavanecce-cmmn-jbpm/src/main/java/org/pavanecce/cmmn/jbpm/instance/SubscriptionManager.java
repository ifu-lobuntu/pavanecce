package org.pavanecce.cmmn.jbpm.instance;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.common.ObjectPersistence;

public interface SubscriptionManager {
	String ENV_NAME = SubscriptionManager.class.getName();

	void subscribe(CaseInstance process, CaseFileItem caseFileItem, Object target, ObjectPersistence p);

	void subscribeToParent(CaseInstance process, CaseFileItem caseFileItem, Object parent, ObjectPersistence p);

	ObjectPersistence getObjectPersistence(CaseInstance p);

	void unsubscribe(CaseInstance process, CaseFileItem caseFileItem, Object target, ObjectPersistence p);

	void unsubscribeFromParent(CaseInstance process, CaseFileItem caseFileItem, Object parent, ObjectPersistence p);
}
