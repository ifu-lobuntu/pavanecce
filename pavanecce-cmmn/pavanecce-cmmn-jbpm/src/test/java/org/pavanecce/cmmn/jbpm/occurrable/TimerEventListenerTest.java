package org.pavanecce.cmmn.jbpm.occurrable;

import java.util.Collection;

import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.process.instance.timer.TimerManager;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public class TimerEventListenerTest extends AbstractOccurrableTestCase {

	public TimerEventListenerTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}

	public String getCaseName() {
		return "TimerEventListenerTests";
	}

	public String getProcessFile() {
		return "test/occurrable/TimerEventListenerTests.cmmn";
	}

	@Override
	protected void triggerOccurrence() throws Exception {
		getPersistence().start();
		caseInstance = (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance.getId());
		TimerManager tm = ((InternalProcessRuntime) caseInstance.getKnowledgeRuntime().getProcessRuntime()).getTimerManager();
		Collection<TimerInstance> timers = tm.getTimers();
		for (TimerInstance timerInstance : timers) {
			caseInstance.signalEvent("timerTriggered", timerInstance);
		}
		getPersistence().commit();
	}

}
