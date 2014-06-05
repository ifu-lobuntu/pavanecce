package org.pavanecce.cmmn.jbpm.jcr;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.pavanecce.cmmn.jbpm.event.AbstractPersistentSubscriptionManager;
import org.pavanecce.common.jcr.JcrSessionFactory;

public class JcrCasePersistence extends JcrObjectPersistence {
	RuntimeManager runtimeManager;

	public JcrCasePersistence(JcrSessionFactory factory, RuntimeManager runtimeManager) {
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
			return (T) getCurrentSession().getNodeByIdentifier((String) id);
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
	public void start() {
		super.start();
		super.factory.updateEventListener();
	}

	@Override
	public void commit() {
		try {
			startTransaction();
			getCurrentSession().save();
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
				getCurrentSession().save();
			} catch (Exception e) {
				throw convertException(e);
			}
		}
		if (AbstractPersistentSubscriptionManager.commitSubscriptionsTo(this)) {
			try {
				getCurrentSession().save();
			} catch (Exception e) {
				throw convertException(e);
			}
		}
	}

	protected JcrCaseSubscriptionInfo getSubscription(String className, String id) {
		try {
			Node node = getCurrentSession().getRootNode().getNode("subscriptions/" + className + "$" + id);
			return new JcrCaseSubscriptionInfo(node);
		} catch (PathNotFoundException e) {
			return null;
		} catch (Exception e) {
			throw convertException(e);
		}
	}
}
