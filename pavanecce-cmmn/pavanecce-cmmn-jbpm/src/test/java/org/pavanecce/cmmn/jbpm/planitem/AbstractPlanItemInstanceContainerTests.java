package org.pavanecce.cmmn.jbpm.planitem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.AbstractConstructionTestCase;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.DefaultJoin;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceContainerLifecycle;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.instance.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.CaseTaskPlanItemInstance;

import test.ConstructionCase;
import test.House;
import test.HousePlan;

public abstract class AbstractPlanItemInstanceContainerTests extends AbstractConstructionTestCase {

	protected HousePlan housePlan;
	protected House house;
	protected CaseInstance caseInstance;

	public abstract String getProcessFile();

	public abstract String getCaseName();

	protected abstract void ensurePlanItemContainerIsStarted();

	public AbstractPlanItemInstanceContainerTests() {
		super();
	}

	public AbstractPlanItemInstanceContainerTests(boolean setupDataSource, boolean sessionPersistence, String persistenceUnitName) {
		super(setupDataSource, sessionPersistence, persistenceUnitName);
	}

	public AbstractPlanItemInstanceContainerTests(boolean setupDataSource, boolean sessionPersistence) {
		super(setupDataSource, sessionPersistence);
	}

	protected PlanItemInstanceContainerLifecycle getPlanItemInstanceContainer() {
		return reloadCaseInstance();
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
		assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.ENABLED);
		assertEquals(PlanElementState.ACTIVE, ci1.getPlanElementState());// Because autoComplete defaults to false
		List<TaskSummary> subTasksByParent = getTaskService().getSubTasksByParent(getTaskService().getTaskByWorkItemId(getWorkitemId()).getId());
		assertEquals(3, subTasksByParent.size());
		CaseInstance subCase = startSubCaseTask(subTasksByParent);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.ACTIVE);
		return subCase;
	}

	public long getWorkitemId() {
		return caseInstance.getWorkItemId();
	}

	private CaseInstance startSubCaseTask(List<TaskSummary> subTasksByParent) {
		for (TaskSummary taskSummary : subTasksByParent) {
			if (taskSummary.getName().equals("TheCaseTaskPlanItem")) {
				getTaskService().start(taskSummary.getId(), "ConstructionProjectManager");
				getPersistence().start();
				long workItemId = getTaskService().getTaskById(taskSummary.getId()).getTaskData().getWorkItemId();
				CaseTaskPlanItemInstance ni = (CaseTaskPlanItemInstance) reloadCaseInstance().findNodeForWorkItem(workItemId);
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
				long workItemId = getTaskService().getTaskById(taskSummary.getId()).getTaskData().getWorkItemId();
				CaseTaskPlanItemInstance ctpi = (CaseTaskPlanItemInstance) ci3.findNodeForWorkItem(workItemId);
				getRuntimeEngine().getKieSession().getProcessInstance(ctpi.getProcessInstanceId()).signalEvent("TheUserEvent", new Object());
				printState(" ", ci3);
				getPersistence().commit();
				assertEquals(PlanElementState.COMPLETED, ((CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(ctpi.getProcessInstanceId())).getPlanElementState());
				getPersistence().start();
				getRuntimeEngine().getKieSession().getProcessInstance(ctpi.getProcessInstanceId()).signalEvent(DefaultJoin.CLOSE, new Object());
				getPersistence().commit();
				assertNull(getRuntimeEngine().getKieSession().getProcessInstance(ctpi.getProcessInstanceId()));
			} else if (taskSummary.getName().equals("TheStagePlanItem")) {
				getPersistence().start();
				getRuntimeEngine().getKieSession().signalEvent("StageCompletingEvent", new Object(), caseInstance.getId());
				getPersistence().commit();
				assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItemInTheStage", PlanElementState.COMPLETED);
			}
		}
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.COMPLETED);

	}

	protected void printState(String s, PlanItemInstanceContainerLifecycle pi) {
		if (true)
			return;
		System.out.println(pi);
		for (PlanItemInstanceLifecycle<?> ni : pi.getChildren()) {
			if (ni instanceof PlanItemInstanceLifecycle) {
				System.out.println(s + ni.getPlanItemName() + ":" + ni.getPlanElementState());
			} else {
				System.out.println(s + ni.getPlanItemName());
			}
			if (ni instanceof PlanItemInstanceContainerLifecycle) {
				printState(s + " ", (PlanItemInstanceContainerLifecycle) ni);
			}
		}
	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager(getProcessFile(), "test/SubCase.cmmn");
		Map<String, Object> params = new HashMap<String, Object>();
		getPersistence().start();

		ConstructionCase cc = new ConstructionCase("/cases/case1");
		housePlan = new HousePlan(cc);
		house = new House(cc);
		getPersistence().persist(cc);
		getPersistence().commit();
		params.put("housePlan", housePlan);
		params.put("house", house);
		params.put(Case.CASE_OWNER, "Spielman");
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess(getCaseName(), params);
		getPersistence().commit();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		assertNodeTriggered(caseInstance.getId(), "defaultSplit");
		getPersistence().start();
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "StartUserEventPlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheStagePlanItem", PlanElementState.AVAILABLE);
		assertPlanItemInState(caseInstance.getId(), "TheCaseTaskPlanItem", PlanElementState.AVAILABLE);
		assertEquals(PlanElementState.ACTIVE, caseInstance.getPlanElementState());
		ensurePlanItemContainerIsStarted();

	}

}