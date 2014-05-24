package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Collection;
import java.util.HashSet;
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

	public void addParentSubscription(CaseFileItem cfi, Object v) {
		Collection<Object> collection = parentSubscriptions.get(cfi);
		if (collection == null) {
			parentSubscriptions.put(cfi, collection = new HashSet<Object>());
		}
		addOneOrMany(v, collection);
	}

	@SuppressWarnings("unchecked")
	private void addOneOrMany(Object v, Collection<Object> collection) {
		if (v instanceof Collection) {
			collection.addAll((Collection<? extends Object>) v);
		} else if (v != null) {
			collection.add(v);
		}
	}

	public void addSubscription(Object v) {
		addOneOrMany(v, subscriptions);
	}

	public Collection<Object> getSubscriptions() {
		return subscriptions;
	}

	public CaseInstance getProcessInstance() {
		return processInstance;
	}

}
