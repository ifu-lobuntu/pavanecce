package org.pavanecce.cmmn.jbpm.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.jbpm.infra.OnPartInstanceSubscription;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public class DemarcatedSubscriptionContext {
	private static ThreadLocal<Map<Long, Set<OnPartInstanceSubscription>>> demarcatedSubscriptions = new ThreadLocal<Map<Long, Set<OnPartInstanceSubscription>>>();

	public static void activateSubscriptionsFrom(CaseInstance theCase) {
		Map<Long, Set<OnPartInstanceSubscription>> map = demarcatedSubscriptions.get();
		if (map == null) {
			demarcatedSubscriptions.set(map = new HashMap<Long, Set<OnPartInstanceSubscription>>());
		}
		map.put(theCase.getId(), theCase.findOnPartInstanceSubscriptions());
	}

	public static Set<OnPartInstanceSubscription> getSubscriptionsInScopeForFor(Object source, CaseFileItemTransition... transitions) {
		if (demarcatedSubscriptions.get() != null) {
			Set<OnPartInstanceSubscription> result = new HashSet<OnPartInstanceSubscription>();
			for (Entry<Long, Set<OnPartInstanceSubscription>> entry : demarcatedSubscriptions.get().entrySet()) {
				for (CaseFileItemTransition t : transitions) {
					for (OnPartInstanceSubscription opis : entry.getValue()) {
						if (opis.isListeningTo(source, t)) {
							result.add(opis);
						}
					}
				}
			}
			return result;
		}
		return Collections.emptySet();
	}

	public static void deactiveSubscriptions() {
		// TODO do by processInstance
		demarcatedSubscriptions.set(null);
	}

}
