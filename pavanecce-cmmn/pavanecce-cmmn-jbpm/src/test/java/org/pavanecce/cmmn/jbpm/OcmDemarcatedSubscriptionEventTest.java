package org.pavanecce.cmmn.jbpm;

import org.junit.Test;
import org.pavanecce.cmmn.jbpm.instance.AbstractSubscriptionManager;
import org.pavanecce.cmmn.jbpm.instance.CaseInstance;

public class OcmDemarcatedSubscriptionEventTest extends OcmPersistentSubscriptionEventTest {
	@Override
	protected AbstractSubscriptionManager<?, ?> getSubscriptionManager() {
		return null;
	}
	@Override
	protected void maybeStartSubscription() {
		getPersistence().start();
		AbstractSubscriptionManager.addScopedSubscriptions((CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId()));
	}
	@Override
	protected void endSubscription() {
		AbstractSubscriptionManager.removeScopedSubscriptions();
	}
	@Test
	public void testCreationOfObjectInCollectionFileItem() throws Exception {
		super.testCreationOfObjectInCollectionFileItem();
	}
}
