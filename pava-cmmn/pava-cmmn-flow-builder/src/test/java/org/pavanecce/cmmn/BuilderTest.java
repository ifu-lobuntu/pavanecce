package org.pavanecce.cmmn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.EventService;
import org.pavanecce.cmmn.flow.builder.CMMNBuilder;
import org.pavanecce.cmmn.instance.CaseInstance;
import org.pavanecce.cmmn.instance.CaseTaskLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuilderTest extends AbstrasctJbpmCaseBaseTestCase {
	private static final Logger logger = LoggerFactory.getLogger(BuilderTest.class);

	public BuilderTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void testBuild() throws Exception {
		createRuntimeManager("test/hello.cmmn");
		RuntimeEngine runtimeEngine = getRuntimeEngine();
		KieSession ksession = runtimeEngine.getKieSession();
		EventService eventService = (EventService) runtimeEngine.getTaskService();
		eventService.registerTaskLifecycleEventListener(new CaseTaskLifecycleListener(ksession));
		TaskService taskService = runtimeEngine.getTaskService();
		Map<String, Object> params = new HashMap<String, Object>();
		UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
		ut.begin();
		EntityManager em = getEmf().createEntityManager();

		HousePlan housePlan = new HousePlan();
		em.persist(housePlan);
		em.flush();
		ut.commit();
		params.put("housePlan", Arrays.asList(housePlan));
		ut.begin();
		CaseInstance processInstance = (CaseInstance) ksession.startProcess("hello", params);
		ut.commit();
		assertProcessInstanceActive(processInstance.getId(), ksession);
		assertNodeTriggered(processInstance.getId(), "defaultSplit");
		// Sentries:
		assertNodeTriggered(processInstance.getId(), "WaitingForWallPlanCreated");
		assertNodeTriggered(processInstance.getId(), "WaitingForFoundationLaid");
		addWallPlan(ut, housePlan);
		addWallPlan(ut, housePlan);
		assertNodeTriggered(processInstance.getId(), "LayFoundationPlanItem");
		// // let Builder execute LayFoundationPlanItem
		List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertEquals("LayFoundationPlanItem", list.get(0).getName());
		assertEquals("LayFoundationPlanItem", list.get(1).getName());
		ut.begin();
		processInstance = (CaseInstance) ksession.getProcessInstance(processInstance.getId());
		assertNodeActive(processInstance.getId(), ksession, "WaitingForWallPlanCreated");
		assertNodeActive(processInstance.getId(), ksession, "WaitingForFoundationLaid");
		assertNodeActive(processInstance.getId(), ksession, "LayFoundationPlanItem");
		ut.commit();
		taskService.start(list.get(0).getId(), "Builder");
		taskService.complete(list.get(0).getId(), "Builder", new HashMap<String, Object>());
		ut.begin();
		assertNodeActive(processInstance.getId(), ksession, "LayBricksPlanItem");
		assertNodeActive(processInstance.getId(), ksession, "LayFoundationPlanItem");
		ut.commit();
		taskService.start(list.get(1).getId(), "Builder");
		taskService.complete(list.get(1).getId(), "Builder", new HashMap<String, Object>());

		list = taskService.getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(2, list.size());
		assertEquals("LayBricksPlanItem", list.get(0).getName());
		assertEquals("LayBricksPlanItem", list.get(1).getName());
		taskService.start(list.get(0).getId(), "Builder");
		taskService.complete(list.get(0).getId(), "Builder", new HashMap<String, Object>());
		Task sum = taskService.getTaskById(list.get(1).getId());
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

	@Test
	public void testBuildWallExitCritera() throws Exception {
		createRuntimeManager("test/bye.cmmn");
		RuntimeEngine runtimeEngine = getRuntimeEngine();
		KieSession ksession = runtimeEngine.getKieSession();
		EventService eventService = (EventService) runtimeEngine.getTaskService();
		eventService.registerTaskLifecycleEventListener(new CaseTaskLifecycleListener(ksession));
		TaskService taskService = runtimeEngine.getTaskService();
		Map<String, Object> params = new HashMap<String, Object>();
		UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
		ut.begin();
		EntityManager em = getEmf().createEntityManager();

		HousePlan housePlan = new HousePlan();
		em.persist(housePlan);

		House house = new House();
		em.persist(house);
		em.flush();
		ut.commit();
		params.put("housePlan", Arrays.asList(housePlan));
		params.put("house", Arrays.asList(house));
		ut.begin();
		CaseInstance processInstance = (CaseInstance) ksession.startProcess("bye", params);
		ut.commit();
		assertProcessInstanceActive(processInstance.getId(), ksession);
		assertNodeTriggered(processInstance.getId(), "defaultSplit");
		// Sentries:
		assertNodeTriggered(processInstance.getId(), "OnWallCreatePart");
		assertNodeTriggered(processInstance.getId(), "OnWallPlanCreatePart");
		assertNodeTriggered(processInstance.getId(), "OnRoofPlanCreatePart");
		addWallPlan(ut, housePlan);
		assertNodeTriggered(processInstance.getId(), "BuildWallPlanItem");
		// // let Builder execute LayFoundationPlanItem
		List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		assertEquals(1, list.size());
		assertEquals("BuildWallPlanItem", list.get(0).getName());

		
		//exit criterion
		addWall(ut, house);
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

	private void addWallPlan(UserTransaction ut, HousePlan housePlan) throws NotSupportedException, SystemException, RollbackException,
			HeuristicMixedException, HeuristicRollbackException {
		EntityManager em;
		ut.begin();
		em = getEmf().createEntityManager();
		housePlan = em.find(HousePlan.class, housePlan.getId());
		housePlan.getWallPlans().add(new WallPlan());
		em.flush();
		ut.commit();
	}

	private void addWall(UserTransaction ut, House house) throws NotSupportedException, SystemException, RollbackException,
			HeuristicMixedException, HeuristicRollbackException {
		EntityManager em;
		ut.begin();
		em = getEmf().createEntityManager();
		house= em.find(House.class, house.getId());
		house.getWalls().add(new Wall());
		em.flush();
		ut.commit();
	}

	protected RuntimeManager createRuntimeManager(Strategy strategy, String identifier, String... process) {
		Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
		for (String p : process) {
			resources.put(p, CMMNBuilder.CMMN_RESOURCE_TYPE);
		}
		return createRuntimeManager(strategy, resources, identifier);
	}
}
