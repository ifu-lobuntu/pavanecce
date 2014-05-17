package org.pavanecce.cmmn.jbpm.planitem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.AbstractConstructionTestCase;
import org.pavanecce.cmmn.jbpm.flow.DefaultJoin;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceContainer;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.instance.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.CaseTaskPlanItemInstance;

import test.ConstructionCase;
import test.House;
import test.HousePlan;

public class CaseInstanceTests extends AbstractConstructionTestCase {

	protected HousePlan housePlan;
	protected House house;
	protected CaseInstance caseInstance;
	{
		super.isJpa = true;
	}

	public CaseInstanceTests() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void testCaseLifecycleCannotComplete() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerInitialActivity();
		// *****THEN
		getPersistence().start();
		CaseInstance ci2 = reloadCaseInstance();
		// Cannot complete it
		assertFalse(ci2.canComplete());
		try {
			getTaskService().complete(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId(), "ConstructionProjectManager", new HashMap<String, Object>());
			fail("The stage/case instance cannot be completed yet");
		} catch (RuntimeException e) {
			Status currentStatus = getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getTaskData().getStatus();
			assertEquals(org.kie.api.task.model.Status.InProgress, currentStatus);
		}
		printState(" ", ci2);
		getPersistence().commit();
	}

	@Test
	public void testCaseLifecycleCanComplete() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		triggerInitialActivity();
		// *****WHEN
		// complete some pending tasks
		completeTasks(getTaskService().getSubTasksByParent(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId()));

		// *****THEN
		// Now we can complete it
		getPersistence().start();
		CaseInstance ci4 = reloadCaseInstance();
		assertTrue(ci4.canComplete());
		assertEquals(PlanElementState.ACTIVE, ci4.getPlanElementState());
		printState(" ", ci4);
		getPersistence().commit();
		Task taskByWorkItemId = getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId());
		getTaskService().complete(taskByWorkItemId.getId(), "ConstructionProjectManager", new HashMap<String, Object>());
		getPersistence().start();
		CaseInstance ci5 = reloadCaseInstance();
		assertEquals(PlanElementState.COMPLETED, ci5.getPlanElementState());
		printState(" ", ci5);
		getPersistence().commit();
		// After completion, the planItem's state remain the same
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
		// assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.COMPLETED);
		// and close it
		getPersistence().start();
		CaseInstance ci6 = reloadCaseInstance();
		ci6.signalEvent(DefaultJoin.CLOSE, new Object());
		assertEquals(PlanElementState.CLOSED, ci6.getPlanElementState());
		assertNull(getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId()));
		getPersistence().commit();
		// *****THEN
	}

	@Test
	public void testTaskLifecycleSuspendAndReactivate() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		CaseInstance subCase = triggerInitialActivity();
		// *****WHEN
		Task taskByWorkItemId = getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId());
		getTaskService().suspend(taskByWorkItemId.getId(), "ConstructionProjectManager");
		// *******THEN
		getPersistence().start();
		CaseInstance ci5 = reloadCaseInstance();
		assertEquals(PlanElementState.SUSPENDED, ci5.getPlanElementState());
		printState(" ", ci5);
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.SUSPENDED);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.SUSPENDED);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.SUSPENDED);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.SUSPENDED);
		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
		// assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.SUSPENDED);
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
		// assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.ACTIVE);
	}

	@Test
	public void testTaskLifecycleFailed() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		CaseInstance subCase = triggerInitialActivity();
		Task taskByWorkItemId = getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId());
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
		// assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.ACTIVE);
		assertEquals(PlanElementState.ACTIVE, reloadCaseInstance(subCase).getPlanElementState());
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
		Task taskByWorkItemId = getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId());
		getTaskService().exit(taskByWorkItemId.getId(), "ConstructionProjectManager");
		// *******THEN
		getPersistence().start();
		CaseInstance ci5 = reloadCaseInstance();
		assertEquals(PlanElementState.TERMINATED, ci5.getPlanElementState());
		printState(" ", ci5);
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.TERMINATED);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.TERMINATED);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.TERMINATED);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.TERMINATED);
		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
		// assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.TERMINATED);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.TERMINATED);
		assertNull(reloadCaseInstance(subCase));
		// reactivate
