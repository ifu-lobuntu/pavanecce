package org.pavanecce.cmmn.jbpm.planitem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.AbstractConstructionTestCase;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.task.ReactivateTaskCommand;
import org.pavanecce.cmmn.jbpm.task.ReenableTaskCommand;

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
	}

	@Test
	public void testTaskLifecycleTerminate() throws Exception {
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
	}

	@Test
	public void testTaskLifecycleFailed() throws Exception {
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
	}

	@Test
	public void testTaskLifecycleExit() throws Exception {
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
	}

	public abstract String getEventGeneratingTaskUser();

	@Test
	public void testEventGeneratedOnCompletionOfTask() throws Exception {
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
	}

	@Test
	public void testEventGeneratedOnFaultOnTask() throws Exception {
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

	}

	@Test
	public void testEventGeneratedOnSuspensionOfTask() throws Exception {
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

	}

	@Test
	public void testEventGeneratedOnTerminationOfTask() throws Exception {
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
	}

	protected String getBusinessAdministratorUser() {
		return "Administrator";
	}

	@Test
	public void testEventGeneratedOnResumptionOfTask() throws Exception {
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
	}

	@Test
	public void testEventGeneratedOnExitOfTask() throws Exception {
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
	}

	private void triggerExitOfTask(List<TaskSummary> list) {
		getTaskService().start(list.get(0).getId(), getEventGeneratingTaskUser());
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
		List<TaskSummary> list = getTaskService().getTasksOwned(getEventGeneratingTaskUser(), "en-UK");
		assertEquals(1, list.size());
		getTaskService().skip(list.get(0).getId(), getEventGeneratingTaskUser());
		// *****THEN
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskDisabled");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskDisabled");
		assertPlanItemInState(caseInstance.getId(), "TheEventGeneratingTaskPlanItem", PlanElementState.DISABLED);
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
		List<TaskSummary> list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskEnabled");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskEnabled");
		assertPlanItemInState(caseInstance.getId(), "TheManuallyActivatedTaskPlanItem", PlanElementState.ENABLED);

	}

	@Test
	public void testEventGeneratedOnManualStartOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("UserEventToStartManuallyActivatedTask", new Object(), caseInstance.getId());
		getPersistence().commit();
		List<TaskSummary> list = getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskUser(), "en-UK");
		assertTrue(list.size() > 0);// there could be 2
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

	@Test
	public void testEventGeneratedOnAutomaticStartOfTask() throws Exception {
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
		assertTrue(list.size() > 0);// could be two
		list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskAutomaticallyStarted");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskAutomaticallyStarted");
		assertPlanItemInState(caseInstance.getId(), "TheAutoActivatedTaskPlanItem", PlanElementState.ACTIVE);
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
		params.put(Case.CASE_OWNER, getCaseOwner());
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
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new WallPlan(housePlan);
		getPersistence().update(housePlan);
		getPersistence().commit();
		List<TaskSummary> list = getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskUser(), "en-UK");
		getTaskService().claim(findTask(list, "TheEventGeneratingTaskPlanItem"), getEventGeneratingTaskUser());
	}

}