package org.pavanecce.cmmn.jbpm.container;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.flow.DefaultJoin;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstanceContainerLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public abstract class AbstractPlanItemInstanceContainerLifecycleTests extends AbstractPlanItemInstanceContainerTest {

	public AbstractPlanItemInstanceContainerLifecycleTests() {
		super();
	}

	public AbstractPlanItemInstanceContainerLifecycleTests(boolean setupDataSource, boolean sessionPersistence, String persistenceUnitName) {
		super(setupDataSource, sessionPersistence, persistenceUnitName);
	}

	public AbstractPlanItemInstanceContainerLifecycleTests(boolean setupDataSource, boolean sessionPersistence) {
		super(setupDataSource, sessionPersistence);
	}

	@Test
	public void testCaseLifecycleCannotComplete() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerInitialActivity();
		// *****THEN
		getPersistence().start();
		// Cannot complete it
		assertFalse(getPlanItemInstanceContainer().canComplete());
		getPersistence().commit();
		try {
			getTaskService().complete(getTaskService().getTaskByWorkItemId(getWorkitemId()).getId(), "ConstructionProjectManager", new HashMap<String, Object>());
			fail("The stage/case instance cannot be completed yet");
		} catch (RuntimeException e) {
			Status currentStatus = getTaskService().getTaskByWorkItemId(getWorkitemId()).getTaskData().getStatus();
			assertEquals(org.kie.api.task.model.Status.InProgress, currentStatus);
		}
		getPersistence().start();
		printState(" ", getPlanItemInstanceContainer());
		getPersistence().commit();
	}

	@Test
	public void testCaseLifecycleCanComplete() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerInitialActivity();
		// *****WHEN
		// complete some pending tasks
		completeTasks(getTaskService().getSubTasksByParent(getTaskService().getTaskByWorkItemId(getWorkitemId()).getId()));

		// *****THEN
		// Now we can complete it
		getPersistence().start();
		PlanItemInstanceContainerLifecycle piic = getPlanItemInstanceContainer();
		assertTrue(piic.canComplete());
		assertEquals(PlanElementState.ACTIVE, piic.getPlanElementState());
		printState(" ", piic);
		getPersistence().commit();
		Task taskByWorkItemId = getTaskService().getTaskByWorkItemId(getWorkitemId());
		getTaskService().complete(taskByWorkItemId.getId(), "ConstructionProjectManager", new HashMap<String, Object>());
		getPersistence().start();
		assertEquals(PlanElementState.COMPLETED, getPlanItemInstanceContainer().getPlanElementState());
		printState(" ", getPlanItemInstanceContainer());
		getPersistence().commit();
		// After completion, the planItem's state remain the same
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.COMPLETED);
		// and close it
		if (piic instanceof CaseInstance) {
			getPersistence().start();
			CaseInstance ci = reloadCaseInstance();
			ci.signalEvent(DefaultJoin.CLOSE, new Object());
			assertEquals(PlanElementState.CLOSED, ci.getPlanElementState());
			assertNull(getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId()));
			getPersistence().commit();
		}
		// *****THEN
	}

	@Test
	public void testTaskLifecycleSuspendAndReactivate() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		CaseInstance subCase = triggerInitialActivity();
		// *****WHEN
		Task taskByWorkItemId = getTaskService().getTaskByWorkItemId(getWorkitemId());
		getTaskService().suspend(taskByWorkItemId.getId(), "ConstructionProjectManager");
		// *******THEN
		getPersistence().start();
		PlanItemInstanceContainerLifecycle piic = getPlanItemInstanceContainer();
		assertEquals(PlanElementState.SUSPENDED, piic.getPlanElementState());
		printState(" ", piic);
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.SUSPENDED);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.SUSPENDED);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.SUSPENDED);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.SUSPENDED);
		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.SUSPENDED);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.SUSPENDED);
		List<TaskSummary> subTasksByParent = getTaskService().getSubTasksByParent(taskByWorkItemId.getTaskData().getWorkItemId());
		assertTaskInState(subTasksByParent, "TheHumanTaskPlanItem", Status.Suspended);
		assertTaskInState(subTasksByParent, "TheStagePlanItem", Status.Suspended);
		assertTaskInState(subTasksByParent, "TheCaseTaskPlanItem", Status.Suspended);
		assertEquals(PlanElementState.SUSPENDED, reloadCaseInstance(subCase).getPlanElementState());
		// reactivate
		getTaskService().resume(taskByWorkItemId.getId(), "ConstructionProjectManager");
		assertEquals(PlanElementState.ACTIVE, reloadCaseInstance(subCase).getPlanElementState());
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.ENABLED);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.ACTIVE);
		subTasksByParent = getTaskService().getSubTasksByParent(taskByWorkItemId.getTaskData().getWorkItemId());
		assertTaskInState(subTasksByParent, "TheHumanTaskPlanItem", Status.Reserved);
		assertTaskInState(subTasksByParent, "TheStagePlanItem", Status.Reserved);
		assertTaskInState(subTasksByParent, "TheCaseTaskPlanItem", Status.InProgress);
	}

	@Test
	public void testTaskLifecycleFailed() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		CaseInstance subCase = triggerInitialActivity();
		// Task taskByWorkItemId = getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId());
		// *****WHEN
		getPersistence().start();
		CaseInstance ci5 = reloadCaseInstance();
		ci5.fault();// TODO do from a task rather
		// *******THEN
		assertEquals(PlanElementState.FAILED, ci5.getPlanElementState());
		printState(" ", ci5);
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.ENABLED);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.ACTIVE);
		assertEquals(PlanElementState.ACTIVE, reloadCaseInstance(subCase).getPlanElementState());
		Task taskByWorkItemId = getTaskService().getTaskByWorkItemId(getWorkitemId());
		List<TaskSummary> subTasksByParent = getTaskService().getSubTasksByParent(taskByWorkItemId.getTaskData().getWorkItemId());
