package org.pavanecce.cmmn.jbpm.occurrable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.AbstractConstructionTestCase;
import org.pavanecce.cmmn.jbpm.TaskParameters;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

import test.cmmn.ConstructionCase;
import test.cmmn.House;
import test.cmmn.HousePlan;

public abstract class AbstractOccurrableTestCase extends AbstractConstructionTestCase {

	{
		super.isJpa = true;
	}

	protected abstract void triggerOccurrence() throws Exception;

	public AbstractOccurrableTestCase() {
		super();
	}

	public AbstractOccurrableTestCase(boolean setupDataSource, boolean sessionPersistence, String persistenceUnitName) {
		super(setupDataSource, sessionPersistence, persistenceUnitName);
	}

	public AbstractOccurrableTestCase(boolean setupDataSource, boolean sessionPersistence) {
		super(setupDataSource, sessionPersistence);
	}

	@Test
	public void testSuspend() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		getPersistence().start();
		PlanItemInstance<?> piil = null;
		for (NodeInstance ni : reloadCaseInstance(caseInstance).getNodeInstances()) {
			if (ni instanceof PlanItemInstance && ((PlanItemInstance<?>) ni).getItem().getEffectiveName().equals("TheOccurrablePlanItem")) {
				piil = (PlanItemInstance<?>) ni;
			}
		}
		piil.suspend();
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheOccurrablePlanItem", PlanElementState.SUSPENDED);
		// *****THEN
		List<TaskSummary> tasks = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertTaskTypeCreated(tasks, "TaskEnteredOnSuspensionOfOccurrablePlanItem");
		assertNodeTriggered(caseInstance.getId(), "TaskEnteredOnSuspensionOfOccurrablePlanItem");
		assertPlanItemInState(caseInstance.getId(), "TaskEnteredOnSuspensionOfOccurrablePlanItem", PlanElementState.ENABLED);
		triggerOccurrence();
		assertNodeNotTriggered(caseInstance.getId(), "TaskEnteredOnOccurrenceOfOccurrablePlanItem");
	}

	@Test
	public void testTerminate() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		getPersistence().start();
		PlanItemInstance<?> piil = null;
		for (NodeInstance ni : reloadCaseInstance(caseInstance).getNodeInstances()) {
			if (ni instanceof PlanItemInstance && ((PlanItemInstance<?>) ni).getItem().getEffectiveName().equals("TheOccurrablePlanItem")) {
				piil = (PlanItemInstance<?>) ni;
			}
		}
		piil.terminate();
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheOccurrablePlanItem", PlanElementState.TERMINATED);
		// *****THEN
		List<TaskSummary> tasks = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertTaskTypeCreated(tasks, "TaskEnteredOnTerminationOfOccurrablePlanItem");
		assertNodeTriggered(caseInstance.getId(), "TaskEnteredOnTerminationOfOccurrablePlanItem");
		assertPlanItemInState(caseInstance.getId(), "TaskEnteredOnTerminationOfOccurrablePlanItem", PlanElementState.ENABLED);
		triggerOccurrence();
		assertNodeNotTriggered(caseInstance.getId(), "TaskEnteredOnOccurrenceOfOccurrablePlanItem");
	}

	@Test
	public void testParentTerminate() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		getPersistence().start();
		PlanItemInstance<?> piil = null;
		for (NodeInstance ni : reloadCaseInstance(caseInstance).getNodeInstances()) {
			if (ni instanceof PlanItemInstance && ((PlanItemInstance<?>) ni).getItem().getEffectiveName().equals("TheOccurrablePlanItem")) {
				piil = (PlanItemInstance<?>) ni;
			}
		}
		piil.parentTerminate();
		getPersistence().commit();
		assertPlanItemInState(caseInstance.getId(), "TheOccurrablePlanItem", PlanElementState.TERMINATED);
		// *****THEN
		List<TaskSummary> tasks = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertTaskTypeCreated(tasks, "TaskEnteredOnParentTerminationOfOccurrablePlanItem");
		assertNodeTriggered(caseInstance.getId(), "TaskEnteredOnParentTerminationOfOccurrablePlanItem");
		assertPlanItemInState(caseInstance.getId(), "TaskEnteredOnParentTerminationOfOccurrablePlanItem", PlanElementState.ENABLED);
		triggerOccurrence();
		assertNodeNotTriggered(caseInstance.getId(), "TaskEnteredOnOccurrenceOfOccurrablePlanItem");
	}

	@Test
	public void testResume() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		getPersistence().start();
		PlanItemInstance<?> piil = null;
		for (NodeInstance ni : reloadCaseInstance(caseInstance).getNodeInstances()) {
			if (ni instanceof PlanItemInstance && ((PlanItemInstance<?>) ni).getItem().getEffectiveName().equals("TheOccurrablePlanItem")) {
				piil = (PlanItemInstance<?>) ni;
			}
		}
		piil.suspend();

		// *****WHEN
		piil.resume();
		getPersistence().commit();

		assertPlanItemInState(caseInstance.getId(), "TheOccurrablePlanItem", PlanElementState.AVAILABLE);
		// *****THEN
		List<TaskSummary> tasks = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertTaskTypeCreated(tasks, "TaskEnteredOnResumptionOfOccurrablePlanItem");
		assertNodeTriggered(caseInstance.getId(), "TaskEnteredOnResumptionOfOccurrablePlanItem");
		assertPlanItemInState(caseInstance.getId(), "TaskEnteredOnResumptionOfOccurrablePlanItem", PlanElementState.ENABLED);
		triggerOccurrence();
		assertNodeTriggered(caseInstance.getId(), "TaskEnteredOnOccurrenceOfOccurrablePlanItem");
	}

	@Test
	public void testCreate() throws Exception {
		// *****GIVEN/WHEN
		givenThatTheTestCaseIsStarted();
		assertPlanItemInState(caseInstance.getId(), "TheOccurrablePlanItem", PlanElementState.AVAILABLE);
		// *****THEN
		List<TaskSummary> tasks = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertTaskTypeCreated(tasks, "TaskEnteredOnCreationOfOccurrablePlanItem");
		assertNodeTriggered(caseInstance.getId(), "TaskEnteredOnCreationOfOccurrablePlanItem");
		assertPlanItemInState(caseInstance.getId(), "TaskEnteredOnCreationOfOccurrablePlanItem", PlanElementState.ENABLED);
	}

	@Test
	public void testOccurrence() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		assertPlanItemInState(caseInstance.getId(), "TheOccurrablePlanItem", PlanElementState.AVAILABLE);
		// *****WHEN
		triggerOccurrence();
		// *****THEN
		List<TaskSummary> tasks = getTaskService().getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertTaskTypeCreated(tasks, "TaskEnteredOnOccurrenceOfOccurrablePlanItem");
		assertNodeTriggered(caseInstance.getId(), "TheOccurrablePlanItem");
		assertNodeTriggered(caseInstance.getId(), "TaskEnteredOnOccurrenceOfOccurrablePlanItem");
		assertPlanItemInState(caseInstance.getId(), "TheOccurrablePlanItem", PlanElementState.COMPLETED);
		assertPlanItemInState(caseInstance.getId(), "TaskEnteredOnOccurrenceOfOccurrablePlanItem", PlanElementState.ENABLED);

	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager(getProcessFile());
		Map<String, Object> params = new HashMap<String, Object>();
		getPersistence().start();

		ConstructionCase cc = new ConstructionCase("/cases/case1");
		housePlan = new HousePlan(cc);
		house = new House(cc);
		getPersistence().persist(cc);
		getPersistence().commit();
		params.put("housePlan", housePlan);
		params.put("house", house);
		params.put(TaskParameters.INITIATOR, "Spielman");
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess(getCaseName(), params);
		getPersistence().commit();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		assertNodeTriggered(caseInstance.getId(), "defaultSplit");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "onTheOccurrablePlanItemOccurPartId");
		getPersistence().commit();
	}

	public abstract String getProcessFile();

	public abstract String getCaseName();

}