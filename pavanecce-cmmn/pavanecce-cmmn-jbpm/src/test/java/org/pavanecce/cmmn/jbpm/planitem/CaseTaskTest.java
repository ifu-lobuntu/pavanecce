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
	public String getEventGeneratingTaskRole() {
		return "ConstructionProjectManager";
	}
	@Override
	protected String getBusinessAdministratorRole() {
		return "ConstructionProjectManager";
	}
	@Test
	public void testParameterMappings() throws Exception{
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(getEventGeneratingTaskRole(), "en-UK");
		
		assertEquals(1, list.size());
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "ConstructionProjectManager");
		// *******THEN
		getPersistence().start();
		long id = getSubProcessInstanceId(list.get(0).getId());
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
