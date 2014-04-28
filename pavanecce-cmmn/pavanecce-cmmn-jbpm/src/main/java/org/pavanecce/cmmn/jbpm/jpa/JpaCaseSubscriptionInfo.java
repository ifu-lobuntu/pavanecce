package org.pavanecce.cmmn.jbpm.jpa;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.pavanecce.cmmn.jbpm.instance.CaseSubscriptionInfo;

@Entity
public class JpaCaseSubscriptionInfo implements CaseSubscriptionInfo<JpaCaseFileItemSubscriptionInfo> {
	@EmbeddedId()
	private JpaCaseSubscriptionKey id;
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<JpaCaseFileItemSubscriptionInfo> caseFileItemSubscriptions = new HashSet<JpaCaseFileItemSubscriptionInfo>();
	@Transient
	private Map<String, Set<JpaCaseFileItemSubscriptionInfo>> caseFileItemSubscriptionsByName;

	public JpaCaseSubscriptionKey getId() {
		return id;
	}

	public JpaCaseSubscriptionInfo() {
	}

	public JpaCaseSubscriptionInfo(Object o) {
		this.id = new JpaCaseSubscriptionKey(o);
	}

	public Set<? extends JpaCaseFileItemSubscriptionInfo> getCaseFileItemSubscriptions() {
		return caseFileItemSubscriptions;
	}

	public void addCaseFileItemSubscription(JpaCaseFileItemSubscriptionInfo a) {
		caseFileItemSubscriptions.add(a);
	}

	public void removeCaseFileItemSubscription(JpaCaseFileItemSubscriptionInfo a) {
		caseFileItemSubscriptions.remove(a);
	}

}
