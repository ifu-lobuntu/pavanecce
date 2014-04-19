package org.pavanecce.cmmn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.shared.services.impl.events.JbpmServicesEventListener;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.EventService;
import org.kie.internal.task.api.model.NotificationEvent;
import org.pavanecce.cmmn.flow.builder.CMMNBuilder;
import org.pavanecce.cmmn.instance.CaseInstance;
import org.pavanecce.cmmn.instance.CaseTaskLifecycleListener;
import org.pavanecce.cmmn.test.domain.ConstructionCase;
import org.pavanecce.cmmn.test.domain.House;
import org.pavanecce.cmmn.test.domain.HousePlan;
import org.pavanecce.cmmn.test.domain.Wall;
import org.pavanecce.cmmn.test.domain.WallPlan;

public class BuilderTest extends AbstrasctJbpmCaseBaseTestCase {

	public BuilderTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleEntryCriteria() throws Exception {
		createRuntimeManager("test/hello.cmmn");
		RuntimeEngine runtimeEngine = getRuntimeEngine();
		KieSession ksession = runtimeEngine.getKieSession();
		EventService<JbpmServicesEventListener<NotificationEvent>,JbpmServicesEventListener<Task>> eventService = (EventService<JbpmServicesEventListener<NotificationEvent>,JbpmServicesEventListener<Task>>) runtimeEngine.getTaskService();
		eventService.registerTaskLifecycleEventListener(new CaseTaskLifecycleListener(ksession));
		TaskService taskService = runtimeEngine.getTaskService();
		Map<String, Object> params = new HashMap<String, Object>();
		getPersistence().start();

		ConstructionCase cc = new ConstructionCase("/cases/case1");
		HousePlan housePlan = new HousePlan(cc);
		getPersistence().persist(cc);
		getPersistence().commit();
		params.put("housePlan", Arrays.asList(housePlan));
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
		taskService.start(list.get(1).getId(), "Builder");
		taskService.complete(list.get(1).getId(), "Builder", new HashMap<String, Object>());

		list = taskService.getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertEquals("LayBricksPlanItem", list.get(0).getName());
		assertEquals("LayBricksPlanItem", list.get(1).getName());
		taskService.start(list.get(0).getId(), "Builder");
		taskService.complete(list.get(0).getId(), "Builder", new HashMap<String, Object>());
		list = taskService.getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());
		System.out.println(list.get(0).getStatus());
		taskService.start(list.get(0).getId(), "Builder");
		taskService.complete(list.get(0).getId(), "Builder", new HashMap<String, Object>());

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

	@SuppressWarnings("unchecked")
	@Test
	public void testBuildWallExitCritera() throws Exception {
		createRuntimeManager("test/bye.cmmn");
		RuntimeEngine runtimeEngine = getRuntimeEngine();
		KieSession ksession = runtimeEngine.getKieSession();
		EventService<JbpmServicesEventListener<NotificationEvent>,JbpmServicesEventListener<Task>> eventService = (EventService<JbpmServicesEventListener<NotificationEvent>,JbpmServicesEventListener<Task>>) runtimeEngine.getTaskService();
		eventService.registerTaskLifecycleEventListener(new CaseTaskLifecycleListener(ksession));
		TaskService taskService = runtimeEngine.getTaskService();
		Map<String, Object> params = new HashMap<String, Object>();
		getPersistence().start();

		ConstructionCase cc = new ConstructionCase("/cases/case1");
		HousePlan housePlan = new HousePlan(cc);
		House house = new House(cc);
		getPersistence().persist(cc);
		getPersistence().commit();
		params.put("housePlan", Arrays.asList(housePlan));
		params.put("house", Arrays.asList(house));
		getPersistence().start();
		CaseInstance processInstance = (CaseInstance) ksession.startProcess("bye", params);
		getPersistence().commit();
		assertProcessInstanceActive(processInstance.getId(), ksession);
		assertNodeTriggered(processInstance.getId(), "defaultSplit");
		// Sentries:
		assertNodeTriggered(processInstance.getId(), "OnWallCreatePart");
		assertNodeTriggered(processInstance.getId(), "OnWallPlanCreatePart");
		assertNodeTriggered(processInstance.getId(), "OnRoofPlanCreatePart");
		addWallPlan( housePlan);
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

	private void addWallPlan(HousePlan housePlan) throws Exception{
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new WallPlan(housePlan);
		getPersistence().update(housePlan);
		getPersistence().commit();
	}

	private void addWall(House house) throws Exception{
		house = getPersistence().find(House.class, house.getId());
		new Wall(house);
		getPersistence().update(house);
		getPersistence().commit();
	}

	protected RuntimeManager createRuntimeManager(Strategy strategy, String identifier, String... process) {
		Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
		for (String p : process) {
			resources.put(p, CMMNBuilder.CMMN_RESOURCE_TYPE);
		}
		return createRuntimeManager(strategy, resources, identifier);
	}
}
