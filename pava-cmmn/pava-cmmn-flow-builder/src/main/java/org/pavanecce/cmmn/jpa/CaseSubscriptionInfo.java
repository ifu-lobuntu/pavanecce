package org.pavanecce.cmmn.jpa;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class CaseSubscriptionInfo {
	@EmbeddedId()
	private CaseSubscriptionKey id;
	@OneToMany(cascade=CascadeType.ALL,orphanRemoval=true)
	private Set<CaseFileItemSubscriptionInfo> caseFileItemSubscriptions=new HashSet<CaseFileItemSubscriptionInfo>();
	@Transient
	private Map<String, Set<CaseFileItemSubscriptionInfo>> caseFileItemSubscriptionsByName;
	public CaseSubscriptionKey getId() {
		return id;
	}

	public CaseSubscriptionInfo() {
	}

	public CaseSubscriptionInfo(Object o) {
		this.id=new CaseSubscriptionKey(o);
	}
	public Set<CaseFileItemSubscriptionInfo> getCaseFileItemSubscriptions() {
		return caseFileItemSubscriptions;
	}
	public void addCaseFileItemSubscription(CaseFileItemSubscriptionInfo a){
		caseFileItemSubscriptions.add(a);
	}
	public void removeCaseFileItemSubscription(CaseFileItemSubscriptionInfo a){
		caseFileItemSubscriptions.remove(a);
	}

}
