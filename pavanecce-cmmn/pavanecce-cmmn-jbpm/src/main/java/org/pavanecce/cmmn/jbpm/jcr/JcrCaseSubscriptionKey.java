package org.pavanecce.cmmn.jbpm.jcr;

import static org.pavanecce.cmmn.jbpm.jcr.JcrUtil.convertException;

import java.io.Serializable;

import javax.jcr.Node;

import org.pavanecce.cmmn.jbpm.event.CaseSubscriptionKey;

public class JcrCaseSubscriptionKey implements Serializable, CaseSubscriptionKey {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2689025545130505083L;
	private String nodeTypeName;
	private String id;

	public JcrCaseSubscriptionKey(String path) {
		String[] split = path.substring("/subscriptions/".length()).split("\\$");
		nodeTypeName = split[0];
		id = split[1];
	}

	public JcrCaseSubscriptionKey() {

	}

	public JcrCaseSubscriptionKey(Node node) {
		try {
			this.id = node.getIdentifier();
			this.nodeTypeName = node.getPrimaryNodeType().getName();
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	public String getClassName() {
		return nodeTypeName;
	}

	public String getId() {
		return id;
	}

	public String toString() {
		return nodeTypeName + "$" + id;
	}

	@Override
	public int hashCode() {
		return nodeTypeName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof JcrCaseSubscriptionKey) {
			JcrCaseSubscriptionKey other = (JcrCaseSubscriptionKey) obj;
			return other.nodeTypeName.equals(nodeTypeName) && other.id.equals(id);
		}
		return false;
	}
}
