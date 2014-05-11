package org.pavanecce.cmmn.jbpm.planitem;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.pavanecce.cmmn.jbpm.AbstractConstructionTestCase;
import org.pavanecce.cmmn.jbpm.instance.CaseInstance;

import test.ConstructionCase;
import test.House;
import test.HousePlan;

public class MilestoneTest extends AbstractConstructionTestCase {
	protected HousePlan housePlan;
	protected House house;
	private CaseInstance caseInstance;

	public MilestoneTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void testOccurrence() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN

		triggerStartOfMilestone();
		// *****THEN
		assertNodeTriggered(caseInstance.getId(), "TheMilestonePlanItem");
		assertNodeTriggered(caseInstance.getId(), "TheTask");

	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager("test/MilestoneTests.cmmn");
		Map<String, Object> params = new HashMap<String, Object>();
		getPersistence().start();

		ConstructionCase cc = new ConstructionCase("/cases/case1");
		housePlan = new HousePlan(cc);
		house = new House(cc);
		getPersistence().persist(cc);
		getPersistence().commit();
		params.put("housePlan", housePlan);
		params.put("house", house);
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess("MilestoneTests", params);
		getPersistence().commit();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		assertNodeTriggered(caseInstance.getId(), "defaultSplit");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "onTheUserEventOccurPartId");
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "onTheMilestoneOccurPartId");
		getPersistence().commit();
	}

	private void triggerStartOfMilestone() throws Exception {
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		caseInstance.signalEvent("TheUserEvent", new Object());
		getPersistence().commit();
	}

}
