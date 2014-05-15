package org.pavanecce.cmmn.jbpm.planitem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.AbstractConstructionTestCase;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;
import org.pavanecce.cmmn.jbpm.instance.impl.CaseInstance;

import test.ConstructionCase;
import test.House;
import test.HousePlan;
import test.WallPlan;

public abstract class AbstractTasklifecycleTests extends AbstractConstructionTestCase {

	protected HousePlan housePlan;
	protected House house;
	protected CaseInstance caseInstance;

	public abstract void failTask(long taskId);

	public abstract void completeTask(long taskId);

	public AbstractTasklifecycleTests() {
		super();
	}

	public AbstractTasklifecycleTests(boolean setupDataSource, boolean sessionPersistence, String persistenceUnitName) {
		super(setupDataSource, sessionPersistence, persistenceUnitName);
	}

	public AbstractTasklifecycleTests(boolean setupDataSource, boolean sessionPersistence) {
		super(setupDataSource, sessionPersistence);
	}

	@Test
	public void testTaskLifecycleComplete() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
		// *******THEN
		assertEquals(1, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ENABLED);
		long taskId = list.get(0).getId();
		getRuntimeEngine().getTaskService().start(taskId, getEventGeneratingTaskRole());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		getRuntimeEngine().getTaskService().suspend(taskId, getEventGeneratingTaskRole());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.SUSPENDED);
		getRuntimeEngine().getTaskService().resume(taskId, getEventGeneratingTaskRole());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		completeTask(taskId);
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.COMPLETED);
		// *****THEN
	}

	@Test
	public void testTaskLifecycleTerminate() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
		// *******THEN
		assertEquals(1, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ENABLED);
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), getEventGeneratingTaskRole());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		getRuntimeEngine().getTaskService().suspend(list.get(0).getId(), getEventGeneratingTaskRole());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.SUSPENDED);
		getRuntimeEngine().getTaskService().resume(list.get(0).getId(), getEventGeneratingTaskRole());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		getRuntimeEngine().getTaskService().exit(list.get(0).getId(), getBusinessAdministratorRole());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.TERMINATED);
		// *****THEN
	}

	@Test
	public void testTaskLifecycleFailed() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
		// *******THEN
		assertEquals(1, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ENABLED);
		long taskId = list.get(0).getId();
		getRuntimeEngine().getTaskService().start(taskId, getEventGeneratingTaskRole());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		getRuntimeEngine().getTaskService().suspend(taskId, getEventGeneratingTaskRole());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.SUSPENDED);
		getRuntimeEngine().getTaskService().resume(taskId, getEventGeneratingTaskRole());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		failTask(taskId);
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.FAILED);
		// *****THEN
	}

	@Test
	public void testTaskLifecycleExit() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
		// *******THEN
		assertEquals(1, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ENABLED);
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), getEventGeneratingTaskRole());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		getRuntimeEngine().getTaskService().suspend(list.get(0).getId(), getEventGeneratingTaskRole());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.SUSPENDED);
		getRuntimeEngine().getTaskService().resume(list.get(0).getId(), getEventGeneratingTaskRole());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("TheUserEvent", new Object(), caseInstance.getId());
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.TERMINATED);
	}

	public abstract String getEventGeneratingTaskRole();

	@Test
	public void testEventGeneratedOnCompletionOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		assertNodeTriggered(caseInstance.getId(), "TheEventGeneratingTaskPlanItem");
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		long id = list.get(0).getId();
		getRuntimeEngine().getTaskService().start(id, getEventGeneratingTaskRole());
		completeTask(id);
		// *****THEN
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskCompleted");
		assertPlanItemInState(caseInstance.getId(), "PlanItemEnteredWhenTaskCompleted", PlanElementState.ENABLED);
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
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		long id = list.get(0).getId();
		getRuntimeEngine().getTaskService().start(id, getEventGeneratingTaskRole());
		failTask(id);
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
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		// *****WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), getEventGeneratingTaskRole());
		getRuntimeEngine().getTaskService().suspend(list.get(0).getId(), getEventGeneratingTaskRole());
		// *****THEN
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());

		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskSuspended");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskSuspended");
	}

	@Test
	public void testEventGeneratedOnTerminationOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		// *****WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), getEventGeneratingTaskRole());
		getRuntimeEngine().getTaskService().exit(list.get(0).getId(), getBusinessAdministratorRole());
		// *****THEN
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());

		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskTerminated");
		assertEquals("PlanItemEnteredWhenTaskTerminated", list.get(0).getName());
	}

	protected String getBusinessAdministratorRole() {
		return "Administrator";
	}

	@Test
	public void testEventGeneratedOnResumptionOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		// *****WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), getEventGeneratingTaskRole());
		getRuntimeEngine().getTaskService().suspend(list.get(0).getId(), getEventGeneratingTaskRole());
		getRuntimeEngine().getTaskService().resume(list.get(0).getId(), getEventGeneratingTaskRole());
		// *****THEN
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());

		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskResumed");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskResumed");
	}

	@Test
	public void testEventGeneratedOnExitOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
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
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), getEventGeneratingTaskRole());
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("TheUserEvent", new Object(), caseInstance.getId());
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.TERMINATED);
	}

	@Test
	public void testEventGeneratedOnDisableOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
		assertEquals(1, list.size());
		getRuntimeEngine().getTaskService().skip(list.get(0).getId(), getEventGeneratingTaskRole());
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
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());
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
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
		assertEquals(1, list.size());
		assertNodeTriggered(caseInstance.getId(), "TheManuallyActivatedTaskPlanItem");
		// ******WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), getEventGeneratingTaskRole());
		// *****THEN
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
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
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
		assertNodeTriggered(caseInstance.getId(), "TheAutoActivatedTaskPlanItem");
		assertTaskTypeCreated(list, "TheAutoActivatedTaskPlanItem");
		assertEquals(1, list.size());
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskAutomaticallyStarted");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskAutomaticallyStarted");
	}

	public void testEventGeneratedOnReactivateOfTask() throws Exception {
	}

	public void testEventGeneratedOnReenableOfTask() throws Exception {
	}

	public void testEventGeneratedOnCreateOfTask() throws Exception {
		// FIRE FROM SENTRY
	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager(getProcessFileNames());
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
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess(getNameOfProcessToStart(), params);
		getPersistence().commit();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		assertNodeTriggered(caseInstance.getId(), "defaultSplit");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "onWallPlanCreatedPartId");
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "WaitingForWallPlanCreatedSentry");
		getPersistence().commit();
	}

	public abstract String getNameOfProcessToStart();

	public abstract String[] getProcessFileNames();

	protected void triggerStartOfTask() throws Exception {
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new WallPlan(housePlan);
		getPersistence().update(housePlan);
		getPersistence().commit();
	}

}