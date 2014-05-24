package org.pavanecce.cmmn.jbpm.planning;

import java.util.ArrayList;
import java.util.Collection;
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
	public void testGetPlanningTable() throws Exception {
		givenThatTheTestCaseIsStarted();
		triggerInitialActivity();
		PlanningTableInstance pti = getPlanningService().startPlanning(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(), "ConstructionProjectManager",false);
		for (PlannedTaskSummary plannedTaskSummary : pti.getPlannedTasks()) {
			assertNull(plannedTaskSummary.getDiscretionaryItemId());
			assertEquals(PlanningStatus.PLANNING_IN_PROGRESS, plannedTaskSummary.getPlanningStatus());
		}
		assertDiscretionaryItemPresent(pti.getApplicableDiscretionaryItems(), "TheCaseTask");
		assertDiscretionaryItemPresent(pti.getApplicableDiscretionaryItems(), "TheHumanTask");
		assertDiscretionaryItemPresent(pti.getApplicableDiscretionaryItems(), "TheStage");
		assertItemPresent(pti.getPlannedTasks(), "TheCaseTaskPlanItem");
		assertItemPresent(pti.getPlannedTasks(), "TheStagePlanItem");
		assertItemPresent(pti.getPlannedTasks(), "TheHumanTaskPlanItem");
	}

	@Test
	public void testPreparePlannedTask() throws Exception {
		givenThatTheTestCaseIsStarted();
		triggerInitialActivity();
		PlannedTask plannnedCaseTask = getPlanningService().preparePlannedTask(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(), "theCaseTaskDiscretionaryItemId");
		PlannedTask plannnedHumanTask = getPlanningService().preparePlannedTask(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(), "theHumanTaskDiscretionaryItemId");
		PlannedTask plannnedStage = getPlanningService().preparePlannedTask(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(), "theStageDiscretionaryItemId");
		getPersistence().start();
		CaseInstance ci = reloadCaseInstance(caseInstance);
		assertEquals("TheCaseTask", plannnedCaseTask.getNames().get(0).getText());
		assertEquals("TheHumanTask", plannnedHumanTask.getNames().get(0).getText());
		assertEquals("TheStage", plannnedStage.getNames().get(0).getText());
		assertEquals(Status.Created, plannnedCaseTask.getTaskData().getStatus());
		assertEquals(Status.Created, plannnedHumanTask.getTaskData().getStatus());
		assertEquals(Status.Created, plannnedStage.getTaskData().getStatus());
		assertNull(ci.findNodeForWorkItem(plannnedCaseTask.getTaskData().getWorkItemId()));
		assertNull(ci.findNodeForWorkItem(plannnedHumanTask.getTaskData().getWorkItemId()));
		assertNull(ci.findNodeForWorkItem(plannnedStage.getTaskData().getWorkItemId()));
		getPersistence().commit();
	}

	@Test
	public void testSubmitPlanWithContainerAlreadyActive() throws Exception {
		givenThatTheTestCaseIsStarted();
		triggerInitialActivity();
		Long parentTaskId = getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId();
		List<PlannedTask> plannedTasks = new ArrayList<PlannedTask>();
		PlannedTask plannnedCaseTask = getPlanningService().preparePlannedTask(parentTaskId, "theCaseTaskDiscretionaryItemId");
		PlannedTask plannnedHumanTask = getPlanningService().preparePlannedTask(parentTaskId, "theHumanTaskDiscretionaryItemId");
		PlannedTask plannnedStage = getPlanningService().preparePlannedTask(parentTaskId, "theStageDiscretionaryItemId");
		PlanningTableInstance pti = getPlanningService().startPlanning(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(), "ConstructionProjectManager",false);
		plannedTasks.add(plannnedCaseTask);
		plannedTasks.add(plannnedStage);
		plannedTasks.add(plannnedHumanTask);
		for (PlannedTaskSummary s : pti.getPlannedTasks()) {
			plannedTasks.add(getPlanningService().getPlannedTaskById(s.getId()));
		}
		for (PlannedTask plannedTask : plannedTasks) {
			((InternalTaskData) plannedTask.getTaskData()).setActualOwner(new UserImpl("salaboy"));
			plannedTask.getPeopleAssignments().getPotentialOwners().add(new UserImpl("salaboy"));
		}
		getPlanningService().submitPlan(parentTaskId, plannedTasks,false);
		List<TaskSummary> tasks = getTaskService().getTasksOwned("salaboy", "en-UK");
		assertEquals(6,tasks.size());
		for (TaskSummary taskSummary : tasks) {
			if(taskSummary.getId()==plannnedCaseTask.getId() || taskSummary.getId()==plannnedHumanTask.getId()||taskSummary.getId()==plannnedStage.getId()){
				//They're all ENABLED
				assertEquals(Status.Reserved, taskSummary.getStatus());
			}
		}
		getPersistence().start();
		CaseInstance ci = reloadCaseInstance(caseInstance);
		assertNotNull(ci.findNodeForWorkItem(plannnedCaseTask.getTaskData().getWorkItemId()));
		assertNotNull(ci.findNodeForWorkItem(plannnedHumanTask.getTaskData().getWorkItemId()));
		assertNotNull(ci.findNodeForWorkItem(plannnedStage.getTaskData().getWorkItemId()));
		getPersistence().commit();
	}
	@Test
	public void testSubmitPlanWithContainerNotActive() throws Exception {
		givenThatTheTestCaseIsStarted();
		triggerInitialActivity();
		Long parentTaskId = getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId();
		List<PlannedTask> plannedTasks = new ArrayList<PlannedTask>();
		PlannedTask plannnedCaseTask = getPlanningService().preparePlannedTask(parentTaskId, "theCaseTaskDiscretionaryItemId");
		PlannedTask plannnedHumanTask = getPlanningService().preparePlannedTask(parentTaskId, "theHumanTaskDiscretionaryItemId");
		PlannedTask plannnedStage = getPlanningService().preparePlannedTask(parentTaskId, "theStageDiscretionaryItemId");
		PlanningTableInstance pti = getPlanningService().startPlanning(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(), "ConstructionProjectManager",true);
		plannedTasks.add(plannnedCaseTask);
		plannedTasks.add(plannnedStage);
		plannedTasks.add(plannnedHumanTask);
		assertEquals(PlanElementState.SUSPENDED, reloadCaseInstance().getPlanElementState());
		for (PlannedTaskSummary s : pti.getPlannedTasks()) {
			plannedTasks.add(getPlanningService().getPlannedTaskById(s.getId()));
		}
		for (PlannedTask plannedTask : plannedTasks) {
			((InternalTaskData) plannedTask.getTaskData()).setActualOwner(new UserImpl("salaboy"));
		}
		getPlanningService().submitPlan(parentTaskId, plannedTasks,false);
		List<TaskSummary> tasks = getTaskService().getTasksOwned("salaboy", "en-UK");
		assertEquals(6,tasks.size());
		for (TaskSummary taskSummary : tasks) {
			if(taskSummary.getId()==plannnedCaseTask.getId() || taskSummary.getId()==plannnedHumanTask.getId()||taskSummary.getId()==plannnedStage.getId()){
				//They're all ENABLED
				assertEquals(Status.Created, taskSummary.getStatus());
			}else{
				assertEquals(Status.Suspended, taskSummary.getStatus());
			}
		}
		getPersistence().start();
		CaseInstance ci = reloadCaseInstance(caseInstance);
		assertNotNull(ci.findNodeForWorkItem(plannnedCaseTask.getTaskData().getWorkItemId()));
		assertNotNull(ci.findNodeForWorkItem(plannnedHumanTask.getTaskData().getWorkItemId()));
		assertNotNull(ci.findNodeForWorkItem(plannnedStage.getTaskData().getWorkItemId()));
		getPersistence().commit();
	}

	private void assertDiscretionaryItemPresent(Collection<ApplicableDiscretionaryItem> pts, String itemName) {
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