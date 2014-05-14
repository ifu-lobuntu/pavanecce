package org.pavanecce.uml.jbpm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.builder.dialect.ProcessDialectRegistry;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.shared.services.impl.events.JbpmServicesEventListener;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.EventService;
import org.kie.internal.task.api.model.NotificationEvent;
import org.pavanecce.cmmn.jbpm.infra.CaseTaskLifecycleListener;
import org.pavanecce.cmmn.jbpm.infra.CaseTaskWorkItemHandler;
import org.pavanecce.cmmn.jbpm.instance.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.ocm.OcmCaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.ocm.OcmCaseSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.test.AbstractCmmnCaseTestCase;
import org.pavanecce.cmmn.jbpm.xml.handler.CMMNBuilder;

import test.ConstructionCase;
import test.House;
import test.HousePlan;
import test.RoofPlan;
import test.RoomPlan;
import test.Wall;
import test.WallPlan;

public class OclProcessDialectTests extends AbstractCmmnCaseTestCase {
	{
		super.isJpa = true;
	}
	protected HousePlan housePlan;
	protected House house;
	private CaseInstance caseInstance;

	public OclProcessDialectTests() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Override
	protected Class<?>[] getClasses() {
		return new Class<?>[] { ConstructionCase.class, HousePlan.class, House.class, Wall.class, WallPlan.class, RoofPlan.class, OcmCaseSubscriptionInfo.class, OcmCaseFileItemSubscriptionInfo.class,
				RoomPlan.class };
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
		Map<String, Object> contentData = (Map<String, Object>) ContentMarshallerHelper.unmarshall(input.getContent(), getRuntimeEngine().getKieSession().getEnvironment());
		assertEquals(housePlan.getWallPlans().iterator().next().getId(), ((WallPlan) contentData.get("wallPlan")).getId());
	}

	protected void givenThatTheTestCaseIsStarted() {
		RuntimeManager runtimeManager = createRuntimeManager("test/test.uml", "test/OclProcessDialectTests.cmmn");
		Map<String, Object> params = new HashMap<String, Object>();
		getPersistence().start();

		ConstructionCase cc = new ConstructionCase("/cases/case1");
		housePlan = new HousePlan(cc);
		house = new House(cc);
		getPersistence().persist(cc);
		getPersistence().commit();
		params.put("housePlan", housePlan);
		params.put("house", house);
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess("OclProcessDialectTests", params);
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

	@Override
	protected RuntimeManager createRuntimeManager(Strategy strategy, String identifier, String... process) {
		Map<String, ResourceType> resources = new LinkedHashMap<String, ResourceType>();
		for (String p : process) {
			if (p.endsWith(".cmmn")) {
				resources.put(p, CMMNBuilder.CMMN_RESOURCE_TYPE);
			} else if (p.endsWith(".uml")) {
				resources.put(p, UmlBuilder.UML_RESOURCE_TYPE);
			} else if (p.endsWith(".bpmn")) {
				resources.put(p, ResourceType.BPMN2);
			}
		}
		return createRuntimeManager(strategy, resources, identifier);
	}

	@Override
	protected RuntimeManager createRuntimeManager(String... processFile) {

		ProcessDialectRegistry.setDialect("ocl", new OclProcessDialect());
		return super.createRuntimeManager(processFile);
	}

}
