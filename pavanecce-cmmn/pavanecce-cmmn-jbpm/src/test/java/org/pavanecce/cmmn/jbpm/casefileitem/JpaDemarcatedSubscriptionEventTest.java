package org.pavanecce.cmmn.jbpm.casefileitem;

import org.junit.Test;
import org.pavanecce.cmmn.jbpm.instance.AbstractPersistentSubscriptionManager;
import org.pavanecce.cmmn.jbpm.instance.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.DemarcatedSubscriptionContent;

public class JpaDemarcatedSubscriptionEventTest extends JpaPersistentSubscriptionEventTest {
	@Override
	protected AbstractPersistentSubscriptionManager<?, ?> getSubscriptionManager() {
		return null;
	}
	@Override
	protected void maybeStartSubscription() {
		getPersistence().start();
		DemarcatedSubscriptionContent.activateSubscriptionsFrom((CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId()));
	}
	@Override
	protected void endSubscription() {
		DemarcatedSubscriptionContent.deactiveSubscriptions();
	}
	@Test
	public void testCreationOfObjectInCollectionFileItem() throws Exception {
		super.testCreationOfObjectInCollectionFileItem();
	}
}
