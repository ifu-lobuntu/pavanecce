package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Collection;
import java.util.Map;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;

public class SubscriptionContext {
	Map<CaseFileItem, Collection<Object>> parentSubscriptions;
	Collection<Object> subscriptions;
	CaseInstance processInstance;
	public SubscriptionContext(CaseInstance processInstance, Collection<Object> subscriptions, Map<CaseFileItem, Collection<Object>> parentSubscriptions) {
		super();
		this.processInstance = processInstance;
		this.subscriptions = subscriptions;
		this.parentSubscriptions = parentSubscriptions;
	}
	public Map<CaseFileItem, Collection<Object>> getParentSubscriptions() {
		return parentSubscriptions;
	}
	public Collection<Object> getSubscriptions() {
		return subscriptions;
	}
	public CaseInstance getProcessInstance() {
		return processInstance;
	}
	
}
