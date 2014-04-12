package org.pavanecce.cmmn.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.pavanecce.cmmn.flow.Case;
import org.pavanecce.cmmn.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.instance.CaseInstance;

@Entity
public class CaseFileItemSubscriptionInfo {
	@Id
	@GeneratedValue
	private Long id;
	@ManyToOne
	private CaseSubscriptionInfo caseSubscription;
	private String itemName;
	private CaseFileItemTransition transition;
	private long processId;
	private String caseKey;

	public CaseFileItemSubscriptionInfo() {
		super();
	}

	public CaseFileItemSubscriptionInfo(CaseSubscriptionInfo caseSubscription, String itemName, CaseFileItemTransition transition, CaseInstance instance) {
		super();
		this.caseSubscription = caseSubscription;
		this.caseSubscription.getCaseFileItemSubscriptions().add(this);
		this.itemName = itemName;
		this.transition = transition;
		this.processId = instance.getId();
		this.caseKey = ((Case) instance.getProcess()).getCaseKey();
	}


	public Long getId() {
		return id;
	}

	public String getCaseKey() {
		return caseKey;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getItemName() {
		return itemName;
	}

	public CaseFileItemTransition getTransition() {
		return transition;
	}

	public long getProcessId() {
		return processId;
	}

}
