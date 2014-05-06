package org.pavanecce.cmmn.jbpm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.shared.services.impl.events.JbpmServicesEventListener;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.EventService;
import org.kie.internal.task.api.model.NotificationEvent;
import org.pavanecce.cmmn.jbpm.flow.builder.CMMNBuilder;
import org.pavanecce.cmmn.jbpm.instance.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.CaseTaskLifecycleListener;

import test.ConstructionCase;
import test.House;
import test.HousePlan;
import test.RoofPlan;
import test.WallPlan;

public class SinglePlanItemEntryCriterionTests extends AbstractJbpmCaseTestCase {
	{
		super.isJpa = true;
	}
	protected HousePlan housePlan;
	protected House house;
	private CaseInstance caseInstance;

	public SinglePlanItemEntryCriterionTests() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void testOnCompletionOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		assertNodeTriggered(caseInstance.getId(), "TheEventGeneratingTaskPlanItem");
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1,list.size());
		// *****WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		getRuntimeEngine().getTaskService().complete(list.get(0).getId(), "Builder", new HashMap<String,Object>());
		// *****THEN
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskCompleted");
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1,list.size());
		assertEquals("PlanItemEnteredWhenTaskCompleted", list.get(0).getName());
	}
	@Test
	public void testOnFaultOnTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		assertNodeTriggered(caseInstance.getId(), "TheEventGeneratingTaskPlanItem");
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1,list.size());
		// *****WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		getRuntimeEngine().getTaskService().fail(list.get(0).getId(), "Builder", new HashMap<String,Object>());
		// *****THEN
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskFaultOccurred");
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1,list.size());
		assertEquals("PlanItemEnteredWhenTaskFaultOccurred", list.get(0).getName());
	}
	@Test
	public void testOnSuspensionOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1,list.size());
		// *****WHEN
		// *****WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		getRuntimeEngine().getTaskService().suspend(list.get(0).getId(), "Builder");
		// *****THEN
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2,list.size());
		
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskSuspended");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskSuspended");
	}

	protected void assertTaskTypeCreated(List<TaskSummary> list, String expected) {
		for (TaskSummary taskSummary : list) {
			if(taskSummary.getName().equals(expected)){
				return;
			}
		}
		fail("Task not created: " + expected);
	}
	@Test
	public void testOnTerminationOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1,list.size());
		// *****WHEN
		// *****WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		getRuntimeEngine().getTaskService().exit(list.get(0).getId(), "Builder");
		// *****THEN
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1,list.size());
		
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskTerminated");
		assertEquals("PlanItemEnteredWhenTaskTerminated", list.get(0).getName());
	}
	@Test
	public void testOnResumptionOfTask() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1,list.size());
		// *****WHEN
		// *****WHEN
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		getRuntimeEngine().getTaskService().suspend(list.get(0).getId(), "Builder");
		getRuntimeEngine().getTaskService().resume(list.get(0).getId(), "Builder");
		// *****THEN
		list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(3,list.size());
		
		assertNodeTriggered(caseInstance.getId(), "PlanItemEnteredWhenTaskResumed");
		assertTaskTypeCreated(list, "PlanItemEnteredWhenTaskResumed");
	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager("test/SinglePlanItemEntryCriterionTests.cmmn");
		@SuppressWarnings("unchecked")
		EventService<JbpmServicesEventListener<NotificationEvent>, JbpmServicesEventListener<Task>> eventService = (EventService<JbpmServicesEventListener<NotificationEvent>, JbpmServicesEventListener<Task>>) getRuntimeEngine()
				.getTaskService();
		eventService.registerTaskLifecycleEventListener(new CaseTaskLifecycleListener(getRuntimeEngine().getKieSession()));
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
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess("SinglePlanItemEntryCriterionTests", params);
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
