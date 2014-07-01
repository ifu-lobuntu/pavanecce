package org.pavanecce.cmmn.jbpm.occurrable;

import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public class UserEventListenerTest extends AbstractOccurrableTestCase {

	public UserEventListenerTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	public String getCaseName() {
		return "UserEventListenerTests";
	}

	public String getProcessFile() {
		return "test/occurrable/UserEventListenerTests.cmmn";
	}

	@Override
	protected void triggerOccurrence() throws Exception {
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		caseInstance.signalEvent("TheUserEvent", new Object());
		getPersistence().commit();
	}

}
