package org.pavanecce.cmmn.jbpm.event;

import java.lang.reflect.InvocationTargetException;

import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;

import org.jbpm.services.task.events.AfterTaskActivatedEvent;
import org.jbpm.services.task.events.AfterTaskAddedEvent;
import org.jbpm.services.task.events.AfterTaskClaimedEvent;
import org.jbpm.services.task.events.AfterTaskCompletedEvent;
import org.jbpm.services.task.events.AfterTaskDelegatedEvent;
import org.jbpm.services.task.events.AfterTaskExitedEvent;
import org.jbpm.services.task.events.AfterTaskFailedEvent;
import org.jbpm.services.task.events.AfterTaskForwardedEvent;
import org.jbpm.services.task.events.AfterTaskReleasedEvent;
import org.jbpm.services.task.events.AfterTaskResumedEvent;
import org.jbpm.services.task.events.AfterTaskSkippedEvent;
import org.jbpm.services.task.events.AfterTaskStartedEvent;
import org.jbpm.services.task.events.AfterTaskStoppedEvent;
import org.jbpm.services.task.events.AfterTaskSuspendedEvent;
import org.jbpm.services.task.lifecycle.listeners.TaskLifeCycleEventListener;
import org.jbpm.shared.services.impl.events.JbpmServicesEventListener;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.ProcessRuntime;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.Task;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.instance.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstance;

public class CaseTaskLifecycleListener extends JbpmServicesEventListener<Task> implements TaskLifeCycleEventListener {
	ProcessRuntime runtimeManager;

	public CaseTaskLifecycleListener(ProcessRuntime runtimeManager) {
		super();
		this.runtimeManager = runtimeManager;
	}

	@Override
	public void afterTaskStartedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskStartedEvent Task ti) {
		signalEvent(ti, PlanItemTransition.START);
	}

	@Override
	public void afterTaskActivatedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskActivatedEvent Task ti) {
		signalEvent(ti, PlanItemTransition.MANUAL_START);
	}

	@Override
	public void afterTaskClaimedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskClaimedEvent Task ti) {
		signalEvent(ti, PlanItemTransition.START);
	}

	@Override
	public void afterTaskSkippedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskSkippedEvent Task ti) {
		signalEvent(ti, PlanItemTransition.DISABLE);
	}

	@Override
	public void afterTaskStoppedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskStoppedEvent Task ti) {
	}

	@Override
	public void afterTaskCompletedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskCompletedEvent Task ti) {
		signalEvent(ti, PlanItemTransition.COMPLETE);
	}

	protected void signalEvent(Task ti, PlanItemTransition standardEvent) {
		I18NText i18nText = ti.getNames().get(0);
		PlanItemEvent event = new PlanItemEvent(i18nText.getText(), standardEvent, ti);
		ProcessInstance pi = runtimeManager.getProcessInstance(ti.getTaskData().getProcessInstanceId());
		if (pi instanceof CaseInstance) {
			runtimeManager.signalEvent(OnPart.getType(i18nText.getText(), standardEvent), event, ti.getTaskData().getProcessInstanceId());
		}
	}

	private void invokeTransitionMethod(Task ti, PlanItemTransition standardEvent, ProcessInstance pi) {
		CaseInstance ci = (CaseInstance) pi;
		PlanItemInstance pii= ci.findPlanItemInstanceForWorkItemId(ti.getTaskData().getWorkItemId());
		try {
			standardEvent.getMethod().invoke(pii);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			if(e.getTargetException() instanceof RuntimeException){
				throw (RuntimeException)e.getTargetException();
			}else{
				throw new RuntimeException(e.getTargetException());
			}
		}
	}

	@Override
	public void afterTaskFailedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskFailedEvent Task ti) {
		signalEvent(ti, PlanItemTransition.FAULT);
	}

	@Override
	public void afterTaskAddedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskAddedEvent Task ti) {

	}

	@Override
	public void afterTaskExitedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskExitedEvent Task ti) {
		signalEvent(ti, PlanItemTransition.TERMINATE);// In CMMN exit is when exit criteria occur
	}

	@Override
	public void afterTaskReleasedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskReleasedEvent Task ti) {
	}

	@Override
	public void afterTaskResumedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskResumedEvent Task ti) {
		signalEvent(ti, PlanItemTransition.RESUME);
	}

	@Override
	public void afterTaskSuspendedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskSuspendedEvent Task ti) {
		signalEvent(ti, PlanItemTransition.SUSPEND);
	}

	@Override
	public void afterTaskForwardedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskForwardedEvent Task ti) {
	}

	@Override
	public void afterTaskDelegatedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskDelegatedEvent Task ti) {
	}

}
