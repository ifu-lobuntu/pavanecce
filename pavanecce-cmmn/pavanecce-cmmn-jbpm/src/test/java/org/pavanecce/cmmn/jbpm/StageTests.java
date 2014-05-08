package org.pavanecce.cmmn.jbpm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.shared.services.impl.events.JbpmServicesEventListener;
import org.junit.Test;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.EventService;
import org.kie.internal.task.api.model.NotificationEvent;
import org.pavanecce.cmmn.jbpm.instance.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.CaseTaskLifecycleListener;
import org.pavanecce.cmmn.jbpm.instance.CaseTaskWorkItemHandler;

import test.ConstructionCase;
import test.House;
import test.HousePlan;
import test.WallPlan;

public class StageTests extends AbstractConstructionTestCase {
	{
		super.isJpa = true;
	}
	protected HousePlan housePlan;
	protected House house;
	private CaseInstance caseInstance;

	public StageTests() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void testStageTriggered() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		assertNodeTriggered(caseInstance.getId(), "TopLevelTask");
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1, list.size());
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		// *****WHEN
		getRuntimeEngine().getTaskService().complete(list.get(0).getId(), "Builder", new HashMap<String,Object>());
		// *****THEN
		assertNodeTriggered(caseInstance.getId(), "TheStagePlanItem");
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
		params.put("housePlan", Arrays.asList(housePlan));
		params.put("house", Arrays.asList(house));
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
