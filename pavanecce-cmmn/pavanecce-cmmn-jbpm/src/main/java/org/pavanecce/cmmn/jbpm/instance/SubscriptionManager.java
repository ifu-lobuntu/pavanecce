package org.pavanecce.cmmn.jbpm.instance;

import java.util.Collection;
import java.util.Map;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.common.ObjectPersistence;

public interface SubscriptionManager {
	String ENV_NAME = SubscriptionManager.class.getName();

	void subscribe(CaseInstance process, Collection<Object> targets, Map<CaseFileItem, Collection<Object>> parentSubscriptions, ObjectPersistence p);


	ObjectPersistence getObjectPersistence(CaseInstance p);

}
