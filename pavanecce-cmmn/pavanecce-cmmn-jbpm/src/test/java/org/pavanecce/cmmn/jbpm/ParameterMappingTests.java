package org.pavanecce.cmmn.jbpm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.shared.services.impl.events.JbpmServicesEventListener;
import org.junit.Test;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.EventService;
import org.kie.internal.task.api.model.NotificationEvent;
import org.pavanecce.cmmn.jbpm.instance.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.CaseTaskLifecycleListener;
import org.pavanecce.cmmn.jbpm.instance.CaseTaskWorkItemHandler;
import org.pavanecce.cmmn.jbpm.test.AbstractCmmnCaseTestCase;

import test.ConstructionCase;
import test.House;
import test.HousePlan;
import test.WallPlan;

public class ParameterMappingTests extends AbstractConstructionTestCase {
	{
		super.isJpa = true;
	}
	protected HousePlan housePlan;
	protected House house;
	private CaseInstance caseInstance;

	public ParameterMappingTests() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void testInputParameters() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		triggerStartOfTask();
		// *****THEN
		assertNodeTriggered(caseInstance.getId(), "TheTask");
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1, list.size());
		Task task = getRuntimeEngine().getTaskService().getTaskById(list.get(0).getId());
		Content input = getRuntimeEngine().getTaskService().getContentById(task.getTaskData().getDocumentContentId());
		@SuppressWarnings("unchecked")
		Map<String, Object> contentData = (Map<String, Object>) ContentMarshallerHelper.unmarshall(input.getContent(), getRuntimeEngine().getKieSession(). getEnvironment());
		assertEquals(housePlan.getWallPlans().iterator().next().getId(), ((WallPlan) contentData.get("wallPlan")).getId());
	}

	protected void givenThatTheTestCaseIsStarted() {
		RuntimeManager runtimeManager = createRuntimeManager("test/ParameterTests.cmmn");
		@SuppressWarnings("unchecked")
		EventService<JbpmServicesEventListener<NotificationEvent>, JbpmServicesEventListener<Task>> eventService = (EventService<JbpmServicesEventListener<NotificationEvent>, JbpmServicesEventListener<Task>>) getRuntimeEngine()
				.getTaskService();
		eventService.registerTaskLifecycleEventListener(new CaseTaskLifecycleListener(getRuntimeEngine().getKieSession()));
		CaseTaskWorkItemHandler handler = new CaseTaskWorkItemHandler();
		handler.setRuntimeManager(runtimeManager);
		getRuntimeEngine().getKieSession().getWorkItemManager().registerWorkItemHandler("Human Task", handler);
		Map<String, Object> params = new HashMap<String, Object>();
		getPersistence().start();
		
		ConstructionCase cc = new ConstructionCase("/cases/case1");
		housePlan = new HousePlan(cc);
		house = new House(cc);
		getPersistence().persist(cc);
		getPersistence().commit();
		params.put("housePlan", Arrays.asList(housePlan));
		params.put("house", Arrays.asList(house));
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess("ParameterTests", params);
		getPersistence().commit();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		assertNodeTriggered(caseInstance.getId(), "defaultSplit");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "onWallPlanCreatedPartId");
		getPersistence().commit();
	}

	private void triggerStartOfTask() throws Exception {
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new WallPlan(housePlan);
		getPersistence().update(housePlan);
		getPersistence().commit();
	}


}
