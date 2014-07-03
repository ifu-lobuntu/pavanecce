package org.pavanecce.cmmn.jbpm.controllable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.junit.Test;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseTaskInstance;

import test.cmmn.ConstructionCase;
import test.cmmn.HousePlan;
import test.cmmn.RoofPlan;
import test.cmmn.WallPlan;

public class CaseTaskTest extends AbstractControllableLifecycleTest {
	{
		super.isJpa = true;
	}

	public CaseTaskTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	public String getEventGeneratingTaskUser() {
		return "ConstructionProjectManager";
	}

	@Override
	protected String getBusinessAdministratorUser() {
		return "ConstructionProjectManager";
	}

	@Override
	protected String getCaseOwner() {
		return "Spielman";
	}

	@Test
	public void testParameterMappings() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask(); // Creates a second wallPlan
		List<TaskSummary> list = getTaskService().getTasksAssignedAsPotentialOwner("ConstructionProjectManager", "en-UK");
		assertEquals(2, list.size());
		long subTaskId = -1;
		for (TaskSummary taskSummary : list) {
			if (taskSummary.getName().equals("TheEventGeneratingTaskPlanItem")) {
				subTaskId = taskSummary.getId();
			}
		}
		getRuntimeEngine().getTaskService().start(subTaskId, "ConstructionProjectManager");
		// *******THEN
		getPersistence().start();
		long id = getSubProcessInstanceId(subTaskId);
		CaseInstance pi = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(id);
		HousePlan housePlan = (HousePlan) pi.getVariable("housePlan");
		@SuppressWarnings("unchecked")
		Collection<WallPlan> wallPlans = (Collection<WallPlan>) pi.getVariable("wallPlans");
		assertEquals(super.housePlan.getId(), housePlan.getId());
		assertEquals(2, wallPlans.size());
		for (WallPlan wallPlan : wallPlans) {
			assertEquals("I Am Transformed", wallPlan.getShortDescription());
		}
		getPersistence().commit();
		// *****THEN
	}

	@Override
	public String[] getProcessFileNames() {
		return new String[] { "test/controllable/CaseTaskTests.cmmn", "test/SubCase.cmmn" };
	}

	@Override
	public String getNameOfProcessToStart() {
		return "CaseTaskTests";
	}

	@Override
	public void failTask(long taskId) {
		getPersistence().start();
		long subProccessInstanceId = getSubProcessInstanceId(taskId);
		if (subProccessInstanceId >= 0) {
			getRuntimeEngine().getKieSession().abortProcessInstance(subProccessInstanceId);
		}
		getPersistence().commit();
	}

	private long getSubProcessInstanceId(long taskId) {
		long workItemId = getRuntimeEngine().getTaskService().getTaskById(taskId).getTaskData().getWorkItemId();
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		long subProccessInstanceId = -1;
		for (NodeInstance nodeInstance : caseInstance.getNodeInstances()) {
			if (nodeInstance instanceof CaseTaskInstance && ((CaseTaskInstance) nodeInstance).getWorkItemId() == workItemId) {
				subProccessInstanceId = ((CaseTaskInstance) nodeInstance).getProcessInstanceId();
			}
		}
		getPersistence().commit();
		return subProccessInstanceId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void completeTask(long taskId) {
		getPersistence().start();
		long subProcessInstanceId = getSubProcessInstanceId(taskId);
		// Lets mess around with the caseFile
		ConstructionCase otherCase = new ConstructionCase();
		HousePlan otherHousePlan = new HousePlan(otherCase);
		Set<WallPlan> newWallPlans = new HashSet<WallPlan>();
		newWallPlans.add(new WallPlan(otherHousePlan));
		newWallPlans.add(new WallPlan(otherHousePlan));
		newWallPlans.add(new WallPlan(otherHousePlan));
		RoofPlan newRoofPlan = new RoofPlan(otherHousePlan);
		getPersistence().persist(otherCase);
		CaseInstance sp = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(subProcessInstanceId);
		sp.setVariable("wallPlans", newWallPlans);
		sp.setVariable("roofPlan", newRoofPlan);
		getPersistence().commit();
		// Now complete the Case
		getPersistence().start();
		getRuntimeEngine().getKieSession().signalEvent("TheUserEvent", new Object(), subProcessInstanceId);
		getPersistence().commit();
		getPersistence().start();
		sp = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(subProcessInstanceId);
		assertEquals(PlanElementState.COMPLETED, sp.getPlanElementState());
		assertEquals(Status.Completed, getTaskService().getTaskByWorkItemId(sp.getWorkItemId()).getTaskData().getStatus());
		getPersistence().commit();
		// now look at the result - those new WallPlans must be added to the original housePlan due to the refinement
		getPersistence().start();
		this.housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		assertTrue(this.housePlan.getWallPlans().containsAll(newWallPlans));
		assertEquals(5, this.housePlan.getWallPlans().size());
		assertEquals(newRoofPlan, this.housePlan.getRoofPlan());
		getPersistence().commit();
		// Check the task's result
		Task task = getTaskService().getTaskById(taskId);
		Content outputContent = getTaskService().getContentById(task.getTaskData().getOutputContentId());
		getPersistence().start();
		Map<String, Object> outputAsMap = (Map<String, Object>) ContentMarshallerHelper.unmarshall(outputContent.getContent(), getTaskService()
				.getMarshallerContext(task).getEnvironment());
		Collection<WallPlan> wallPlanTaskOutput = (Collection<WallPlan>) outputAsMap.get("wallPlanTaskOutput");
		assertEquals(3, wallPlanTaskOutput.size());
		for (WallPlan wallPlan : wallPlanTaskOutput) {
			assertEquals("I Am Transformed Twice", wallPlan.getShortDescription());
		}
		assertEquals(newRoofPlan, outputAsMap.get("roofPlanTaskOutput"));
		getPersistence().commit();

	}

}
