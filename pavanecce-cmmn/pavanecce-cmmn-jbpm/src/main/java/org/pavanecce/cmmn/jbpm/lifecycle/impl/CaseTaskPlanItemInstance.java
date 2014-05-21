package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.RuntimeDroolsException;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.spi.ProcessContext;
import org.jbpm.process.core.context.exception.ExceptionScope;
import org.jbpm.process.instance.ContextInstanceContainer;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.StartProcessHelper;
import org.jbpm.process.instance.context.exception.ExceptionScopeInstance;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.process.instance.impl.ReturnValueConstraintEvaluator;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.kie.api.KieBase;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.EventListener;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.internal.runtime.KnowledgeRuntime;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.CaseTask;
import org.pavanecce.cmmn.jbpm.flow.ParameterMapping;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.flow.TaskItemWithDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstanceContainerLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseTaskPlanItemInstance extends TaskPlanItemInstance<CaseTask, TaskItemWithDefinition<CaseTask>> implements EventListener, ContextInstanceContainer {

	private static final long serialVersionUID = -2144001908752174712L;
	private static final Logger logger = LoggerFactory.getLogger(CaseTaskPlanItemInstance.class);

	private long processInstanceId;

    protected boolean isLinkedIncomingNodeRequired() {
    	return false;
    }
	@Override
	public void start() {
		super.start();
		startProcess();
	}
	@Override
	public void internalTrigger(NodeInstance from, String type) {
		super.internalTrigger(from, type);
	}

	@Override
	public void manualStart() {
		super.manualStart();
		startProcess();
	}

	@Override
	public void terminate() {
		super.terminate();
		killSubprocessGracefully();
	}

	@Override
	public void reactivate() {
		super.reactivate();
		startProcess();
	}

	private void startProcess() {
		String processId = getPlanItemDefinition().getProcessId();
		KieBase kbase = ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getKieBase();
		// start process instance
		Process process = kbase.getProcess(processId);
		if (process == null) {
			// try to find it by name
			String latestProcessId = StartProcessHelper.findLatestProcessByName(kbase, processId);
			if (latestProcessId != null) {
				processId = latestProcessId;
				process = kbase.getProcess(processId);
			}
		}

		if (process == null) {
			logger.error("Could not find process {}", processId);
			logger.error("Aborting process");
			((ProcessInstance) getProcessInstance()).setState(ProcessInstance.STATE_ABORTED);
			throw new RuntimeDroolsException("Could not find process " + processId);
		} else {
			List<ParameterMapping> parameterMappings = prepareParameterMappings(process);
			Map<String, Object> workItemParameters = getWorkItem().getParameters();
			Map<String, Object> inputParameters = new HashMap<String, Object>();
			ProcessContext ctx = new ProcessContext(getProcessInstance().getKnowledgeRuntime());
			ctx.setNodeInstance(this);
			ctx.setProcessInstance(getProcessInstance());
			for (ParameterMapping pm : parameterMappings) {
				Object sourceValue = workItemParameters.get(pm.getSourceParameter().getName());
				if (pm.getTransformation() instanceof ReturnValueConstraintEvaluator) {
					ReturnValueConstraintEvaluator rvce = (ReturnValueConstraintEvaluator) pm.getTransformation();
					try {
						sourceValue = rvce.getReturnValueEvaluator().evaluate(ctx);
					} catch (Exception e) {
						logger.error("Could not perform transformation", e);
					}
				}
				inputParameters.put(pm.getTargetParameterName(), sourceValue);
			}
			inputParameters.put(Case.WORK_ITEM, getWorkItem());
			KnowledgeRuntime kruntime = ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime();
			RuntimeManager manager = (RuntimeManager) kruntime.getEnvironment().get("RuntimeManager");
			if (manager != null) {
				RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
				kruntime = (KnowledgeRuntime) runtime.getKieSession();
			}
			ProcessInstance processInstance = (ProcessInstance) kruntime.createProcessInstance(processId, inputParameters);
			this.processInstanceId = processInstance.getId();
			((ProcessInstanceImpl) processInstance).setMetaData("ParentProcessInstanceId", getProcessInstance().getId());
			((ProcessInstanceImpl) processInstance).setParentProcessInstanceId(getProcessInstance().getId());
			kruntime.startProcessInstance(processInstance.getId());
			if (!isWaitForCompletion()) {
				triggerCompleted();
			} else if (processInstance.getState() == ProcessInstance.STATE_COMPLETED || processInstance.getState() == ProcessInstance.STATE_ABORTED) {
				processInstanceCompleted(processInstance);
			} else {
				addProcessListener();
			}
		}
	}

	private List<ParameterMapping> prepareParameterMappings(Process process) {
		List<ParameterMapping> parameterMappings = getPlanItemDefinition().getParameterMappings();
		if (process instanceof Case) {
			List<CaseParameter> inputParameters = ((Case) process).getInputParameters();
			for (ParameterMapping pm : parameterMappings) {
				for (CaseParameter caseParameter : inputParameters) {
					if (pm.getTargetParameterId().equals(caseParameter.getElementId())) {
						pm.setTargetParameterName(caseParameter.getName());
						break;
					}
				}

			}
		}
		return parameterMappings;
	}

	@Override
	public void resume() {
		super.resume();
		setSubProcessState(ProcessInstance.STATE_ACTIVE);
	}

	@Override
	public void parentResume() {
		super.parentResume();
		setSubProcessState(ProcessInstance.STATE_ACTIVE);
	}

	@Override
	public void parentSuspend() {
		super.parentSuspend();
		setSubProcessState(ProcessInstance.STATE_SUSPENDED);
	}

	@Override
	public void suspend() {
		super.suspend();
		setSubProcessState(ProcessInstance.STATE_SUSPENDED);
	}

	@Override
	public void exit() {
		super.exit();
		killSubprocessGracefully();
	}

	private void killSubprocessGracefully() {
		WorkflowProcessInstance subProcess = (WorkflowProcessInstance) getProcessInstance().getKnowledgeRuntime().getProcessInstance(getProcessInstanceId());
		if (subProcess instanceof CaseInstance) {
			PlanElementState.terminateChildren((PlanItemInstanceContainerLifecycle) subProcess);
		} else {
			for (NodeInstance nodeInstance : subProcess.getNodeInstances()) {
				if (nodeInstance instanceof org.jbpm.workflow.instance.NodeInstance) {
					((org.jbpm.workflow.instance.NodeInstance) nodeInstance).cancel();
				}
			}
		}
		subProcess.setState(ProcessInstance.STATE_COMPLETED);
	}

	public void cancel() {
		super.cancel();
		setSubProcessState(ProcessInstance.STATE_ABORTED);
	}

	protected void setSubProcessState(int processState) {
		if (getPlanItemDefinition() != null && getPlanItemDefinition().isBlocking()) {
			ProcessInstance processInstance = (ProcessInstance) ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime().getProcessInstance(processInstanceId);
			if (processInstance != null) {
				processInstance.setState(processState);
			}
		}
	}

	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public void internalSetProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public void addEventListeners() {
		super.addEventListeners();
		addProcessListener();
	}

	private void addProcessListener() {
		getProcessInstance().addEventListener("processInstanceCompleted:" + processInstanceId, this, true);
	}

	public void removeEventListeners() {
		super.removeEventListeners();
		getProcessInstance().removeEventListener("processInstanceCompleted:" + processInstanceId, this, true);
	}

	public void signalEvent(String type, Object event) {
		if (type.equals(WORK_ITEM_UPDATED) && isMyWorkItem((WorkItem) event)) {
			this.workItem = (WorkItem) event;
			PlanItemTransition transition = (PlanItemTransition) workItem.getResult(HumanTaskPlanItemInstance.TRANSITION);
			if (transition == PlanItemTransition.TERMINATE) {
				if (!getPlanElementState().isTerminalState()) {
					transition.invokeOn(this);
				}
			} else {
				transition.invokeOn(this);
			}
		} else if (("processInstanceCompleted:" + processInstanceId).equals(type) && !getPlanElementState().isTerminalState()) {
			processInstanceCompleted((ProcessInstance) event);
		} else {
			super.signalEvent(type, event);
		}
	}

	public String[] getEventTypes() {
		return new String[] { "processInstanceCompleted:" + processInstanceId, WORK_ITEM_UPDATED };
	}

	public void processInstanceCompleted(ProcessInstance processInstance) {
		removeEventListeners();
		handleOutMappings(processInstance);
		if (processInstance.getState() == ProcessInstance.STATE_ABORTED) {
			String faultName = processInstance.getOutcome() == null ? "" : processInstance.getOutcome();
			// handle exception as sub process failed with error code
			ExceptionScopeInstance exceptionScopeInstance = (ExceptionScopeInstance) resolveContextInstance(ExceptionScope.EXCEPTION_SCOPE, faultName);
			if (exceptionScopeInstance != null) {
				exceptionScopeInstance.handleException(faultName, null);
			}
			internalFault();
		} else {
			internalComplete();
		}
	}

	private void handleOutMappings(ProcessInstance processInstance) {
		// TODO
	}

	public String getNodeName() {
		Node node = getNode();
		if (node == null) {
			return "[Dynamic] Sub Process";
		}
		return super.getNodeName();
	}

	@Override
	protected boolean isWaitForCompletion() {
		return getPlanItemDefinition().isBlocking();
	}


}
