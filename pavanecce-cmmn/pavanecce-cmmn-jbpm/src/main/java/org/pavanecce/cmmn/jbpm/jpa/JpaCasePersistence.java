package org.pavanecce.cmmn.jbpm.jpa;

import javax.persistence.EntityManagerFactory;

import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.pavanecce.cmmn.jbpm.event.AbstractPersistentSubscriptionManager;
import org.pavanecce.common.jpa.JpaObjectPersistence;

/**
 * 
 * Needed to coordinate the dispatching of Events with storing the updated subscriptions and flushing of the
 * EntityManager. The plan is to do the dispatching asynchronously eventually. There are still some serious issues to
 * resolve, specifically w.r.t. clearing some static variables and avoiding memory leaks
 * 
 */
public class JpaCasePersistence extends JpaObjectPersistence {
	private RuntimeManager runtimeManager;

	public JpaCasePersistence(EntityManagerFactory emf, RuntimeManager rm) {
		super(emf);
		this.runtimeManager = rm;
	}

	@Override
	public void commit() {
		try {
			startOrJoinTransaction();
			getEntityManager().flush();
			doCaseFileItemEvents();
			if (startedTransaction) {
				getTransaction().commit();
				boolean workItemsProcessed = false;
				do {
					this.startedTransaction = false;
					startOrJoinTransaction();
					workItemsProcessed = AbstractPersistentSubscriptionManager.dispatchWorkItemQueue(runtimeManager.getRuntimeEngine(EmptyContext.get()));
					if (workItemsProcessed) {
						doCaseFileItemEvents();
					}
					getTransaction().commit();
				} while (workItemsProcessed);
				this.startedTransaction = false;
			}
			close();
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	private void doCaseFileItemEvents() {
		while (AbstractPersistentSubscriptionManager.dispatchEventQueue(runtimeManager.getRuntimeEngine(EmptyContext.get()))) {
			AbstractPersistentSubscriptionManager.commitSubscriptionsTo(this);
			getEntityManager().flush();
		}
		if (AbstractPersistentSubscriptionManager.commitSubscriptionsTo(this)) {
			getEntityManager().flush();
		}
	}

}