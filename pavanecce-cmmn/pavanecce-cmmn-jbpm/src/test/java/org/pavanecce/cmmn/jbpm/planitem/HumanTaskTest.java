package org.pavanecce.cmmn.jbpm.planitem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.AbstractConstructionTestCase;
import org.pavanecce.cmmn.jbpm.instance.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.PlanItemState;

import test.ConstructionCase;
import test.House;
import test.HousePlan;
import test.WallPlan;

public class HumanTaskTest extends AbstractConstructionTestCase {
	{
		super.isJpa = true;
	}
	protected HousePlan housePlan;
	protected House house;
	private CaseInstance caseInstance;

	public HumanTaskTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void testTaskLifecycleComplete() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		// *******THEN
		assertEquals(1, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.ENABLED);
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.ACTIVE);
		getRuntimeEngine().getTaskService().suspend(list.get(0).getId(), "Builder");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.SUSPENDED);
		getRuntimeEngine().getTaskService().resume(list.get(0).getId(), "Builder");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.ACTIVE);
		getRuntimeEngine().getTaskService().complete(list.get(0).getId(), "Builder", new HashMap<String, Object>());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.COMPLETED);
		// *****THEN
	}

	@Test
	public void testTaskLifecycleTerminate() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		// *******THEN
		assertEquals(1, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.ENABLED);
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.ACTIVE);
		getRuntimeEngine().getTaskService().suspend(list.get(0).getId(), "Builder");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.SUSPENDED);
		getRuntimeEngine().getTaskService().resume(list.get(0).getId(), "Builder");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.ACTIVE);
		getRuntimeEngine().getTaskService().exit(list.get(0).getId(), "Builder");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.TERMINATED);
		// *****THEN
	}

	@Test
	public void testTaskLifecycleFailed() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		// *******THEN
		assertEquals(1, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.ENABLED);
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.ACTIVE);
		getRuntimeEngine().getTaskService().suspend(list.get(0).getId(), "Builder");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.SUSPENDED);
		getRuntimeEngine().getTaskService().resume(list.get(0).getId(), "Builder");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.ACTIVE);
		getRuntimeEngine().getTaskService().fail(list.get(0).getId(), "Builder", new HashMap<String, Object>());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.FAILED);
		// *****THEN
	}

	@Test
	public void testTaskLifecycleExit() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		// *******THEN
		assertEquals(1, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.ENABLED);
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.ACTIVE);
		getRuntimeEngine().getTaskService().suspend(list.get(0).getId(), "Builder");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.SUSPENDED);
		getRuntimeEngine().getTaskService().resume(list.get(0).getId(), "Builder");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.ACTIVE);
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("TheUserEvent", new Object(), caseInstance.getId());
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.TERMINATED);
	}

	@Test
	public void testEventGeneratedOnCompletionOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		assertNodeTriggered(caseInstance.getId(), "TheEventGeneratingTaskPlanItem");
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		getRuntimeEngine().getTaskService().complete(list.get(0).getId(), "Builder", new HashMap<String, Object>());
		// *****THEN
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskCompleted");
		assertPlanItemInState(caseInstance.getId(), "PlanItemEnteredWhenTaskCompleted", PlanItemState.ENABLED);
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());
		assertEquals("PlanItemEnteredWhenTaskCompleted", list.get(0).getName());
	}

	@Test
	public void testEventGeneratedOnFaultOnTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		assertNodeTriggered(caseInstance.getId(), "TheEventGeneratingTaskPlanItem");
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		getRuntimeEngine().getTaskService().fail(list.get(0).getId(), "Builder", new HashMap<String, Object>());
		// *****THEN
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskFaultOccurred");
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());
		assertEquals("PlanItemEnteredWhenTaskFaultOccurred", list.get(0).getName());
	}

	@Test
	public void testEventGeneratedOnSuspensionOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		// *****WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		getRuntimeEngine().getTaskService().suspend(list.get(0).getId(), "Builder");
		// *****THEN
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());

		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskSuspended");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskSuspended");
	}

	@Test
	public void testEventGeneratedOnTerminationOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		// *****WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		getRuntimeEngine().getTaskService().exit(list.get(0).getId(), "Builder");
		// *****THEN
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());

		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskTerminated");
		assertEquals("PlanItemEnteredWhenTaskTerminated", list.get(0).getName());
	}

	@Test
	public void testEventGeneratedOnResumptionOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		// *****WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		getRuntimeEngine().getTaskService().suspend(list.get(0).getId(), "Builder");
		getRuntimeEngine().getTaskService().resume(list.get(0).getId(), "Builder");
		// *****THEN
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(3, list.size());

		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskResumed");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskResumed");
	}

	@Test
	public void testEventGeneratedOnExitOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		triggerExitOfTask(list);
		// *****THEN
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskExited");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskExited");
	}

	private void triggerExitOfTask(List<TaskSummary> list) {
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("TheUserEvent", new Object(), caseInstance.getId());
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanItemState.TERMINATED);
	}

	@Test
	public void testEventGeneratedOnDisableOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1, list.size());
		getRuntimeEngine().getTaskService().skip(list.get(0).getId(), "Builder");
		// *****THEN
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskDisabled");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskDisabled");
	}

	@Test
	public void testEventGeneratedOnEnableOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// ******WHEN
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("UserEventToStartManuallyActivatedTask", new Object(), caseInstance.getId());
		getPersistence().commit();
		assertNodeTriggered(caseInstance.getId(), "TheManuallyActivatedTaskPlanItem");
		// *****THEN
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(2, list.size());
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskEnabled");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskEnabled");
	}

	@Test
	public void testEventGeneratedOnManualStartOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("UserEventToStartManuallyActivatedTask", new Object(), caseInstance.getId());
		getPersistence().commit();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(2, list.size());
		assertNodeTriggered(caseInstance.getId(), "TheManuallyActivatedTaskPlanItem");
		// ******WHEN
		for (TaskSummary t : list) {
			if (t.getName().equals("TheManuallyActivatedTaskPlanItem")) {
				getRuntimeEngine().getTaskService().start(t.getId(), "Builder");
			}
		}
		// *****THEN
		list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(3, list.size());
		assertNodeTriggered(caseInstance.getId(), "TheManuallyActivatedTaskPlanItem");
		assertTaskTypeCreated(list, "TheManuallyActivatedTaskPlanItem");
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskManuallyStarted");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskManuallyStarted");
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskEnabled");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskEnabled");
	}

	@Test
	public void testEventGeneratedOnAutomaticStartOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// ******WHEN
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("UserEventToStartAutoActivatedTask", new Object(), caseInstance.getId());
		getPersistence().commit();
		// *****THEN
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertNodeTriggered(caseInstance.getId(), "TheAutoActivatedTaskPlanItem");
		assertTaskTypeCreated(list, "TheAutoActivatedTaskPlanItem");
		assertEquals(2, list.size());
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskAutomaticallyStarted");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskAutomaticallyStarted");
	}
	//TODO
	public void testEventGeneratedOnReactivateOfTask() throws Exception {
	}	
	//TODO
	public void testEventGeneratedOnReenableOfTask() throws Exception {
	}	
	//TODO
	public void testEventGeneratedOnCreateOfTask() throws Exception {
	//FIRE FROM SENTRY
	}	
	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager("test/HumanTaskTests.cmmn");
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
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess("PlanItemEventTests", params);
		getPersistence().commit();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		assertNodeTriggered(caseInstance.getId(), "defaultSplit");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "onWallPlanCreatedPartId");
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "WaitingForWallPlanCreatedSentry");
		getPersistence().commit();
	}

	private void triggerStartOfTask() throws Exception {
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new WallPlan(housePlan);
		getPersistence().update(housePlan);
		getPersistence().commit();
	}

}
