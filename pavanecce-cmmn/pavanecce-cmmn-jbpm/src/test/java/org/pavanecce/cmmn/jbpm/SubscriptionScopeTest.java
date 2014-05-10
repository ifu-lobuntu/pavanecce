package org.pavanecce.cmmn.jbpm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.junit.Test;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.jbpm.instance.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.CaseSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.instance.PersistedCaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.instance.SubscriptionManager;

import test.ConstructionCase;
import test.House;
import test.HousePlan;
import test.WallPlan;

public abstract class SubscriptionScopeTest extends AbstractConstructionTestCase {
	protected HousePlan housePlan;
	protected House house;
	private CaseInstance caseInstance;

	public SubscriptionScopeTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void testCreateAndDeleteSubscriptionsAgainstParent() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN
		SubscriptionManager subManager = (SubscriptionManager) getRuntimeEngine().getKieSession().getEnvironment().get(SubscriptionManager.ENV_NAME);
		getPersistence().start();
		assertEquals(0, subManager.getCaseSubscriptionInfoFor(housePlan, getPersistence()).getCaseFileItemSubscriptions().size());
		getPersistence().commit();
		
		triggerStartOfTask();
		getPersistence().start();
		assertEquals(2, subManager.getCaseSubscriptionInfoFor(housePlan, getPersistence()).getCaseFileItemSubscriptions().size());
		assertNotNull(subManager.getCaseSubscriptionInfoFor(housePlan, getPersistence()).findCaseFileItemSubscription("wallPlans", CaseFileItemTransition.CREATE));
		assertNotNull(subManager.getCaseSubscriptionInfoFor(housePlan, getPersistence()).findCaseFileItemSubscription("roofPlan", CaseFileItemTransition.DELETE));
		getPersistence().commit();
		List<TaskSummary> list = getRuntimeEngine().getTaskService().getTasksOwned("Builder", "en-UK");
		assertEquals(1, list.size());
		getRuntimeEngine().getTaskService().start(list.get(0).getId(), "Builder");
		getRuntimeEngine().getTaskService().complete(list.get(0).getId(), "Builder",new HashMap<String,Object>());
		getPersistence().start();
		CaseSubscriptionInfo<?> caseSubscriptionInfoFor = subManager.getCaseSubscriptionInfoFor(housePlan, getPersistence());
		assertEquals(0, caseSubscriptionInfoFor.getCaseFileItemSubscriptions().size());
		getPersistence().commit();
		// *****THEN
//		@SuppressWarnings("unchecked")
//		Map<String, Object> contentData = (Map<String, Object>) ContentMarshallerHelper.unmarshall(input.getContent(), getRuntimeEngine().getKieSession(). getEnvironment());
//		assertEquals(housePlan.getWallPlans().iterator().next().getId(), ((WallPlan) contentData.get("wallPlan")).getId());
	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager("test/SubscriptionScopeTests.cmmn");
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
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess("SubscriptionScopeTests", params);
		getPersistence().commit();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		assertNodeTriggered(caseInstance.getId(), "defaultSplit");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "onTheUserEventOccurPartId");
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "TheUserEventPlanItem");
		getPersistence().commit();
	}

	private void triggerStartOfTask() throws Exception {
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		caseInstance.signalEvent("TheUserEvent", new Object());
		getPersistence().commit();
		assertNodeTriggered(caseInstance.getId(), "TheTask");
	}


}
