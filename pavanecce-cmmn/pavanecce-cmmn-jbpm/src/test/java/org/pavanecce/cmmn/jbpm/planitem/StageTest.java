package org.pavanecce.cmmn.jbpm.planitem;

import java.util.List;

import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.flow.StagePlanItem;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;
import org.pavanecce.cmmn.jbpm.instance.impl.StagePlanItemInstance;

import test.House;
import test.Wall;

public class StageTest extends AbstractTasklifecycleTests {
	{
		super.isJpa = true;
	}

	public StageTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	// @Test
	// public void testStageTriggered() throws Exception {
	// // *****GIVEN
	// givenThatTheTestCaseIsStarted();
	// triggerStartOfTask();
	// assertNodeTriggered(caseInstance.getId(), "TopLevelTask");
	// List<TaskSummary> list = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
	// assertEquals(1, list.size());
	// getTaskService().start(list.get(0).getId(), "Builder");
	//
	// // *****WHEN
	// getTaskService().complete(list.get(0).getId(), "Builder", new HashMap<String,Object>());
	// // *****THEN
	// assertNodeTriggered(caseInstance.getId(), "TheStagePlanItem");
	// list = getTaskService().getTasksAssignedAsPotentialOwner("Administrator", "en-UK");
	// assertEquals(2, list.size());//Tasks representing Stage and the Case
	// assertTaskTypeCreated(list, "TheStagePlanItem");
	// }

	@Override
	public void failTask(long taskId) {
		getPersistence().start();
		long wi = getTaskService().getTaskById(taskId).getTaskData().getWorkItemId();
		StagePlanItemInstance spi= (StagePlanItemInstance) reloadCaseInstance(caseInstance).findNodeForWorkItem(wi);
		spi.fault();
		getPersistence().commit();
	}

	@Override
	public void completeTask(long taskId) {
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("StageCompletingEvent", new Object(), caseInstance.getId());
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheMilestonePlanItem", PlanElementState.COMPLETED);
	}

	@Override
	public String getEventGeneratingTaskUser() {
		return "ConstructionProjectManager";
	}

	@Override
	protected String getCaseOwner() {
		return "Spielman";
	}

	@Override
	public String getNameOfProcessToStart() {
		return "StageTests";
	}

	@Override
	public String[] getProcessFileNames() {
		return new String[] { "test/StageTests.cmmn" };
	}

}
