package org.pavanecce.cmmn.jbpm.instance.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.RuntimeDroolsException;
import org.drools.core.process.core.Work;
import org.drools.core.spi.ProcessContext;
import org.jbpm.process.core.Context;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.context.exception.ExceptionScope;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.ContextInstanceContainer;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.StartProcessHelper;
import org.jbpm.process.instance.context.exception.ExceptionScopeInstance;
import org.jbpm.process.instance.impl.ContextInstanceFactory;
import org.jbpm.process.instance.impl.ContextInstanceFactoryRegistry;
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
import org.pavanecce.cmmn.jbpm.flow.CaseTaskPlanItem;
import org.pavanecce.cmmn.jbpm.flow.ParameterMapping;
import org.pavanecce.cmmn.jbpm.instance.ControllablePlanItemInstanceLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseTaskPlanItemInstance extends TaskPlanItemInstance<CaseTask> implements ControllablePlanItemInstanceLifecycle<CaseTask>, EventListener, ContextInstanceContainer {

	private static final long serialVersionUID = -2144001908752174712L;
	private static final Logger logger = LoggerFactory.getLogger(CaseTaskPlanItemInstance.class);

	// NOTE: ContetxInstances are not persisted as current functionality (exception scope) does not require it
	private Map<String, List<ContextInstance>> subContextInstances = new HashMap<String, List<ContextInstance>>();

	private long processInstanceId;

	protected CaseTaskPlanItem getSubProcessNode() {
		return (CaseTaskPlanItem) getNode();
	}

	@Override
	public void start() {
		super.start();
		startProcess();
	}

	@Override
	public void manualStart() {
		super.manualStart();
		startProcess();
	}

	public void startProcess() {
		super.buildParameters(getWork());
		String processId = getSubProcessNode().getProcessId();
		if (processId == null) {
			// if process id is not given try with process name
			processId = getSubProcessNode().getProcessName();
		}

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

	public List<ParameterMapping> prepareParameterMappings(Process process) {
		List<ParameterMapping> parameterMappings = getSubProcessNode().getPlanInfo().getDefinition().getParameterMappings();
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
	public void suspend() {
		super.suspend();
		setSubProcessState(ProcessInstance.STATE_SUSPENDED);
	}

	@Override
	public void exit() {
		super.exit();
		killSubprocessGracefully();
	}

	protected void killSubprocessGracefully() {
		WorkflowProcessInstance subProcess = (WorkflowProcessInstance) getProcessInstance().getKnowledgeRuntime().getProcessInstance(getProcessInstanceId());
		for (NodeInstance nodeInstance : subProcess.getNodeInstances()) {
			if (nodeInstance instanceof org.jbpm.workflow.instance.NodeInstance) {
				((org.jbpm.workflow.instance.NodeInstance) nodeInstance).cancel();
			}
		}
		subProcess.setState(ProcessInstance.STATE_COMPLETED);
	}

	public void cancel() {
		super.cancel();
		setSubProcessState(ProcessInstance.STATE_ABORTED);
	}

	protected void setSubProcessState(int processState) {
		if (getSubProcessNode() == null || !getSubProcessNode().isIndependent()) {
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
		if (("processInstanceCompleted:" + processInstanceId).equals(type) && !getPlanElementState().isTerminalState()) {
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
			fault();
		} else {
			complete();
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
	public List<ContextInstance> getContextInstances(String contextId) {
		return this.subContextInstances.get(contextId);
	}

	@Override
	public void addContextInstance(String contextId, ContextInstance contextInstance) {
		List<ContextInstance> list = this.subContextInstances.get(contextId);
		if (list == null) {
			list = new ArrayList<ContextInstance>();
			this.subContextInstances.put(contextId, list);
		}
		list.add(contextInstance);
	}

	@Override
	public void removeContextInstance(String contextId, ContextInstance contextInstance) {
		List<ContextInstance> list = this.subContextInstances.get(contextId);
		if (list != null) {
			list.remove(contextInstance);
		}
	}

	@Override
	public ContextInstance getContextInstance(String contextId, long id) {
		List<ContextInstance> contextInstances = subContextInstances.get(contextId);
		if (contextInstances != null) {
			for (ContextInstance contextInstance : contextInstances) {
				if (contextInstance.getContextId() == id) {
					return contextInstance;
				}
			}
		}
		return null;
	}

	@Override
	public ContextInstance getContextInstance(Context context) {
		ContextInstanceFactory conf = ContextInstanceFactoryRegistry.INSTANCE.getContextInstanceFactory(context);
		if (conf == null) {
			throw new IllegalArgumentException("Illegal context type (registry not found): " + context.getClass());
		}
		ContextInstance contextInstance = (ContextInstance) conf.getContextInstance(context, this, (ProcessInstance) getProcessInstance());
		if (contextInstance == null) {
			throw new IllegalArgumentException("Illegal context type (instance not found): " + context.getClass());
		}
		return contextInstance;
	}

	@Override
	public ContextContainer getContextContainer() {
		return getSubProcessNode();
	}

	@Override
	protected Work getWork() {
		return getPlanItem().getWork();
	}

	@Override
	public CaseTaskPlanItem getPlanItem() {
		return (CaseTaskPlanItem) super.getPlanItem();
	}

	@Override
	protected boolean isWaitForCompletion() {
		return getPlanItem().isWaitForCompletion();
	}
}
