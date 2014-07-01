package org.pavanecce.cmmn.jbpm.planning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.jbpm.services.task.impl.model.UserImpl;
import org.junit.Test;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.model.InternalTaskData;
import org.pavanecce.cmmn.jbpm.ApplicableDiscretionaryItem;
import org.pavanecce.cmmn.jbpm.container.AbstractPlanItemInstanceContainerTest;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public class PlanningTest extends AbstractPlanItemInstanceContainerTest {
	PlanningService planningService = new PlanningService();
	{
		isJpa = true;
	}

	@Test
	public void testStartPlanning() throws Exception {
		givenThatTheTestCaseIsStarted();
		triggerInitialActivity();
		getPersistence().start();
		PlanningTableInstance pti = getPlanningService().startPlanning(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(),
				"ConstructionProjectManager", false);
		assertEquals(0, pti.getApplicableDiscretionaryItems().size());
		getPersistence().commit();
		getPersistence().start();
		reloadCaseInstance(caseInstance).getRoleAssignments("ConstructionProjectManagers").add("ConstructionProjectManager");
		getPersistence().commit();
		getPersistence().start();
		pti = getPlanningService().startPlanning(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(), "ConstructionProjectManager",
				false);
		for (PlannedTaskSummary plannedTaskSummary : pti.getPlannedTasks()) {
			assertNull(plannedTaskSummary.getDiscretionaryItemId());
			assertEquals(PlanningStatus.PLANNING_IN_PROGRESS, plannedTaskSummary.getPlanningStatus());
		}
		getPersistence().commit();
		assertPlanItemDefinitionPresent(pti.getApplicableDiscretionaryItems(), "TheCaseTask");
		assertPlanItemDefinitionPresent(pti.getApplicableDiscretionaryItems(), "TheHumanTask");
		assertPlanItemDefinitionPresent(pti.getApplicableDiscretionaryItems(), "TheStage");
		assertItemPresent(pti.getPlannedTasks(), "TheCaseTaskPlanItem");
		assertItemPresent(pti.getPlannedTasks(), "TheStagePlanItem");
		assertItemPresent(pti.getPlannedTasks(), "TheHumanTaskPlanItem");
		assertEquals(4, pti.getApplicableDiscretionaryItems().size());
		for (ApplicableDiscretionaryItem pts : pti.getApplicableDiscretionaryItems()) {
			if ("theUnapplicableItem".equals(pts.getDiscretionaryItemId())) {
				fail();
			}
		}
	}

	@Test
	public void testPreparePlannedTask() throws Exception {
		givenThatTheTestCaseIsStarted();
		triggerInitialActivity();
		PlannedTask plannedCaseTask = getPlanningService().preparePlannedTask(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(),
				"theCaseTaskDiscretionaryItemId");
		PlannedTask plannedHumanTask = getPlanningService().preparePlannedTask(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(),
				"theHumanTaskDiscretionaryItemId");
		PlannedTask plannedStage = getPlanningService().preparePlannedTask(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(),
				"theStageDiscretionaryItemId");
		getPersistence().start();
		CaseInstance ci = reloadCaseInstance(caseInstance);
		assertEquals("TheCaseTask", plannedCaseTask.getNames().get(0).getText());
		assertEquals("TheHumanTask", plannedHumanTask.getNames().get(0).getText());
		assertEquals("TheStage", plannedStage.getNames().get(0).getText());
		assertEquals(Status.Created, plannedCaseTask.getTaskData().getStatus());
		assertEquals(Status.Created, plannedHumanTask.getTaskData().getStatus());
		assertEquals(Status.Created, plannedStage.getTaskData().getStatus());
		assertNull(ci.findNodeForWorkItem(plannedCaseTask.getTaskData().getWorkItemId()));
		assertNull(ci.findNodeForWorkItem(plannedHumanTask.getTaskData().getWorkItemId()));
		assertNull(ci.findNodeForWorkItem(plannedStage.getTaskData().getWorkItemId()));
		getPersistence().commit();
	}

	@Test
	public void testActivateDiscretionaryItem() throws Exception {
		givenThatTheTestCaseIsStarted();
		triggerInitialActivity();
		getPlanningService().makeDiscretionaryItemAvailable(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(),
				"theHumanTaskDiscretionaryItemWithEntryCriteriaId");
		assertNodeNotTriggered(caseInstance.getId(), "TheHumanTask");
		getPersistence().start();
		reloadCaseInstance(caseInstance).signalEvent("DiscretionaryStartUserEvent", new Object());
		getPersistence().commit();
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "TheHumanTask");
		getPersistence().commit();
		getPersistence().start();
		List<TaskSummary> tasksAssignedAsPotentialOwner = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertTaskInState(tasksAssignedAsPotentialOwner, "TheHumanTask", Status.Ready);
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheHumanTask", PlanElementState.ENABLED);
	}

	@Test
	public void testSubmitPlanWithContainerAlreadyActive() throws Exception {
		givenThatTheTestCaseIsStarted();
		triggerInitialActivity();
		getPersistence().start();
		Long parentTaskId = getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId();
		List<PlannedTask> plannedTasks = new ArrayList<PlannedTask>();
		PlannedTask plannedCaseTask = getPlanningService().preparePlannedTask(parentTaskId, "theCaseTaskDiscretionaryItemId");
		PlannedTask plannedHumanTask = getPlanningService().preparePlannedTask(parentTaskId, "theHumanTaskDiscretionaryItemId");
		PlannedTask plannedStage = getPlanningService().preparePlannedTask(parentTaskId, "theStageDiscretionaryItemId");
		PlanningTableInstance pti = getPlanningService().startPlanning(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(),
				"ConstructionProjectManager", false);
		plannedTasks.add(plannedCaseTask);
		plannedTasks.add(plannedStage);
		plannedTasks.add(plannedHumanTask);
		for (PlannedTaskSummary s : pti.getPlannedTasks()) {
			plannedTasks.add(getPlanningService().getPlannedTaskById(s.getId()));
		}
		for (PlannedTask plannedTask : plannedTasks) {
			((InternalTaskData) plannedTask.getTaskData()).setActualOwner(new UserImpl("salaboy"));
			plannedTask.getPeopleAssignments().getPotentialOwners().add(new UserImpl("salaboy"));
		}
		getPersistence().commit();
		getPersistence().start();
		getPlanningService().submitPlan(parentTaskId, plannedTasks, false);
		getPersistence().commit();
		getPersistence().start();
		caseInstance = reloadCaseInstance(caseInstance);
		assertFalse(caseInstance.canComplete());
		assertNotNull(caseInstance.findNodeForWorkItem(plannedCaseTask.getTaskData().getWorkItemId()));
		assertNotNull(caseInstance.findNodeForWorkItem(plannedHumanTask.getTaskData().getWorkItemId()));
		assertNotNull(caseInstance.findNodeForWorkItem(plannedStage.getTaskData().getWorkItemId()));
		assertEquals(PlanElementState.ENABLED, caseInstance.findNodeForWorkItem(plannedCaseTask.getTaskData().getWorkItemId()).getPlanElementState());
		assertEquals(PlanElementState.ENABLED, caseInstance.findNodeForWorkItem(plannedHumanTask.getTaskData().getWorkItemId()).getPlanElementState());
		// Automatic activation:
		assertEquals(PlanElementState.ACTIVE, caseInstance.findNodeForWorkItem(plannedStage.getTaskData().getWorkItemId()).getPlanElementState());
		getPersistence().commit();
		getPersistence().start();
		List<TaskSummary> tasks = getTaskService().getTasksOwned("salaboy", "en-UK");
		assertEquals(6, tasks.size());
		assertTaskInState(tasks, plannedCaseTask.getPlanItemName(), Status.Reserved);
		assertTaskInState(tasks, plannedHumanTask.getPlanItemName(), Status.Reserved);
		assertTaskInState(tasks, plannedStage.getPlanItemName(), Status.InProgress);
		super.completeTasks(tasks);
		getPersistence().commit();
		getPersistence().start();
		caseInstance = reloadCaseInstance(caseInstance);
		assertFalse(caseInstance.canComplete());
		getPersistence().commit();
		getPersistence().start();
		getTaskService().start(plannedHumanTask.getId(), "salaboy");
		getTaskService().complete(plannedHumanTask.getId(), "salaboy", new HashMap<String, Object>());
		getPersistence().commit();
		getPersistence().start();
		caseInstance = reloadCaseInstance(caseInstance);
		assertTrue(caseInstance.canComplete());
		getPersistence().commit();
	}

	@Test
	public void testSubmitPlanWithContainerNotActive() throws Exception {
		givenThatTheTestCaseIsStarted();
		triggerInitialActivity();
		Long parentTaskId = getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId();
		List<PlannedTask> plannedTasks = new ArrayList<PlannedTask>();
		PlannedTask plannedCaseTask = getPlanningService().preparePlannedTask(parentTaskId, "theCaseTaskDiscretionaryItemId");
		PlannedTask plannedHumanTask = getPlanningService().preparePlannedTask(parentTaskId, "theHumanTaskDiscretionaryItemId");
		PlannedTask plannedStage = getPlanningService().preparePlannedTask(parentTaskId, "theStageDiscretionaryItemId");
		PlanningTableInstance pti = getPlanningService().startPlanning(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(),
				"ConstructionProjectManager", true);
		plannedTasks.add(plannedCaseTask);
		plannedTasks.add(plannedStage);
		plannedTasks.add(plannedHumanTask);
		assertEquals(PlanElementState.SUSPENDED, reloadCaseInstance().getPlanElementState());
		for (PlannedTaskSummary s : pti.getPlannedTasks()) {
			plannedTasks.add(getPlanningService().getPlannedTaskById(s.getId()));
		}
		for (PlannedTask plannedTask : plannedTasks) {
			((InternalTaskData) plannedTask.getTaskData()).setActualOwner(new UserImpl("salaboy"));
		}
		getPersistence().start();
		getPlanningService().submitPlan(parentTaskId, plannedTasks, false);
		getPersistence().commit();
		List<TaskSummary> tasks = getTaskService().getTasksOwned("salaboy", "en-UK");
		assertEquals(6, tasks.size());
		for (TaskSummary taskSummary : tasks) {
			if (taskSummary.getId() == plannedCaseTask.getId() || taskSummary.getId() == plannedHumanTask.getId()
					|| taskSummary.getId() == plannedStage.getId()) {
				// They're all ENABLED
				assertEquals(Status.Created, taskSummary.getStatus());
			} else {
				assertEquals(Status.Suspended, taskSummary.getStatus());
			}
		}
		getPersistence().start();
		caseInstance = reloadCaseInstance(caseInstance);
		printState("", caseInstance);
		assertNotNull(caseInstance.findNodeForWorkItem(plannedCaseTask.getTaskData().getWorkItemId()));
		assertNotNull(caseInstance.findNodeForWorkItem(plannedHumanTask.getTaskData().getWorkItemId()));
		assertEquals(PlanElementState.INITIAL, caseInstance.findNodeForWorkItem(plannedCaseTask.getTaskData().getWorkItemId()).getPlanElementState());
		assertEquals(PlanElementState.INITIAL, caseInstance.findNodeForWorkItem(plannedHumanTask.getTaskData().getWorkItemId()).getPlanElementState());
		assertEquals(PlanElementState.INITIAL, caseInstance.findNodeForWorkItem(plannedStage.getTaskData().getWorkItemId()).getPlanElementState());
		getPersistence().commit();
		getPersistence().start();
		getTaskService().resume(parentTaskId, "ConstructionProjectManager");
		getPersistence().commit();
		getPersistence().start();
		caseInstance = reloadCaseInstance(caseInstance);
		assertEquals(PlanElementState.ENABLED, caseInstance.findNodeForWorkItem(plannedCaseTask.getTaskData().getWorkItemId()).getPlanElementState());
		assertEquals(PlanElementState.ENABLED, caseInstance.findNodeForWorkItem(plannedHumanTask.getTaskData().getWorkItemId()).getPlanElementState());
		// Automatic activation:
		assertEquals(PlanElementState.ACTIVE, caseInstance.findNodeForWorkItem(plannedStage.getTaskData().getWorkItemId()).getPlanElementState());
		getPersistence().commit();
	}

	private void assertPlanItemDefinitionPresent(Collection<ApplicableDiscretionaryItem> pts, String itemName) {
		for (ApplicableDiscretionaryItem pt : pts) {
			if (itemName.equals(pt.getPlanItemName())) {
				return;
			}
		}
		fail("Item with name '" + itemName + "' not found");
	}

	public void assertItemPresent(Collection<PlannedTaskSummary> pts, String itemName) {
		for (PlannedTaskSummary pt : pts) {
			if (itemName.equals(pt.getPlanItemName())) {
				return;
			}
		}
		fail("Item with name '" + itemName + "' not found");
	}

	public PlanningService getPlanningService() {
		planningService.setTaskService(getTaskService());
		return planningService;
	}

	@Override
	protected RuntimeManager createRuntimeManager(String... processFile) {
		RuntimeManager rm = super.createRuntimeManager(processFile);
		planningService.setRuntimeManager(rm);
		return rm;
	}

	@Override
	public String getProcessFile() {
		return "test/planning/PlanningTests.cmmn";
	}

	@Override
	public String getCaseName() {
		return "PlanningTests";
	}

	@Override
	protected void ensurePlanItemContainerIsStarted() {
	}

}