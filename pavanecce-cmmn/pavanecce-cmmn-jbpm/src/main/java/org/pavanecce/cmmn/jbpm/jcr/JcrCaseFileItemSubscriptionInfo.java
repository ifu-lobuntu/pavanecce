package org.pavanecce.cmmn.jbpm.jcr;

import static org.pavanecce.cmmn.jbpm.jcr.JcrUtil.*;

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
	private String path;
	private String relatedItemName;
	private Node node;

	public JcrCaseFileItemSubscriptionInfo(JcrCaseSubscriptionInfo caseSubscription) {
		super();
		try {
			this.caseSubscription = caseSubscription;
			// TODO the info require is not there yet
			this.node = caseSubscription.getNode().addNode(calculateRelativePath());
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	public JcrCaseFileItemSubscriptionInfo() {
		super();
	}

	public JcrCaseFileItemSubscriptionInfo(Node node) {
		this.node = node;
	}

	public Node getNode() {
		return node;
	}

	public JcrCaseSubscriptionInfo getCaseSubscription() {
		return caseSubscription;
	}

	public String getPath() {
		if (path == null) {
			String string = calculateRelativePath();
			path = caseSubscription.getPath() + string;
		}
		return path;
	}

	private String calculateRelativePath() {
		String string = "/caseFileItemSubscriptions/" + processInstanceId + itemName + transition.name();
		return string;
	}

	public void setPath(String path) {
		this.path = path;
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
}
