package org.pavanecce.cmmn.jbpm.infra;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;

import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.WorkItemManager;
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
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.services.task.wih.ExternalTaskEventListener;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.instance.CaseElementLifecycleWithTask;
import org.pavanecce.cmmn.jbpm.instance.impl.CaseInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseTaskLifecycleListener extends ExternalTaskEventListener {

	private static final Logger logger = LoggerFactory.getLogger(CaseTaskLifecycleListener.class);

	public CaseTaskLifecycleListener() {
		super();
	}

	@Override
	public void afterTaskStartedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskStartedEvent Task ti) {
		signalEvent(ti, PlanItemTransition.MANUAL_START);
	}

	@Override
	public void afterTaskActivatedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskActivatedEvent Task ti) {
	}

	@Override
	public void afterTaskClaimedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskClaimedEvent Task ti) {
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

	protected void signalEvent(Task task, PlanItemTransition standardEvent) {

		long processInstanceId = task.getTaskData().getProcessInstanceId();
		if (processInstanceId <= 0) {
			return;
		}
		RuntimeEngine runtime = getManager(task).getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
		KieSession session = runtime.getKieSession();
		if (session == null) {
			logger.error("EE: I've recieved an event but the session is not known by this handler ( " + task.getTaskData().getProcessSessionId() + ")");
			return;
		}
		ProcessInstance pi = session.getProcessInstance(processInstanceId);
		if (pi instanceof CaseInstance) {
			RuntimeManager manager = getManager(task);
			WorkItemManager workItemManager = (WorkItemManager) session.getWorkItemManager();
			WorkItem workItem = workItemManager.getWorkItem(task.getTaskData().getWorkItemId());
			if (workItem != null) {
				Map<String, Object> results = buildWorkItemResults(task, standardEvent, runtime, session, manager);
				workItem.setResults(results);
				// In CMMN we need the state of the PlanItemInstance for completion calculations
				workItemManager.signalEvent(CaseElementLifecycleWithTask.WORK_ITEM_UPDATED, workItem, processInstanceId);
			}
		} else {
			super.processTaskState(task);
		}

	}

	protected Map<String, Object> buildWorkItemResults(Task task, PlanItemTransition standardEvent, RuntimeEngine runtime, KieSession session, RuntimeManager manager) {
		Map<String, Object> results = new HashMap<String, Object>();
		if (task.getTaskData().getActualOwner() != null) {
			String userId = task.getTaskData().getActualOwner().getId();
			results.put("ActorId", userId);
		}
		long contentId = task.getTaskData().getOutputContentId();
		if (contentId != -1) {
			Content content = runtime.getTaskService().getContentById(contentId);
			ClassLoader cl = null;
			if (manager instanceof InternalRuntimeManager) {
				cl = ((InternalRuntimeManager) manager).getEnvironment().getClassLoader();
			}
			Object result = ContentMarshallerHelper.unmarshall(content.getContent(), session.getEnvironment(), cl);
			results.put("Result", result);
			if (result instanceof Map) {
				Map<?, ?> map = (Map<?, ?>) result;
				for (Map.Entry<?, ?> entry : map.entrySet()) {
					if (entry.getKey() instanceof String) {
						results.put((String) entry.getKey(), entry.getValue());
					}
				}
			}
		}
		results.put(CaseElementLifecycleWithTask.TRANSITION, standardEvent);
		results.put(CaseElementLifecycleWithTask.TASK, task);
		return results;
	}

	protected KieSession getKieSession(Task ti) {
		return getManager(ti).getRuntimeEngine(ProcessInstanceIdContext.get(ti.getTaskData().getProcessInstanceId())).getKieSession();
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
