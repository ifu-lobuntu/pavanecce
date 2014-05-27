package org.pavanecce.cmmn.jbpm.jcr;

import javax.jcr.Node;

import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.pavanecce.cmmn.jbpm.event.AbstractPersistentSubscriptionManager;

public class JcrCasePersistence extends JcrObjectPersistence {
	RuntimeManager runtimeManager;

	public JcrCasePersistence(JcrObjectPersistenceFactory factory, RuntimeManager runtimeManager) {
		super(factory);
		this.runtimeManager = runtimeManager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(Class<T> class1, Object id) {

		try {
			if (id instanceof JcrCaseSubscriptionKey) {
				JcrCaseSubscriptionKey key = (JcrCaseSubscriptionKey) id;
				return (T) getSubscription(key.getClassName(), key.getId());
			}
			return (T) getObjectContentManager().getNodeByIdentifier((String) id);
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	public JcrCaseSubscriptionKey getSubscription(Node node) {
		try {
			return new JcrCaseSubscriptionKey("/subscriptions/" + node.getPrimaryNodeType().getName() + "$" + node.getIdentifier());
		} catch (Exception e) {
			return null;
		}
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
			try {
				getObjectContentManager().save();
			} catch (Exception e) {
				throw convertException(e);
			}
		}
		if (AbstractPersistentSubscriptionManager.commitSubscriptionsTo(this)) {
			try {
				getObjectContentManager().save();
			} catch (Exception e) {
				throw convertException(e);
			}
		}
	}

	protected JcrCaseSubscriptionInfo getSubscription(String className, String id) {
		try {
			return (JcrCaseSubscriptionInfo) getObjectContentManager().getRootNode().getNode("/subscriptions/" + className + "$" + id);
		} catch (Exception e) {
			throw convertException(e);
		}
	}
}
