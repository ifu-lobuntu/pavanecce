package org.pavanecce.cmmn.ocm;

import java.util.HashSet;
import java.util.Set;

import org.pavanecce.cmmn.instance.CaseSubscriptionInfo;

public class OcmCaseSubscriptionInfo implements CaseSubscriptionInfo<OcmCaseFileItemSubscriptionInfo> {
	private OcmCaseSubscriptionKey id;
	private Set<OcmCaseFileItemSubscriptionInfo> caseFileItemSubscriptions = new HashSet<OcmCaseFileItemSubscriptionInfo>();

	public OcmCaseSubscriptionKey getId() {
		return id;
	}

	public OcmCaseSubscriptionInfo() {
	}

	public OcmCaseSubscriptionInfo(Object o) {
		this.id = new OcmCaseSubscriptionKey(o);
	}

	public Set<? extends OcmCaseFileItemSubscriptionInfo> getCaseFileItemSubscriptions() {
		return caseFileItemSubscriptions;
	}

	public void addCaseFileItemSubscription(OcmCaseFileItemSubscriptionInfo a) {
		caseFileItemSubscriptions.add(a);
	}

	public void removeCaseFileItemSubscription(OcmCaseFileItemSubscriptionInfo a) {
		caseFileItemSubscriptions.remove(a);
	}

}
