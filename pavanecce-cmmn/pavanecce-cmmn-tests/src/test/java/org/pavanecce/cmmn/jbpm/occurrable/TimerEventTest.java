package org.pavanecce.cmmn.jbpm.occurrable;

import java.util.Collection;

import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.process.instance.timer.TimerManager;
import org.junit.After;
import org.junit.Test;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

import test.cmmn.HousePlan;
import test.cmmn.WallPlan;

public class TimerEventTest extends AbstractOccurrableTestCase {

	public TimerEventTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	public String getCaseName() {
		return "TimerEventListenerTests";
	}

	public String getProcessFile() {
		return "test/occurrable/TimerEventListenerTests.cmmn";
	}

	@After
	public void deleteTimers() {
		getPersistence().start();
		Collection<TimerInstance> timers = getTimerManager().getTimers();
		for (TimerInstance timerInstance : timers) {
			getTimerManager().cancelTimer(timerInstance.getId());
		}
		getPersistence().commit();
	}

	@Test
	public void testPlanItemTrigger() throws Exception {
		givenThatTheTestCaseIsStarted();
		assertNodeNotTriggered(caseInstance.getId(), "TimerEventWithPlanItemTriggerPlanItem");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "theUserEventTrigger");
		reloadCaseInstance(caseInstance).signalEvent("TheUserEvent", new Object());
		getPersistence().commit();
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "TimerEventWithPlanItemTriggerPlanItem");
		Collection<TimerInstance> timers = getTimerManager().getTimers();
		assertEquals(2, timers.size());
		getPersistence().commit();
	}

	@Test
	public void testCaseFileItemTrigger() throws Exception {
		givenThatTheTestCaseIsStarted();
		assertNodeNotTriggered(caseInstance.getId(), "TimerEventWithCaseFileItemTriggerPlanItem");
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "wallPlanCreatedTrigger");
		housePlan = getPersistence().find(HousePlan.class, housePlan.getId());
		new WallPlan(housePlan);
		getPersistence().update(housePlan);
		getPersistence().commit();
		getPersistence().start();
		assertNodeActive(caseInstance.getId(), getRuntimeEngine().getKieSession(), "TimerEventWithCaseFileItemTriggerPlanItem");
		Collection<TimerInstance> timers = getTimerManager().getTimers();
		assertEquals(2, timers.size());
		getPersistence().commit();
	}

	@Override
	protected void givenThatTheTestCaseIsStarted() {
		super.givenThatTheTestCaseIsStarted();
		getPersistence().start();
		Collection<TimerInstance> timers = getTimerManager().getTimers();
		assertEquals(1, timers.size());
		getPersistence().commit();
	}

	private TimerManager getTimerManager() {
		return ((InternalProcessRuntime) reloadCaseInstance(caseInstance).getKnowledgeRuntime().getProcessRuntime()).getTimerManager();
	}

	@Override
	protected void triggerOccurrence() throws Exception {
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		Collection<TimerInstance> timers = getTimerManager().getTimers();
		for (TimerInstance timerInstance : timers) {
			caseInstance.signalEvent("timerTriggered", timerInstance);
		}
		getPersistence().commit();
	}

}
