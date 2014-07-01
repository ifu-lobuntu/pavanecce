package org.pavanecce.cmmn.jbpm.jcr;

import static org.pavanecce.cmmn.jbpm.jcr.JcrUtil.convertException;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.pavanecce.cmmn.jbpm.event.AbstractCaseSubscriptionInfo;

public class JcrCaseSubscriptionInfo extends AbstractCaseSubscriptionInfo<JcrCaseFileItemSubscriptionInfo> implements NodeAsObject {
	Node subscriptionNode;
	private JcrCaseSubscriptionKey id;
	private String path;
	private Set<JcrCaseFileItemSubscriptionInfo> caseFileItemSubscriptions;

	public JcrCaseSubscriptionKey getId() {
		if (id == null && path != null) {
			id = new JcrCaseSubscriptionKey(path);
		}
		return id;
	}

	public JcrCaseSubscriptionInfo() {
	}

	public JcrCaseSubscriptionInfo(JcrCaseSubscriptionKey id) {
		this.id = id;
		path = "/subscriptions/" + id.toString();
	}

	public void flush() {
		try {
			Node childrenNode = getSubscriptionsNode();
			for (JcrCaseFileItemSubscriptionInfo si : getCaseFileItemSubscriptions()) {
				if (si.getNode() == null) {
					si.setNode(childrenNode.addNode("i:caseFileItemSubscriptions", "i:caseFileItemSubscription"));
				}
				si.flush();
			}
			NodeIterator ni = childrenNode.getNodes("i:caseFileItemSubscriptions");
			while (ni.hasNext()) {
				Node object = ni.nextNode();
				boolean found = false;
				for (JcrCaseFileItemSubscriptionInfo si : getCaseFileItemSubscriptions()) {
					if (si.getNode().getIdentifier().equals(object.getIdentifier())) {
						found = true;
						break;
					}
				}
				if (!found) {
					ni.remove();
				}
			}
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	public JcrCaseSubscriptionInfo(Node subscriptionNode) {
		this.id = new JcrCaseSubscriptionKey(subscriptionNode);
		path = "/subscriptions/" + id.toString();
		this.subscriptionNode = subscriptionNode;
	}

	public Node getNode() {
		return subscriptionNode;
	}

	public String getPath() {
		return path;
	}

	public void setCaseFileItemSubscriptions(Set<JcrCaseFileItemSubscriptionInfo> caseFileItemSubscriptions) {
		this.caseFileItemSubscriptions = caseFileItemSubscriptions;
	}

	@Override
	public Set<? extends JcrCaseFileItemSubscriptionInfo> getCaseFileItemSubscriptions() {
		if (caseFileItemSubscriptions == null) {
			try {
				caseFileItemSubscriptions = new HashSet<JcrCaseFileItemSubscriptionInfo>();
				Node childrenNode = getSubscriptionsNode();
				NodeIterator ni = childrenNode.getNodes("i:caseFileItemSubscriptions");
				while (ni.hasNext()) {
					caseFileItemSubscriptions.add(new JcrCaseFileItemSubscriptionInfo(ni.nextNode()));
				}
			} catch (PathNotFoundException e) {
				e.printStackTrace();
			} catch (RepositoryException e) {
				throw new RuntimeException(e);
			}
		}
		return caseFileItemSubscriptions;
	}

	protected Node getSubscriptionsNode() throws PathNotFoundException, RepositoryException, ItemExistsException, NoSuchNodeTypeException, LockException,
			VersionException, ConstraintViolationException {
		Node childrenNode = null;
		try {
			childrenNode = subscriptionNode.getNode("i:caseFileItemSubscriptions");
		} catch (RepositoryException re) {

		}
		if (childrenNode == null) {
			childrenNode = subscriptionNode.addNode("i:caseFileItemSubscriptions", "i:caseFileItemSubscriptions");
		}
		return childrenNode;
	}

	@Override
	public void addCaseFileItemSubscription(JcrCaseFileItemSubscriptionInfo a) {
		caseFileItemSubscriptions.add(a);
	}

	@Override
	public void removeCaseFileItemSubscription(JcrCaseFileItemSubscriptionInfo a) {
		caseFileItemSubscriptions.remove(a);
	}

	public void setSubscriptionNode(Node subs) {
		this.subscriptionNode = subs;
	}

}
