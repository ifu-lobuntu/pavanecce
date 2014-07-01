package org.pavanecce.cmmn.jbpm.jcr;

import static org.pavanecce.cmmn.jbpm.jcr.JcrUtil.convertException;

import javax.jcr.Node;

import org.pavanecce.cmmn.jbpm.event.AbstractCaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.event.CaseSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.event.PersistedCaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;

public class JcrCaseFileItemSubscriptionInfo extends AbstractCaseFileItemSubscriptionInfo implements PersistedCaseFileItemSubscriptionInfo, NodeAsObject {
	private String id;
	private JcrCaseSubscriptionInfo caseSubscription;
	private String itemName;
	private CaseFileItemTransition transition;
	private long processInstanceId;
	private String caseKey;
	private String relatedItemName;
	private Node node;

	public JcrCaseFileItemSubscriptionInfo(Node node) {
		this.node = node;
		try {
			this.id = node.getIdentifier();
			this.itemName = node.getProperty("i:itemName").getString();
			this.transition = CaseFileItemTransition.valueOf(node.getProperty("i:transition").getString());
			this.processInstanceId = node.getProperty("i:processInstanceId").getLong();
			this.caseKey = node.getProperty("i:caseKey").getString();
			if (node.hasProperty("i:relatedItemName")) {
				this.relatedItemName = node.getProperty("i:relatedItemName").getString();
			}
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	public JcrCaseFileItemSubscriptionInfo() {
	}

	public void flush() {
		try {
			node.setProperty("i:itemName", this.itemName);
			node.setProperty("i:transition", this.transition.name());
			node.setProperty("i:processInstanceId", this.processInstanceId);
			node.setProperty("i:caseKey", this.caseKey);
			node.setProperty("i:relatedItemName", this.relatedItemName);
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	public Node getNode() {
		return node;
	}

	public JcrCaseSubscriptionInfo getCaseSubscription() {
		return caseSubscription;
	}

	@Override
	public void setCaseSubscription(CaseSubscriptionInfo<?> caseSubscription) {
		this.caseSubscription = (JcrCaseSubscriptionInfo) caseSubscription;
	}

	public void setCaseSubscription(JcrCaseSubscriptionInfo caseSubscription) {
		this.caseSubscription = caseSubscription;
	}

	@Override
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	@Override
	public void setTransition(CaseFileItemTransition transition) {
		this.transition = transition;
	}

	public void setProcessInstanceId(long processId) {
		this.processInstanceId = processId;
	}

	@Override
	public void setCaseKey(String caseKey) {
		this.caseKey = caseKey;
	}

	public String getId() {
		return id;
	}

	@Override
	public String getCaseKey() {
		return caseKey;
	}

	public void setId(String id) {
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
	public long getProcessInstanceId() {
		return processInstanceId;
	}

	@Override
	public String getRelatedItemName() {
		return relatedItemName;
	}

	@Override
	public void setRelatedItemName(String relatedItemName) {
		this.relatedItemName = relatedItemName;
	}

	public String getIdentifier() {
		return getCaseSubscription().getId().getClassName() + getCaseSubscription().getId().getId() + super.getIdentifier();
	}

	public void setNode(Node addNode) {
		this.node = addNode;
	}
}
