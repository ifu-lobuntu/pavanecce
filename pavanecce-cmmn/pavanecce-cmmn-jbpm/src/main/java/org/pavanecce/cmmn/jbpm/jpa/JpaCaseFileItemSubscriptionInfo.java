package org.pavanecce.cmmn.jbpm.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.pavanecce.cmmn.jbpm.event.AbstractCaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.event.CaseSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.event.PersistedCaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

@Entity
public class JpaCaseFileItemSubscriptionInfo extends AbstractCaseFileItemSubscriptionInfo implements PersistedCaseFileItemSubscriptionInfo {
	@Id
	@GeneratedValue
	private Long id;
	@ManyToOne
	private JpaCaseSubscriptionInfo caseSubscription;
	private String itemName;
	private String relatedItemName;
	private CaseFileItemTransition transition;
	private long processInstanceId;
	private String caseKey;

	public JpaCaseFileItemSubscriptionInfo() {
		super();
	}

	public JpaCaseFileItemSubscriptionInfo(JpaCaseSubscriptionInfo caseSubscription, String itemName, CaseFileItemTransition transition, CaseInstance instance) {
		super();

	}

	@Override
	public String getRelatedItemName() {
		return relatedItemName;
	}

	@Override
	public void setRelatedItemName(String relatedItemName) {
		this.relatedItemName = relatedItemName;
	}

	public JpaCaseSubscriptionInfo getCaseSubscription() {
		return caseSubscription;
	}

	@Override
	public void setCaseSubscription(CaseSubscriptionInfo<?> caseSubscription) {
		this.caseSubscription = (JpaCaseSubscriptionInfo) caseSubscription;
	}

	@Override
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	@Override
	public void setTransition(CaseFileItemTransition transition) {
		this.transition = transition;
	}

	@Override
	public void setCaseKey(String caseKey) {
		this.caseKey = caseKey;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String getCaseKey() {
		return caseKey;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getItemName() {
		return itemName;
	}

	@Override
	public CaseFileItemTransition getTransition() {
		return transition;
	}

	@Override
	public String toString() {
		return itemName + "[" + transition + "]";
	}

	@Override
	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getIdentifier() {
		return caseSubscription.getId().getClassName() + caseSubscription.getId().getId() + super.getIdentifier();
	}
}
