package org.pavanecce.cmmn.ocm;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.pavanecce.cmmn.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.instance.CaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.instance.CaseSubscriptionInfo;

@Entity
public class OcmCaseFileItemSubscriptionInfo implements CaseFileItemSubscriptionInfo{
	@Id
	@GeneratedValue
	private Long id;
	@ManyToOne
	private OcmCaseSubscriptionInfo caseSubscription;
	private String itemName;
	private CaseFileItemTransition transition;
	private long processId;
	private String caseKey;

	public OcmCaseFileItemSubscriptionInfo() {
		super();
	}



	@Override
	public OcmCaseSubscriptionInfo getCaseSubscription() {
		return caseSubscription;
	}

	@Override
	public void setCaseSubscription(CaseSubscriptionInfo<?> caseSubscription) {
		this.caseSubscription = (OcmCaseSubscriptionInfo) caseSubscription;
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
	public void setProcessId(long processId) {
		this.processId = processId;
	}

	@Override
	public void setCaseKey(String caseKey) {
		this.caseKey = caseKey;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getCaseKey() {
		return caseKey;
	}

	@Override
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
	public long getProcessId() {
		return processId;
	}

}
