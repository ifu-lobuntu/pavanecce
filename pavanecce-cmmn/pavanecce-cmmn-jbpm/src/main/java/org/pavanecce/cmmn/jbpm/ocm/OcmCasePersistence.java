package org.pavanecce.cmmn.jbpm.ocm;

import javax.jcr.Node;

import org.apache.jackrabbit.ocm.mapper.model.ClassDescriptor;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.pavanecce.cmmn.jbpm.event.AbstractPersistentSubscriptionManager;
import org.pavanecce.common.ocm.ObjectContentManagerFactory;
import org.pavanecce.common.ocm.OcmObjectPersistence;

public class OcmCasePersistence extends OcmObjectPersistence {
	RuntimeManager runtimeManager;

	public OcmCasePersistence(ObjectContentManagerFactory factory, RuntimeManager runtimeManager) {
		super(factory);
		this.runtimeManager = runtimeManager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(Class<T> class1, Object id) {
		if (id instanceof OcmCaseSubscriptionKey) {
			OcmCaseSubscriptionKey key = (OcmCaseSubscriptionKey) id;
			return (T) getSubscription(key.getClassName(), key.getId());
		}
		return (T) getObjectContentManager().getObjectByUuid((String) id);
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
	public void start() {
		super.start();
		super.factory.updateEventListener();
	}

	@Override
	public void commit() {
		try {
			startTransaction();
			getObjectContentManager().save();
			doCaseFileItemEvents();
			if (startedTransaction) {
				getTransaction().commit();
				boolean workItemsProcessed = false;
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
			getObjectContentManager().save();
		}
		if (AbstractPersistentSubscriptionManager.commitSubscriptionsTo(this)) {
			getObjectContentManager().save();
		}
	}

	protected OcmCaseSubscriptionInfo getSubscription(String className, String id) {
		return (OcmCaseSubscriptionInfo) getObjectContentManager().getObject("/subscriptions/" + className + "$" + id);
	}
}
