package org.pavanecce.cmmn.jbpm.instance;

import java.util.Map;

import org.jbpm.process.core.Context;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.ContextInstanceContainer;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.runtime.process.NodeInstance;

public class PlanItemInstance extends WorkItemNodeInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3200294767777991641L;

	@Override
	public ContextInstance resolveContextInstance(String contextId, Object param) {
		
		final ContextInstance result = super.resolveContextInstance(contextId, param);
		if(contextId.equals(VariableScope.VARIABLE_SCOPE)){
			return new VariableScopeInstance() {
				private static final long serialVersionUID = 6026168560982471308L;
				private VariableScopeInstance delegate=(VariableScopeInstance) result;

				public void setContextId(long contextId) {
					delegate.setContextId(contextId);
				}

				public ContextInstanceContainer getContextInstanceContainer() {
					return delegate.getContextInstanceContainer();
				}

				public Context getContext() {
					return delegate.getContext();
				}

				public String getContextType() {
					return delegate.getContextType();
				}

				public Object getVariable(String name) {
					if(name.equals("currentEvent")){
						return SentryInstance.getCurrentEvents().iterator().next().getValue();
					}
					if(name.equals("currentEvents")){
						return SentryInstance.getCurrentEvents();
					}
					return delegate.getVariable(name);
				}

				public ProcessInstance getProcessInstance() {
					return delegate.getProcessInstance();
				}

				public Map<String, Object> getVariables() {
					return delegate.getVariables();
				}

				public void setProcessInstance(ProcessInstance processInstance) {
					delegate.setProcessInstance(processInstance);
				}

				public void setVariable(String name, Object value) {
					delegate.setVariable(name, value);
				}

				public void internalSetVariable(String name, Object value) {
					delegate.internalSetVariable(name, value);
				}

				public VariableScope getVariableScope() {
					return delegate.getVariableScope();
				}

				public void setContextInstanceContainer(ContextInstanceContainer contextInstanceContainer) {
					delegate.setContextInstanceContainer(contextInstanceContainer);
				}
				
				
			};
		}
		return result;
	}

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		// TODO Auto-generated method stub
		super.internalTrigger(from, type);
	}
	// @Override
	// public void addEventListeners() {
	// PlanItem planItem=(PlanItem) getNode();
	// Collection<Sentry> values = planItem.getExitCriteria().values();
	// for (Sentry sentry : values) {
	// ((WorkflowProcessInstance)
	// getProcessInstance()).addEventListener("timerTriggered", this, false);
	// }
	// // skip workITem listeners because CMMN takes care of exit criteria
	// if (getTimerInstances() != null && getTimerInstances().size() > 0) {
	// addTimerListener();
	// }
	// }
	// @Override
	// public void signalEvent(String type, Object event) {
	// PlanItem planItem = (PlanItem) getNode();
	// if (planItem.getExitCriteria().isEmpty()) {
	// if ("workItemCompleted".equals(type)) {
	// workItemCompleted((WorkItem) event);
	// } else if ("workItemAborted".equals(type)) {
	// workItemAborted((WorkItem) event);
	// } else {
	// super.signalEvent(type, event);
	// }
	// }
	// }
}
