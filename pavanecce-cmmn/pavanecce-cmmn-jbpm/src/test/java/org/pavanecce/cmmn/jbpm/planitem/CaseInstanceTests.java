package org.pavanecce.cmmn.jbpm.planitem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.junit.Test;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.InternalTaskService;
import org.pavanecce.cmmn.jbpm.AbstractConstructionTestCase;
import org.pavanecce.cmmn.jbpm.flow.DefaultJoin;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;
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
	public void testCaseLifecycleComplete() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		// *******THEN
		getPersistence().start();
		CaseInstance ci0 = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		printState(" ", ci0);
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

		getPersistence().start();
		CaseInstance ci1 = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
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
		InternalTaskService ts = (InternalTaskService) getRuntimeEngine().getTaskService();
		Task planningTask = ts.getTaskByWorkItemId(caseInstance.getWorkItemId());
		List<TaskSummary> subTasksByParent = ts.getSubTasksByParent(planningTask.getId());
		assertEquals(2, subTasksByParent.size());

		getPersistence().start();
		CaseInstance ci2 = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		System.out.println(ci2.getPlanElementState());
		assertFalse(ci2.canComplete());
		printState(" ", ci2);
		getPersistence().commit();
		for (TaskSummary taskSummary : subTasksByParent) {
			if (taskSummary.getName().equals("TheHumanTaskPlanItem")) {
				ts.start(taskSummary.getId(), "Builder");
				ts.complete(taskSummary.getId(), "Builder", new HashMap<String, Object>());
			} else if (taskSummary.getName().equals("TheCaseTaskPlanItem")) {
				ts.start(taskSummary.getId(), "ConstructionProjectManager");
				getPersistence().start();
				CaseInstance ci3 = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
				CaseTaskPlanItemInstance ctpi = (CaseTaskPlanItemInstance) ci3.findNodeForWorkItem(ts.getTaskById(taskSummary.getId()).getTaskData().getWorkItemId());
				getRuntimeEngine().getKieSession().getProcessInstance(ctpi.getProcessInstanceId()).signalEvent("TheUserEvent", new Object());
				printState(" ", ci3);
				getPersistence().commit();
				assertEquals(PlanElementState.COMPLETED, ((CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(ctpi.getProcessInstanceId())).getPlanElementState());
				getPersistence().start();
				getRuntimeEngine().getKieSession().getProcessInstance(ctpi.getProcessInstanceId()).signalEvent(DefaultJoin.CLOSE, new Object());
				getPersistence().commit();
				assertNull(getRuntimeEngine().getKieSession().getProcessInstance(ctpi.getProcessInstanceId()));
				Task caseTask= ts.getTaskById(taskSummary.getId());
			}
		}
		getPersistence().start();
		CaseInstance ci4 = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		System.out.println(ci4.getPlanElementState());
		assertTrue(ci4.canComplete());
		printState(" ", ci4);
		getPersistence().commit();
		// *****THEN
	}

	protected void printState(String s, NodeInstanceContainer pi) {
		for (NodeInstance ni : pi.getNodeInstances()) {
			if (ni instanceof PlanItemInstanceLifecycle) {
				System.out.println(s + ni.getNodeName() + ":" + ((PlanItemInstanceLifecycle<?>) ni).getPlanElementState());
			} else {
				System.out.println(s + ni.getNodeName());
			}
			if (ni instanceof NodeInstanceContainer) {
				printState(s + " ", (NodeInstanceContainer) ni);
			}

		}
	}

	public void testTaskLifecycleTerminate() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		// *******THEN
	}

	public void testTaskLifecycleFailed() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();

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
	}

}