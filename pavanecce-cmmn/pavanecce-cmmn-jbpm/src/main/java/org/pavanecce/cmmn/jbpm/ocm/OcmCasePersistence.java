package org.pavanecce.cmmn.jbpm.ocm;

import org.pavanecce.common.ocm.OcmFactory;
import org.pavanecce.common.ocm.OcmObjectPersistence;

public class OcmCasePersistence extends OcmObjectPersistence {
	public OcmCasePersistence(OcmFactory factory) {
		super(factory);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(Class<T> class1, Object id) {
		if (id instanceof OcmCaseSubscriptionKey) {
			return (T) getSubscription(((OcmCaseSubscriptionKey) id).getId());
		}
		return (T) getSession().getObjectByUuid((String) id);
	}

	public OcmCaseSubscriptionInfo getSubscription(String id) {
		return (OcmCaseSubscriptionInfo) getSession().getObject("/subscriptions/" + id);
	}
}
