package org.pavanecce.cmmn.jbpm;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.process.instance.timer.TimerManager;
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

public class TimerEventListenerTest extends AbstractConstructionTestCase {
	protected HousePlan housePlan;
	protected House house;
	private CaseInstance caseInstance;

	public TimerEventListenerTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	@Test
	public void testCreateAndDeleteSubscriptionsAgainstParent() throws Exception {
		// *****GIVEN
		givenThatTheTestCaseIsStarted();
		// *****WHEN

		triggerStartOfTask();
		// *****THEN
	}

	protected void givenThatTheTestCaseIsStarted() {
		createRuntimeManager("test/TimerEventListenerTests.cmmn");
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
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().startProcess("TimerEventListenerTests", params);
		getPersistence().commit();
		assertProcessInstanceActive(caseInstance.getId(), getRuntimeEngine().getKieSession());
		assertNodeTriggered(caseInstance.getId(), "defaultSplit");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "onTheTimerEventOccurPartId");
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "TheTimerEventPlanItem");
		getPersistence().commit();
	}

	private void triggerStartOfTask() throws Exception {
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		TimerManager tm = ((InternalProcessRuntime)caseInstance.getKnowledgeRuntime().getProcessRuntime()).getTimerManager();
		Collection<TimerInstance> timers = tm.getTimers();
		for (TimerInstance timerInstance : timers) {
			caseInstance.signalEvent("timerTriggered", timerInstance);
		}
		getPersistence().commit();
		assertNodeTriggered(caseInstance.getId(), "TheTask");
	}

}
