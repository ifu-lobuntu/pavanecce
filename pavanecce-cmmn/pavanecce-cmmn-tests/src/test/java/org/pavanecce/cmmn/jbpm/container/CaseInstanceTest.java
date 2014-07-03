package org.pavanecce.cmmn.jbpm.container;

import java.util.Map;

import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.junit.Test;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.ContentMarshallerContext;
import org.pavanecce.cmmn.jbpm.flow.DefaultJoin;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstanceContainer;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

import test.cmmn.WallPlan;

public class CaseInstanceTest extends AbstractPlanItemInstanceContainerLifecycleTest {

	{
		super.isJpa = true;
	}

	public CaseInstanceTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void exitCriteria() {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		CaseInstance subCase = triggerInitialActivity();
		// *****WHEN
		getRuntimeEngine().getKieSession().signalEvent("EndUserEvent", new Object(), caseInstance.getId());
		// *******THEN
		getPersistence().start();
		PlanItemInstanceContainer piic = getPlanItemInstanceContainer();
		assertEquals(PlanElementState.TERMINATED, piic.getPlanElementState());
		printState(" ", piic);
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TheTimerEventPlanItem", PlanElementState.TERMINATED);
		assertPlanItemInState(caseInstance.getId(), "TheUserEventPlanItem", PlanElementState.TERMINATED);
		assertPlanItemInState(caseInstance.getId(), "TheHumanTaskPlanItem", PlanElementState.TERMINATED);
		assertPlanItemInState(caseInstance.getId(), "EndUserEventPlanItem", PlanElementState.COMPLETED);
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

	@SuppressWarnings("unchecked")
	protected void testCloseAndOutput(PlanItemInstanceContainer piic) {
		// and close it
		if (piic instanceof CaseInstance) {
			getPersistence().start();
			CaseInstance ci = reloadCaseInstance();
			ci.signalEvent(DefaultJoin.CLOSE, new Object());
			assertEquals(PlanElementState.CLOSED, ci.getPlanElementState());
			assertNull(getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId()));
			Task task = getTaskService().getTaskByWorkItemId(caseInstance.getWorkItemId());
			Content output = getTaskService().getContentById(task.getTaskData().getOutputContentId());
			ContentMarshallerContext mc = getTaskService().getMarshallerContext(task);
			Map<String, Object> result = (Map<String, Object>) ContentMarshallerHelper
					.unmarshall(output.getContent(), mc.getEnvironment(), mc.getClassloader());
			assertTrue(result.get("theResultingWallPlan") instanceof WallPlan);
			getPersistence().commit();
		}
		// *****THEN
	}

	@Override
	protected void ensurePlanItemContainerIsStarted() {

	}

	@Override
	public String getCaseName() {
		return "CaseInstanceTests";
	}

	@Override
	public String getProcessFile() {
		return "test/container/CaseInstanceTests.cmmn";
	}

}