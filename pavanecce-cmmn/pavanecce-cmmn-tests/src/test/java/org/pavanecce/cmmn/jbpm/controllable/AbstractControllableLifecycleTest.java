package org.pavanecce.cmmn.jbpm.controllable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.AbstractConstructionTestCase;
import org.pavanecce.cmmn.jbpm.TaskParameters;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.task.ReactivateTaskCommand;
import org.pavanecce.cmmn.jbpm.task.ReenableTaskCommand;

import test.cmmn.ConstructionCase;
import test.cmmn.House;
import test.cmmn.HousePlan;
import test.cmmn.WallPlan;

public abstract class AbstractControllableLifecycleTest extends AbstractConstructionTestCase {

	public abstract void failTask(long taskId);

	public abstract void completeTask(long taskId);

	public AbstractControllableLifecycleTest() {
		super();
	}

	public AbstractControllableLifecycleTest(boolean setupDataSource, boolean sessionPersistence, String persistenceUnitName) {
		super(setupDataSource, sessionPersistence, persistenceUnitName);
	}

	public AbstractControllableLifecycleTest(boolean setupDataSource, boolean sessionPersistence) {
		super(setupDataSource, sessionPersistence);
	}

	@Test
	public void testTaskLifecycleComplete() throws Exception {
		stopwatch.start();
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		// *******THEN
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		assertEquals(1, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ENABLED);
		long taskId = list.get(0).getId();
		getTaskService().start(taskId, getEventGeneratingTaskUser());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		getTaskService().suspend(taskId, getEventGeneratingTaskUser());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.SUSPENDED);
		getTaskService().resume(taskId, getEventGeneratingTaskUser());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		completeTask(taskId);
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.COMPLETED);
		// *****THEN
		stopwatch.finish("testTaskLifecycleComplete");
	}

	@Test
	public void testTaskLifecycleTerminate() throws Exception {
		stopwatch.start();

		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		// *******THEN
		assertEquals(1, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ENABLED);
		getTaskService().start(list.get(0).getId(), getEventGeneratingTaskUser());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		getTaskService().suspend(list.get(0).getId(), getEventGeneratingTaskUser());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.SUSPENDED);
		getTaskService().resume(list.get(0).getId(), getEventGeneratingTaskUser());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		getTaskService().exit(list.get(0).getId(), getBusinessAdministratorUser());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.TERMINATED);
		// *****THEN
		stopwatch.finish("testTaskLifecycleTerminate");

	}

	@Test
	public void testTaskLifecycleFailed() throws Exception {
		stopwatch.start();

		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		// *******THEN
		assertEquals(1, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ENABLED);
		long taskId = list.get(0).getId();
		getTaskService().start(taskId, getEventGeneratingTaskUser());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		getTaskService().suspend(taskId, getEventGeneratingTaskUser());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.SUSPENDED);
		getTaskService().resume(taskId, getEventGeneratingTaskUser());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		failTask(taskId);
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.FAILED);
		// *****THEN
		stopwatch.finish("testTaskLifecycleFailed");
	}

	@Test
	public void testTaskLifecycleExit() throws Exception {
		stopwatch.start();

		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		// *******THEN
		assertEquals(1, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ENABLED);
		getTaskService().start(list.get(0).getId(), getEventGeneratingTaskUser());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		getTaskService().suspend(list.get(0).getId(), getEventGeneratingTaskUser());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.SUSPENDED);
		getTaskService().resume(list.get(0).getId(), getEventGeneratingTaskUser());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("TheUserEvent", new Object(), caseInstance.getId());
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.TERMINATED);
		stopwatch.finish("testTaskLifecycleExit");
	}

	public abstract String getEventGeneratingTaskUser();

	@Test
	public void testEventGeneratedOnCompletionOfTask() throws Exception {
		stopwatch.start();

		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		assertNodeTriggered(caseInstance.getId(), "TheEventGeneratingTaskPlanItem");
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		long id = list.get(0).getId();
		getTaskService().start(id, getEventGeneratingTaskUser());
		completeTask(id);
		// *****THEN
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskCompleted");
		assertPlanItemInState(caseInstance.getId(), "PlanItemEnteredWhenTaskCompleted", PlanElementState.ENABLED);
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.COMPLETED);
		assertEquals("PlanItemEnteredWhenTaskCompleted", list.get(0).getName());
		stopwatch.finish("testEventGeneratedOnCompletionOfTask");
	}

	@Test
	public void testEventGeneratedOnFaultOnTask() throws Exception {
		stopwatch.start();

		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		assertNodeTriggered(caseInstance.getId(), "TheEventGeneratingTaskPlanItem");
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		long id = list.get(0).getId();
		getTaskService().start(id, getEventGeneratingTaskUser());
		failTask(id);
		// *****THEN
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskFaultOccurred");
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertEquals("PlanItemEnteredWhenTaskFaultOccurred", list.get(0).getName());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.FAILED);
		stopwatch.finish("testEventGeneratedOnFaultOnTask");
	}

	@Test
	public void testEventGeneratedOnSuspensionOfTask() throws Exception {
		stopwatch.start();

		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		// *****WHEN
		getTaskService().start(list.get(0).getId(), getEventGeneratingTaskUser());
		getTaskService().suspend(list.get(0).getId(), getEventGeneratingTaskUser());
		// *****THEN
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());

		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskSuspended");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskSuspended");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.SUSPENDED);
		stopwatch.finish("testEventGeneratedOnSuspensionOfTask");
	}

	@Test
	public void testEventGeneratedOnTerminationOfTask() throws Exception {
		stopwatch.start();

		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		// *****WHEN
		getTaskService().start(list.get(0).getId(), getEventGeneratingTaskUser());
		getTaskService().exit(list.get(0).getId(), getBusinessAdministratorUser());
		// *****THEN
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());

		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskTerminated");
		assertEquals("PlanItemEnteredWhenTaskTerminated", list.get(0).getName());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.TERMINATED);
		stopwatch.finish("testEventGeneratedOnTerminationOfTask");
	}

	protected String getBusinessAdministratorUser() {
		return "Administrator";
	}

	@Test
	public void testEventGeneratedOnResumptionOfTask() throws Exception {
		stopwatch.start();

		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		assertEquals(1, list.size());
		getTaskService().start(list.get(0).getId(), getEventGeneratingTaskUser());
		getTaskService().suspend(list.get(0).getId(), getEventGeneratingTaskUser());
		// *****WHEN
		getTaskService().resume(list.get(0).getId(), getEventGeneratingTaskUser());
		// *****THEN
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(3, list.size());
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);

		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskResumed");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskResumed");
		stopwatch.finish("testEventGeneratedOnResumptionOfTask");
	}

	@Test()
	public void testEventGeneratedOnExitOfTask() throws Exception {
		stopwatch.start();

		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		assertEquals(1, list.size());
		// *****WHEN
		triggerExitOfTask(list);
		// *****THEN
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskExited");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskExited");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.TERMINATED);
		stopwatch.finish("testEventGeneratedOnExitOfTask");
	}

	private void triggerExitOfTask(List<TaskSummary> list) {
		stopwatch.start();

		getTaskService().start(list.get(0).getId(), getEventGeneratingTaskUser());
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("TheUserEvent", new Object(), caseInstance.getId());
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.TERMINATED);
	}

	@Test
	public void testEventGeneratedOnDisableOfTask() throws Exception {
		stopwatch.start();

		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		assertEquals(1, list.size());
		getTaskService().skip(list.get(0).getId(), getEventGeneratingTaskUser());
		// *****THEN
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskDisabled");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskDisabled");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.DISABLED);
		stopwatch.finish("testEventGeneratedOnDisableOfTask");

	}

	@Test
	public void testEventGeneratedOnEnableOfTask() throws Exception {
		stopwatch.start();

		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// ******WHEN
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("UserEventToStartManuallyActivatedTask", new Object(), caseInstance.getId());
		getPersistence().commit();
		assertNodeTriggered(caseInstance.getId(), "TheManuallyActivatedTaskPlanItem");
		// *****THEN
		List<TaskSummary> list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskEnabled");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskEnabled");
		assertPlanItemInState(caseInstance.getId(), "TheManuallyActivatedTaskPlanItem", PlanElementState.ENABLED);
		stopwatch.finish("testEventGeneratedOnEnableOfTask");

	}

	@Test
	public void testEventGeneratedOnManualStartOfTask() throws Exception {
		stopwatch.start();

		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("UserEventToStartManuallyActivatedTask", new Object(), caseInstance.getId());
		getPersistence().commit();
		List<TaskSummary> list = getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskUser(), "en-UK");
		assertTrue(list.size() > 0); // there could be 2
		assertNodeTriggered(caseInstance.getId(), "TheManuallyActivatedTaskPlanItem");
		getTaskService().start(findTask(list, "TheManuallyActivatedTaskPlanItem"), getEventGeneratingTaskUser());
		// *****THEN
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(3, list.size());
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskManuallyStarted");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskManuallyStarted");
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskEnabled");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskEnabled");
		assertPlanItemInState(caseInstance.getId(), "TheManuallyActivatedTaskPlanItem", PlanElementState.ACTIVE);
		stopwatch.finish("testEventGeneratedOnManualStartOfTask");

	}

	private long findTask(List<TaskSummary> list, String taskName) {
		long eventGeneratingTaskId = -1;
		// ******WHEN
		for (TaskSummary ts : list) {
			if (ts.getName().equals(taskName)) {
				eventGeneratingTaskId = ts.getId();
			}
		}
		return eventGeneratingTaskId;
	}

	private TaskSummary findTaskSummary(List<TaskSummary> list, String taskName) {
		// ******WHEN
		for (TaskSummary ts : list) {
			if (ts.getName().equals(taskName)) {
				return ts;
			}
		}
		throw new IllegalArgumentException("Task '" + taskName + "' not found");
	}

	@Test
	public void testEventGeneratedOnAutomaticStartOfTask() throws Exception {
		stopwatch.start();

		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// ******WHEN
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("UserEventToStartAutoActivatedTask", new Object(), caseInstance.getId());
		getPersistence().commit();
		// *****THEN
		List<TaskSummary> list = getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskUser(), "en-UK");
		assertNodeTriggered(caseInstance.getId(), "TheAutoActivatedTaskPlanItem");
		assertTaskTypeCreated(list, "TheAutoActivatedTaskPlanItem");
		assertTrue(list.size() > 0); // could be two
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskAutomaticallyStarted");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskAutomaticallyStarted");
		assertPlanItemInState(caseInstance.getId(), "TheAutoActivatedTaskPlanItem", PlanElementState.ACTIVE);
		stopwatch.finish("testEventGeneratedOnAutomaticStartOfTask");

	}

	@Test
	public void testEventGeneratedOnReactivateOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		assertEquals(1, list.size());
		getTaskService().start(list.get(0).getId(), getEventGeneratingTaskUser());
		getTaskService().fail(list.get(0).getId(), getEventGeneratingTaskUser(), new HashMap<String, Object>());
		// *****WHEN
		getTaskService().execute(new ReactivateTaskCommand(list.get(0).getId(), getEventGeneratingTaskUser()));
		// *****THEN
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertTrue(list.size() > 0);
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskReactivated");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskReactivated");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ACTIVE);
		stopwatch.finish("testEventGeneratedOnReactivateOfTask");

	}

	@Test
	public void testEventGeneratedOnReenableOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		assertEquals(1, list.size());
		getTaskService().skip(list.get(0).getId(), getEventGeneratingTaskUser());
		// *****WHEN
		getTaskService().execute(new ReenableTaskCommand(list.get(0).getId(), getEventGeneratingTaskUser()));
		// *****THEN
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertTrue(list.size() > 0);
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskReenabled");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskReenabled");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ENABLED);
		stopwatch.finish("testEventGeneratedOnReenableOfTask");

	}

	@Test
	public void testEventGeneratedOnCreateOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		assertEquals(1, list.size());
		// *****THEN
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertTrue(list.size() > 0);
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskCreated");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskCreated");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.ENABLED);
		stopwatch.finish("testEventGeneratedOnCreateOfTask");

	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager(getProcessFileNames());
		Map<String, Object> params = new HashMap<String, Object>();
		getPersistence().start();

		ConstructionCase cc = new ConstructionCase("/cases/case1");
		housePlan = new HousePlan(cc);
		new WallPlan(housePlan);
		house = new House(cc);
		getPersistence().persist(cc);
		getPersistence().commit();
		params.put("housePlan", housePlan);
		params.put("house", house);
		params.put(TaskParameters.CASE_OWNER, getCaseOwner());
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

	protected abstract String getCaseOwner();

	public abstract String getNameOfProcessToStart();

	public abstract String[] getProcessFileNames();

	protected void triggerStartOfTask() throws Exception {
		getPersistence().start();
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new WallPlan(housePlan);
		getPersistence().commit();
		List<TaskSummary> list = getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskUser(), "en-UK");
		getPersistence().start();
		TaskSummary ts = findTaskSummary(list, "TheEventGeneratingTaskPlanItem");
		if (ts.getActualOwner() != null && !ts.getActualOwner().getId().equals(getEventGeneratingTaskUser())) {
			// may have been assigned to the caseOwner/initiator
			getTaskService().forward(ts.getId(), ts.getActualOwner().getId(), getEventGeneratingTaskUser());
		}
		getTaskService().claim(ts.getId(), getEventGeneratingTaskUser());
		getPersistence().commit();
	}

}