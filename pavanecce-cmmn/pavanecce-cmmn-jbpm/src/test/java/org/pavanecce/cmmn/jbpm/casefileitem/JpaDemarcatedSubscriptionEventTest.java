package org.pavanecce.cmmn.jbpm.casefileitem;

import org.junit.Test;
import org.pavanecce.cmmn.jbpm.event.AbstractPersistentSubscriptionManager;
import org.pavanecce.cmmn.jbpm.event.DemarcatedSubscriptionContext;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public class JpaDemarcatedSubscriptionEventTest extends JpaPersistentSubscriptionEventTest {
	@Override
	protected AbstractPersistentSubscriptionManager<?, ?> getSubscriptionManager() {
		return null;
	}

	@Override
	protected void maybeStartSubscription() {
		getPersistence().start();
		DemarcatedSubscriptionContext.activateSubscriptionsFrom((CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId()));
	}

	@Override
	protected void endSubscription() {
		DemarcatedSubscriptionContext.deactiveSubscriptions();
	}

	@Test
	public void testCreationOfObjectInCollectionFileItem() throws Exception {
		super.testCreationOfObjectInCollectionFileItem();
	}
}
