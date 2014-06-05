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
import org.jbpm.services.task.events.BeforeTaskCompletedEvent;
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
import org.pavanecce.cmmn.jbpm.TaskParameters;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.lifecycle.ControllableItemInstance;
//TODO try to remove dependencies from 'impl'
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.StageInstance;
import org.pavanecce.cmmn.jbpm.task.AfterExitCriteriaEvent;
import org.pavanecce.cmmn.jbpm.task.AfterTaskReactivatedEvent;
import org.pavanecce.cmmn.jbpm.task.AfterTaskReenabledEvent;
import org.pavanecce.cmmn.jbpm.task.AfterTaskResumedFromParentEvent;
import org.pavanecce.cmmn.jbpm.task.AfterTaskStartedAutomaticallyEvent;
import org.pavanecce.cmmn.jbpm.task.AfterTaskSuspendedFromParentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseTaskLifecycleListener extends ExternalTaskEventListener {

	private static final Logger logger = LoggerFactory.getLogger(CaseTaskLifecycleListener.class);

	public CaseTaskLifecycleListener() {
		super();
	}

	public void afterTaskReactivatedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskReactivatedEvent Task task) {
		signalEvent(task, PlanItemTransition.REACTIVATE);
	}

	public void afterTaskReenabledEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskReenabledEvent Task task) {
		signalEvent(task, PlanItemTransition.REENABLE);
	}

	public void afterTaskParentSuspended(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskSuspendedFromParentEvent Task task) {
		signalEvent(task, PlanItemTransition.PARENT_SUSPEND);
	}

	public void afterTaskParentResumed(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskResumedFromParentEvent Task task) {
		signalEvent(task, PlanItemTransition.PARENT_RESUME);
	}

	public void afterTaskExitCriteriaEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterExitCriteriaEvent Task task) {
		signalEvent(task, PlanItemTransition.EXIT);
	}

	public void afterTaskStartedAutomaticallyEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskStartedAutomaticallyEvent Task task) {
		signalEvent(task, PlanItemTransition.START);
	}

	@Override
	public void afterTaskResumedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskResumedEvent Task task) {
		if (isCaseInstance(task)) {
			signalEvent(task, PlanItemTransition.REACTIVATE);
		} else {
			signalEvent(task, PlanItemTransition.RESUME);
		}
	}

	@Override
	public void afterTaskSuspendedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskSuspendedEvent Task task) {
		signalEvent(task, PlanItemTransition.SUSPEND);
	}

	@Override
	public void afterTaskStartedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskStartedEvent Task task) {
		if (!isCaseInstance(task)) {
			signalEvent(task, PlanItemTransition.MANUAL_START);
		}
	}

	private boolean isCaseInstance(Task task) {
		KieSession session = getKieSession(task);
		ProcessInstance pi = session.getProcessInstance(task.getTaskData().getProcessInstanceId());
		boolean isCaseInstance = false;
		if (pi instanceof CaseInstance) {
			CaseInstance ci = (CaseInstance) pi;
			if (ci.getWorkItemId() == task.getTaskData().getWorkItemId()) {
				isCaseInstance = true;
			}
		}
		return isCaseInstance;
	}

	public void beforeTaskCompletedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @BeforeTaskCompletedEvent Task task) {
		KieSession session = getKieSession(task);
		ProcessInstance pi = session.getProcessInstance(task.getTaskData().getProcessInstanceId());
		if (pi instanceof CaseInstance) {
			CaseInstance ci = (CaseInstance) pi;
			if (ci.getWorkItemId() == task.getTaskData().getWorkItemId()) {
				if (ci.canComplete()) {
					// TODO read output from CaseInstance
				} else {
					throw new IllegalStateException("Task " + task + " represents Case Instance " + ci.getCase().getName() + "[" + ci.getId()
							+ "] which cannot be completed yet");
				}
			} else {
				ControllableItemInstance<?> node = ci.findNodeForWorkItem(task.getTaskData().getWorkItemId());
				if (node instanceof StageInstance && !((StageInstance) node).canComplete()) {
					throw new IllegalStateException("Task " + task + " represents Stage Instance " + node.getItem().getEffectiveName() + "[" + ci.getId()
							+ "] which cannot be completed yet");
				}
			}
		}

	}

	@Override
	public void afterTaskFailedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskFailedEvent Task task) {
		signalEvent(task, PlanItemTransition.FAULT);
	}

	@Override
	public void afterTaskExitedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskExitedEvent Task task) {
		signalEvent(task, PlanItemTransition.TERMINATE); // In CMMN exit is when exit criteria occur
	}

	@Override
	public void afterTaskSkippedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskSkippedEvent Task task) {
		if (!isCaseInstance(task)) {
			signalEvent(task, PlanItemTransition.DISABLE);
		}
	}

	@Override
	public void afterTaskCompletedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskCompletedEvent Task task) {
		signalEvent(task, PlanItemTransition.COMPLETE);
	}

	protected void signalEvent(Task task, PlanItemTransition standardEvent) {
		long processInstanceId = task.getTaskData().getProcessInstanceId();
		if (processInstanceId <= 0) {
			return;
		}
		KieSession session = getKieSession(task);
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
				Map<String, Object> results = buildWorkItemResults(task, standardEvent, getRuntimeEngine(task), session, manager);
				workItem.setResults(results);
				// In CMMN we need the state of the PlanItemInstance for completion calculations
				workItemManager.signalEvent(TaskParameters.WORK_ITEM_UPDATED, workItem, processInstanceId);
			}
		} else {
			super.processTaskState(task);
		}

	}

	private RuntimeEngine getRuntimeEngine(Task task) {
		return getManager(task).getRuntimeEngine(ProcessInstanceIdContext.get(task.getTaskData().getProcessInstanceId()));
	}

	protected Map<String, Object> buildWorkItemResults(Task task, PlanItemTransition standardEvent, RuntimeEngine runtime, KieSession session,
			RuntimeManager manager) {
		Map<String, Object> results = new HashMap<String, Object>();
		if (task.getTaskData().getActualOwner() != null) {
			String userId = task.getTaskData().getActualOwner().getId();
			results.put(TaskParameters.ACTUAL_OWNER, userId);
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
		results.put(TaskParameters.TRANSITION, standardEvent);
		results.put(TaskParameters.TASK, task);
		return results;
	}

	@Override
	public void afterTaskAddedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskAddedEvent Task task) {

	}

	@Override
	public void afterTaskReleasedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskReleasedEvent Task task) {
	}

	@Override
	public void afterTaskForwardedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskForwardedEvent Task task) {
	}

	@Override
	public void afterTaskDelegatedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskDelegatedEvent Task task) {
	}

	@Override
	public void afterTaskActivatedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskActivatedEvent Task task) {
		if (!isCaseInstance(task)) {
			signalEvent(task, PlanItemTransition.ENABLE);
		}
	}

	@Override
	public void afterTaskClaimedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskClaimedEvent Task task) {
		updateCaseOwner(task);
	}

	private void updateCaseOwner(Task task) {
		long processInstanceId = task.getTaskData().getProcessInstanceId();
		if (processInstanceId <= 0) {
			return;
		}
		ProcessInstance pi = getProcessInstance(task, processInstanceId);
		if (pi instanceof CaseInstance && task.getTaskData().getWorkItemId() == ((CaseInstance) pi).getWorkItemId()) {
			CaseInstance ci = (CaseInstance) pi;
			ci.setVariable(TaskParameters.CASE_OWNER, task.getTaskData().getActualOwner().getId());
		}
	}

	private ProcessInstance getProcessInstance(Task task, long processInstanceId) {
		KieSession session = getKieSession(task);
		ProcessInstance pi = session.getProcessInstance(processInstanceId);
		return pi;
	}

	private KieSession getKieSession(Task task) {
		return getRuntimeEngine(task).getKieSession();
	}

	@Override
	public void afterTaskStoppedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskStoppedEvent Task task) {
	}

}
