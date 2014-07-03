package org.pavanecce.cmmn.jbpm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

import test.cmmn.ConstructionCase;
import test.cmmn.House;
import test.cmmn.HousePlan;
import test.cmmn.Wall;
import test.cmmn.WallPlan;

public class BuilderTest extends AbstractConstructionTestCase {

	public BuilderTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void testSimpleEntryCriteria() throws Exception {
		createRuntimeManager("test/hello.cmmn");
		RuntimeEngine runtimeEngine = getRuntimeEngine();
		KieSession ksession = runtimeEngine.getKieSession();
		TaskService taskService = runtimeEngine.getTaskService();
		Map<String, Object> params = new HashMap<String, Object>();
		getPersistence().start();
		ConstructionCase cc = new ConstructionCase("/cases/case1");
		HousePlan housePlan = new HousePlan(cc);
		House house = new House(cc);
		getPersistence().persist(cc);
		getPersistence().commit();
		params.put("housePlan", housePlan);
		params.put("house", house);
		params.put(TaskParameters.INITIATOR, "Spielman");
		getPersistence().start();
		CaseInstance processInstance = (CaseInstance) ksession.startProcess("hello", params);
		getPersistence().commit();
		assertProcessInstanceActive(processInstance.getId(), ksession);
		assertNodeTriggered(processInstance.getId(), "defaultSplit");
		// Sentries:
		assertNodeTriggered(processInstance.getId(), "WaitingForWallPlanCreated");
		assertNodeTriggered(processInstance.getId(), "WaitingForFoundationLaid");
		addWallPlan(housePlan);
		addWallPlan(housePlan);
		assertNodeTriggered(processInstance.getId(), "LayFoundationPlanItem");
		// // let Builder execute LayFoundationPlanItem
		List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertEquals("LayFoundationPlanItem", list.get(0).getName());
		assertEquals("LayFoundationPlanItem", list.get(1).getName());
		getPersistence().start();
		processInstance = (CaseInstance) ksession.getProcessInstance(processInstance.getId());
		assertNodeActive(processInstance.getId(), ksession, "WaitingForWallPlanCreated");
		assertNodeActive(processInstance.getId(), ksession, "WaitingForFoundationLaid");
		assertNodeActive(processInstance.getId(), ksession, "LayFoundationPlanItem");
		getPersistence().commit();
		taskService.start(list.get(0).getId(), "Builder");
		taskService.complete(list.get(0).getId(), "Builder", new HashMap<String, Object>());
		getPersistence().start();
		assertNodeActive(processInstance.getId(), ksession, "LayBricksPlanItem");
		assertNodeActive(processInstance.getId(), ksession, "LayFoundationPlanItem");
		getPersistence().commit();
		getPersistence().start();
		taskService.start(list.get(1).getId(), "Builder");
		taskService.complete(list.get(1).getId(), "Builder", new HashMap<String, Object>());
		getPersistence().commit();

		list = taskService.getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertEquals("LayBricksPlanItem", list.get(0).getName());
		assertEquals("LayBricksPlanItem", list.get(1).getName());
		getPersistence().start();
		taskService.start(list.get(0).getId(), "Builder");
		taskService.complete(list.get(0).getId(), "Builder", new HashMap<String, Object>());
		getPersistence().commit();
		list = taskService.getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());
		taskService.start(list.get(0).getId(), "Builder");
		getPersistence().start();
		taskService.complete(list.get(0).getId(), "Builder", new HashMap<String, Object>());
		getPersistence().commit();

	}

	@Test
	public void testBuildWallExitCritera() throws Exception {
		createRuntimeManager("test/bye.cmmn");
		RuntimeEngine runtimeEngine = getRuntimeEngine();
		KieSession ksession = runtimeEngine.getKieSession();
		TaskService taskService = runtimeEngine.getTaskService();
		Map<String, Object> params = new HashMap<String, Object>();
		getPersistence().start();
		Node node = getOcmFactory().getCurrentObjectContentManager().getSession().getNode("/cases");
		assertNotNull(node);
		ConstructionCase cc = new ConstructionCase("/cases/case1");
		HousePlan housePlan = new HousePlan(cc);
		House house = new House(cc);
		getPersistence().persist(cc);
		getPersistence().commit();
		params.put("housePlan", housePlan);
		params.put("house", house);
		params.put(TaskParameters.INITIATOR, "Spielman");
		getPersistence().start();
		CaseInstance processInstance = (CaseInstance) ksession.startProcess("bye", params);
		getPersistence().commit();
		assertProcessInstanceActive(processInstance.getId(), ksession);
		assertNodeTriggered(processInstance.getId(), "defaultSplit");
		// Sentries:
		assertNodeTriggered(processInstance.getId(), "OnWallCreateOnlyPart");
		assertNodeTriggered(processInstance.getId(), "OnWallPlanCreatePart");
		assertNodeTriggered(processInstance.getId(), "OnRoofPlanCreatePart");
		addWallPlan(housePlan);
		assertNodeTriggered(processInstance.getId(), "BuildWallPlanItem");
		// // let Builder execute LayFoundationPlanItem
		List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());
		assertEquals("BuildWallPlanItem", list.get(0).getName());

		// exit criterion
		addWall(house);
		list = taskService.getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(0, list.size());
		// TODO:l
		// When all planItems complete, check the process completes
		// TaskSummary task = list.get(0);
		// logger.info("John is executing task {}", task.getName());
		// taskService.start(task.getId(), "john");
		// taskService.complete(task.getId(), "john", null);
		//
		// assertNodeTriggered(processInstance.getId(), "End");
		// assertProcessInstanceCompleted(processInstance.getId(), ksession);
	}

	private void addWallPlan(HousePlan housePlan) throws Exception {
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new WallPlan(housePlan);
		getPersistence().update(housePlan);
		getPersistence().commit();
	}

	private void addWall(House house) throws Exception {
		house = getPersistence().find(House.class, house.getId());
		new Wall(house);
		getPersistence().update(house);
		getPersistence().commit();
	}

}
