package org.pavanecce.cmmn.jbpm.ocm;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.ocm.exception.ObjectContentManagerException;
import org.apache.jackrabbit.ocm.mapper.model.ClassDescriptor;
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
			OcmCaseSubscriptionKey key = (OcmCaseSubscriptionKey) id;
			return (T) getSubscription(key.getClassName(), key.getId());
		}
		return (T) getSession().getObjectByUuid((String) id);
	}

	public OcmCaseSubscriptionKey getSubscription(Node node) {
		try {
			ClassDescriptor classDescriptor = getClassDescriptor(node.getPrimaryNodeType().getName());
			return new OcmCaseSubscriptionKey("/subscriptions/" + classDescriptor.getClassName() + "$" + node.getIdentifier());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}
	}

	@Override
	public void commit() {
		super.commit();
	}

	protected OcmCaseSubscriptionInfo getSubscription(String className, String id) {
		return (OcmCaseSubscriptionInfo) getSession().getObject("/subscriptions/" + className + "$" + id);
	}
}
