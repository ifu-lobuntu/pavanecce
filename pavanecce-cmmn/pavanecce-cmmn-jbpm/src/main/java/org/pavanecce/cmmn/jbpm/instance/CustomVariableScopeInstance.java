package org.pavanecce.cmmn.jbpm.instance;

import java.util.Map;

import org.jbpm.process.core.Context;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.ContextInstanceContainer;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.SentryInstance;
/**
 * The purpose of this class is to make "special" variables available from the environment
 * TODO find a better way of doing this
 * @author ampie
 *
 */
public final class CustomVariableScopeInstance extends VariableScopeInstance {
	private static final long serialVersionUID = 6026168560982471308L;
	private VariableScopeInstance delegate;

	public CustomVariableScopeInstance(ContextInstance result) {
		delegate = (VariableScopeInstance) result;
	}

	@Override
	public void setContextId(long contextId) {
		delegate.setContextId(contextId);
	}

	@Override
	public ContextInstanceContainer getContextInstanceContainer() {
		return delegate.getContextInstanceContainer();
	}

	@Override
	public Context getContext() {
		return delegate.getContext();
	}

	@Override
	public String getContextType() {
		return delegate.getContextType();
	}

	@Override
	public Object getVariable(String name) {
		if (name.equals("currentEvent")) {
			return SentryInstance.getCurrentEvents().iterator().next();
		}
		if (name.equals("currentEvents")) {
			return SentryInstance.getCurrentEvents();
		}
		return delegate.getVariable(name);
	}

	@Override
	public ProcessInstance getProcessInstance() {
		return delegate.getProcessInstance();
	}

	@Override
	public Map<String, Object> getVariables() {
		return delegate.getVariables();
	}

	@Override
	public void setProcessInstance(ProcessInstance processInstance) {
		delegate.setProcessInstance(processInstance);
	}

	@Override
	public void setVariable(String name, Object value) {
		delegate.setVariable(name, value);
	}

	@Override
	public void internalSetVariable(String name, Object value) {
		delegate.internalSetVariable(name, value);
	}

	@Override
	public VariableScope getVariableScope() {
		return delegate.getVariableScope();
	}

	@Override
	public void setContextInstanceContainer(ContextInstanceContainer contextInstanceContainer) {
		delegate.setContextInstanceContainer(contextInstanceContainer);
	}
}