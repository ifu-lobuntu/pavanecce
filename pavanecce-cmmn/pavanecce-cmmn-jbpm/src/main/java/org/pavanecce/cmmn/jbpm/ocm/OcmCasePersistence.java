package org.pavanecce.cmmn.jbpm.ocm;

import javax.jcr.Node;

import org.apache.jackrabbit.ocm.mapper.model.ClassDescriptor;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.pavanecce.cmmn.jbpm.event.AbstractPersistentSubscriptionManager;
import org.pavanecce.common.ocm.OcmFactory;
import org.pavanecce.common.ocm.OcmObjectPersistence;

public class OcmCasePersistence extends OcmObjectPersistence {
	RuntimeManager runtimeManager;
	public OcmCasePersistence(OcmFactory factory, RuntimeManager runtimeManager) {
		super(factory);
		this.runtimeManager=runtimeManager;
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
		try {
			startTransaction();
			getSession().save();
			doCaseFileItemEvents();
			if (startedTransaction) {
				getTransaction().commit();
				boolean workItemsProcessed=false;
				do {
					getTransaction().begin();
					workItemsProcessed = AbstractPersistentSubscriptionManager.dispatchWorkItemQueue(runtimeManager.getRuntimeEngine(EmptyContext.get()));
					if (workItemsProcessed) {
						doCaseFileItemEvents();
					}
					getTransaction().commit();
				} while (workItemsProcessed);
			}
			startedTransaction = false;
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	private void doCaseFileItemEvents() {
		while (AbstractPersistentSubscriptionManager.dispatchEventQueue(runtimeManager.getRuntimeEngine(EmptyContext.get()))) {
			AbstractPersistentSubscriptionManager.commitSubscriptionsTo(this);
			getSession().save();
		}
		if(AbstractPersistentSubscriptionManager.commitSubscriptionsTo(this)){
			getSession().save();
		}
	}

	protected OcmCaseSubscriptionInfo getSubscription(String className, String id) {
		return (OcmCaseSubscriptionInfo) getSession().getObject("/subscriptions/" + className + "$" + id);
	}
}
