package org.pavanecce.cmmn.jbpm.jpa;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.pavanecce.cmmn.jbpm.event.AbstractPersistentSubscriptionManager;
import org.pavanecce.cmmn.jbpm.event.CaseSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.event.CaseSubscriptionKey;
import org.pavanecce.cmmn.jbpm.event.PersistedCaseFileItemSubscriptionInfo;
import org.pavanecce.common.jpa.JpaObjectPersistence;

/**
 * 
 * Needed to coordinate the dispatching of Events with storing the updated subscriptions and flushing of the
 * EntityManager. The plan is to do the dispatching asynchronously eventually. There are still some serious issues to
 * resolve, specifically w.r.t. clearing some static variables and avoiding memory leaks
 * 
 */
public class JbpmJpaObjectPersistence extends JpaObjectPersistence {
	private RuntimeManager runtimeManager;

	public JbpmJpaObjectPersistence(EntityManagerFactory emf, RuntimeManager rm) {
		super(emf);
		this.runtimeManager=rm;
	}

	@Override
	public void commit() {
		try {
			startOrJoinTransaction();
			getEntityManager().flush();
			while (AbstractPersistentSubscriptionManager.dispatchEventQueue(runtimeManager.getRuntimeEngine(EmptyContext.get()))) {
				AbstractPersistentSubscriptionManager.commitSubscriptionsTo(this);
				getEntityManager().flush();
			}
			if (startedTransaction) {
				getTransaction().commit();
				this.startedTransaction = false;
			}
			close();
		} catch (Exception e) {
			throw convertException(e);
		}
	}

}