//		assertTaskInState(subTasksByParent, "TheHumanTaskPlanItem", Status.Reserved);
//		assertTaskInState(subTasksByParent, "TheStagePlanItem", Status.Reserved);
//		assertTaskInState(subTasksByParent, "TheCaseTaskPlanItem", Status.InProgress);
		// // reactivate
		// getTaskService().resume(taskByWorkItemId.getId(), "ConstructionProjectManager");
		// assertEquals(PlanElementState.ACTIVE, reloadCaseInstance(subCase).getPlanElementState());
		// assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		// assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.AVAILABLE);
		// assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.AVAILABLE);
		// assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.ENABLED);
		// assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.AVAILABLE);
		// assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
		// // assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.AVAILABLE);
		// assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.ACTIVE);
	}

	@Test
	public void testTaskLifecycleTerminate() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		CaseInstance subCase = triggerInitialActivity();
		// *****WHEN
		Task taskByWorkItemId = getTaskService().getTaskByWorkItemId(getWorkitemId());
		getTaskService().exit(taskByWorkItemId.getId(), "ConstructionProjectManager");
		// *******THEN
		getPersistence().start();
		PlanItemInstanceContainerLifecycle piic = getPlanItemInstanceContainer();
		assertEquals(PlanElementState.TERMINATED, piic.getPlanElementState());
		printState(" ", piic);
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.TERMINATED);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.TERMINATED);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.TERMINATED);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.TERMINATED);
		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.TERMINATED);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.TERMINATED);
		assertNull(reloadCaseInstance(subCase));
		List<TaskSummary> subTasksByParent = getTaskService().getSubTasksByParent(taskByWorkItemId.getTaskData().getWorkItemId());
		assertTaskInState(subTasksByParent, "TheHumanTaskPlanItem", Status.Exited);
		assertTaskInState(subTasksByParent, "TheStagePlanItem", Status.Exited);
		assertTaskInState(subTasksByParent, "TheCaseTaskPlanItem", Status.Exited);
		// reactivate
		// getTaskService().resume(taskByWorkItemId.getId(), "ConstructionProjectManager");
		// assertEquals(PlanElementState.ACTIVE, reloadCaseInstance(subCase).getPlanElementState());
		// assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		// assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.AVAILABLE);
		// assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.AVAILABLE);
		// assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.ENABLED);
		// assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.AVAILABLE);
		// assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
		// // assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.AVAILABLE);
		// assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.ACTIVE);
	}

}