//		getTaskService().resume(taskByWorkItemId.getId(), "ConstructionProjectManager");
//		assertEquals(PlanElementState.ACTIVE, reloadCaseInstance(subCase).getPlanElementState());
//		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
//		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.AVAILABLE);
//		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.AVAILABLE);
//		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.ENABLED);
//		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.AVAILABLE);
//		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
//		// assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.AVAILABLE);
//		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.ACTIVE);
	}

	private CaseInstance triggerInitialActivity() {
		getPersistence().start();
		CaseInstance ci1 = reloadCaseInstance();
		ci1.signalEvent("StartUserEvent", new Object());
		printState(" ", ci1);
		getPersistence().commit();

		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.ENABLED);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
		// assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.ENABLED);
		assertEquals(PlanElementState.ACTIVE, ci1.getPlanElementState());// Because autoComplete defaults to false
		List<TaskSummary> subTasksByParent = getTaskService().getSubTasksByParent(getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId()).getId());
		assertEquals(2, subTasksByParent.size());
		CaseInstance subCase = startSubCaseTask(subTasksByParent);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.ACTIVE);
		return subCase;
	}

	private CaseInstance startSubCaseTask(List<TaskSummary> subTasksByParent) {
		for (TaskSummary taskSummary : subTasksByParent) {
			if (taskSummary.getName().equals("TheCaseTaskPlanItem")) {
				getTaskService().start(taskSummary.getId(), "ConstructionProjectManager");
				getPersistence().start();
				CaseTaskPlanItemInstance ni = (CaseTaskPlanItemInstance) reloadCaseInstance().findNodeForWorkItem(getTaskService().getTaskById(taskSummary.getId()).getTaskData().getWorkItemId());
				CaseInstance subCase = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(((CaseTaskPlanItemInstance) ni).getProcessInstanceId());
				getPersistence().commit();
				return subCase;
			}
		}
		return null;
	}

	private CaseInstance reloadCaseInstance() {
		return reloadCaseInstance(caseInstance);
	}

	private void completeTasks(List<TaskSummary> subTasksByParent) {
		for (TaskSummary taskSummary : subTasksByParent) {
			if (taskSummary.getName().equals("TheHumanTaskPlanItem")) {
				getTaskService().start(taskSummary.getId(), "Builder");
				getTaskService().complete(taskSummary.getId(), "Builder", new HashMap<String, Object>());
			} else if (taskSummary.getName().equals("TheCaseTaskPlanItem")) {
				getPersistence().start();
				CaseInstance ci3 = reloadCaseInstance();
				CaseTaskPlanItemInstance ctpi = (CaseTaskPlanItemInstance) ci3.findNodeForWorkItem(getTaskService().getTaskById(taskSummary.getId()).getTaskData().getWorkItemId());
				getRuntimeEngine().getKieSession().getProcessInstance(ctpi.getProcessInstanceId()).signalEvent("TheUserEvent", new Object());
				printState(" ", ci3);
				getPersistence().commit();
				assertEquals(PlanElementState.COMPLETED, ((CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(ctpi.getProcessInstanceId())).getPlanElementState());
				getPersistence().start();
				getRuntimeEngine().getKieSession().getProcessInstance(ctpi.getProcessInstanceId()).signalEvent(DefaultJoin.CLOSE, new Object());
				getPersistence().commit();
				assertNull(getRuntimeEngine().getKieSession().getProcessInstance(ctpi.getProcessInstanceId()));
			}
		}
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
		// assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.COMPLETED);

	}

	protected void printState(String s, PlanItemInstanceContainer pi) {
		System.out.println(pi);
		for (PlanItemInstanceLifecycle<?> ni : pi.getChildren()) {
			if (ni instanceof PlanItemInstanceLifecycle) {
				System.out.println(s + ni.getPlanItemName() + ":" + ni.getPlanElementState());
			} else {
				System.out.println(s + ni.getPlanItemName());
			}
			if (ni instanceof PlanItemInstanceContainer) {
				printState(s + " ", (PlanItemInstanceContainer) ni);
			}
		}
	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager("test/CaseInstanceTests.cmmn", "test/SubCase.cmmn");
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
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess("CaseInstanceTests", params);
		getPersistence().commit();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		assertNodeTriggered(caseInstance.getId(), "defaultSplit");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "WaitingForEndUserEventSentry");
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.AVAILABLE);
		// assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.AVAILABLE);
		assertEquals(PlanElementState.ACTIVE, caseInstance.getPlanElementState());

	}

}