package org.pavanecce.cmmn.jbpm.planitem;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.instance.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.CaseTaskPlanItemInstance;

import test.HousePlan;
import test.WallPlan;

public class CaseTaskTest extends AbstractTasklifecycleTests {
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
	public void testParameterMappings() throws Exception{
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getTaskService().getTasksAssignedAsPotentialOwner("ConstructionProjectManager", "en-UK");
		assertEquals(2, list.size());
		long subTaskId=-1;
		for (TaskSummary taskSummary : list) {
			if(taskSummary.getName().equals("TheEventGeneratingTaskPlanItem")){
				subTaskId=taskSummary.getId();
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
		assertEquals(1, wallPlans.size());
		getPersistence().commit();
		// *****THEN
	}
	@Override
	public String[] getProcessFileNames() {
		return new String[] { "test/CaseTaskTests.cmmn", "test/SubCase.cmmn" };
	}

	@Override
	public String getNameOfProcessToStart() {
		return "CaseTaskTests";
	}

	@Override
	public void failTask(long taskId) {
		long subProccessInstanceId = getSubProcessInstanceId(taskId);
		if (subProccessInstanceId >= 0) {
			getRuntimeEngine().getKieSession().abortProcessInstance(subProccessInstanceId);
		}
	}

	private long getSubProcessInstanceId(long taskId) {
		long workItemId = getRuntimeEngine().getTaskService().getTaskById(taskId).getTaskData().getWorkItemId();
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		long subProccessInstanceId = -1;
		for (NodeInstance nodeInstance : caseInstance.getNodeInstances()) {
			if (nodeInstance instanceof CaseTaskPlanItemInstance && ((CaseTaskPlanItemInstance) nodeInstance).getWorkItemId() == workItemId) {
				subProccessInstanceId = ((CaseTaskPlanItemInstance) nodeInstance).getProcessInstanceId();
			}
		}
		getPersistence().commit();
		return subProccessInstanceId;
	}

	@Override
	public void completeTask(long taskId) {
		getPersistence().start();
		long subProcessInstanceId = getSubProcessInstanceId(taskId);
		getRuntimeEngine().getKieSession().signalEvent("TheUserEvent", new Object(), subProcessInstanceId);
		ProcessInstance sp = getRuntimeEngine().getKieSession().getProcessInstance(subProcessInstanceId);
		getPersistence().commit();
	}

}
