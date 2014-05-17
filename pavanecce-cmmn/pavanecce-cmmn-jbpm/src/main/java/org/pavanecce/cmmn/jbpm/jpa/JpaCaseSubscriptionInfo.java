package org.pavanecce.cmmn.jbpm.jpa;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.pavanecce.cmmn.jbpm.event.AbstractCaseSubscriptionInfo;

@Entity
public class JpaCaseSubscriptionInfo extends AbstractCaseSubscriptionInfo<JpaCaseFileItemSubscriptionInfo> {
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

	@Override
	public Set<? extends JpaCaseFileItemSubscriptionInfo> getCaseFileItemSubscriptions() {
		return caseFileItemSubscriptions;
	}

	@Override
	public void addCaseFileItemSubscription(JpaCaseFileItemSubscriptionInfo a) {
		caseFileItemSubscriptions.add(a);
	}

	@Override
	public void removeCaseFileItemSubscription(JpaCaseFileItemSubscriptionInfo a) {
		caseFileItemSubscriptions.remove(a);
	}

}
