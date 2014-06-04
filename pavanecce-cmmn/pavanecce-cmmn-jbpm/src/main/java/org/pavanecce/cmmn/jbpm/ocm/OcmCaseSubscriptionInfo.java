package org.pavanecce.cmmn.jbpm.ocm;

import java.util.HashSet;
import java.util.Set;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;
import org.pavanecce.cmmn.jbpm.event.AbstractCaseSubscriptionInfo;

@Node(discriminator = false, jcrType = "i:caseSubscriptionInfo")
public class OcmCaseSubscriptionInfo extends AbstractCaseSubscriptionInfo<OcmCaseFileItemSubscriptionInfo> {
	private OcmCaseSubscriptionKey id;
	@Field(path = true)
	private String path;
	@Collection(jcrName = "i:caseFileItemSubscriptions", jcrElementName = "i:caseFileItemSubscriptions")
	private Set<OcmCaseFileItemSubscriptionInfo> caseFileItemSubscriptions = new HashSet<OcmCaseFileItemSubscriptionInfo>();

	public OcmCaseSubscriptionKey getId() {
		if (id == null && path != null) {
			id = new OcmCaseSubscriptionKey(path);
		}
		return id;
	}

	public OcmCaseSubscriptionInfo() {
	}

	public OcmCaseSubscriptionInfo(Object o) {
		this.id = new OcmCaseSubscriptionKey(o);
		path = "/subscriptions/" + id.toString();
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setId(OcmCaseSubscriptionKey id) {
		this.id = id;
	}

	public void setCaseFileItemSubscriptions(Set<OcmCaseFileItemSubscriptionInfo> caseFileItemSubscriptions) {
		this.caseFileItemSubscriptions = caseFileItemSubscriptions;
	}

	@Override
	public Set<? extends OcmCaseFileItemSubscriptionInfo> getCaseFileItemSubscriptions() {
		return caseFileItemSubscriptions;
	}

	@Override
	public void addCaseFileItemSubscription(OcmCaseFileItemSubscriptionInfo a) {
		caseFileItemSubscriptions.add(a);
	}

	@Override
	public void removeCaseFileItemSubscription(OcmCaseFileItemSubscriptionInfo a) {
		caseFileItemSubscriptions.remove(a);
	}

}
