package org.pavanecce.cmmn.jbpm.jcr;

import static org.pavanecce.cmmn.jbpm.jcr.JcrUtil.*;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;

import org.pavanecce.cmmn.jbpm.event.AbstractCaseSubscriptionInfo;

public class JcrCaseSubscriptionInfo extends AbstractCaseSubscriptionInfo<JcrCaseFileItemSubscriptionInfo> implements NodeAsObject {
	Node node;
	private JcrCaseSubscriptionKey id;
	private String path;
	private Set<JcrCaseFileItemSubscriptionInfo> caseFileItemSubscriptions = new HashSet<JcrCaseFileItemSubscriptionInfo>();

	public JcrCaseSubscriptionKey getId() {
		if (id == null && path != null) {
			id = new JcrCaseSubscriptionKey(path);
		}
		return id;
	}

	public JcrCaseSubscriptionInfo(Node node) {
		this.node = node;
	}

	public JcrCaseSubscriptionInfo() {
	}

	public JcrCaseSubscriptionInfo(Object o) {
		this.id = new JcrCaseSubscriptionKey(o);
		path = "/subscriptions/" + id.toString();
	}

	public Node getNode() {
		return node;
	}

	public String getPath() {
		try {
			return node.getPath();
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setId(JcrCaseSubscriptionKey id) {
		this.id = id;
	}

	public void setCaseFileItemSubscriptions(Set<JcrCaseFileItemSubscriptionInfo> caseFileItemSubscriptions) {
		this.caseFileItemSubscriptions = caseFileItemSubscriptions;
	}

	@Override
	public Set<? extends JcrCaseFileItemSubscriptionInfo> getCaseFileItemSubscriptions() {
		return caseFileItemSubscriptions;
	}

	@Override
	public void addCaseFileItemSubscription(JcrCaseFileItemSubscriptionInfo a) {
		caseFileItemSubscriptions.add(a);
	}

	@Override
	public void removeCaseFileItemSubscription(JcrCaseFileItemSubscriptionInfo a) {
		caseFileItemSubscriptions.remove(a);
	}

}
