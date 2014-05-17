package org.pavanecce.cmmn.jbpm.planitem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.AbstractConstructionTestCase;
import org.pavanecce.cmmn.jbpm.instance.impl.CaseInstance;

import test.ConstructionCase;
import test.House;
import test.HousePlan;
import test.WallPlan;

public class StageTest extends AbstractConstructionTestCase {
	{
		super.isJpa = true;
	}
	protected HousePlan housePlan;
	protected House house;
	private CaseInstance caseInstance;

	public StageTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void testStageTriggered() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		assertNodeTriggered(caseInstance.getId(), "TopLevelTask");
		List<TaskSummary> list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());
		getTaskService().start(list.get(0).getId(), "Builder");
		
		// *****WHEN
		getTaskService().complete(list.get(0).getId(), "Builder", new HashMap<String,Object>());
		// *****THEN
		assertNodeTriggered(caseInstance.getId(), "TheStagePlanItem");
		list = getTaskService().getTasksAssignedAsPotentialOwner("Administrator", "en-UK");
		assertEquals(2, list.size());//Tasks representing Stage and the Case
		assertTaskTypeCreated(list, "TheStagePlanItem");
	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager("test/StageTests.cmmn");
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
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess("StageTests", params);
		getPersistence().commit();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		assertNodeTriggered(caseInstance.getId(), "defaultSplit");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "onWallPlanCreatedPartId");
		getPersistence().commit();
	}

	private void triggerStartOfTask() throws Exception {
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new WallPlan(housePlan);
		getPersistence().update(housePlan);
		getPersistence().commit();
	}


}
