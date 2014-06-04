package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Map;

import org.jbpm.process.core.Context;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstanceContainer;
import org.jbpm.process.instance.ContextableInstance;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;

/**
 * The purpose of this class is to make "special" variables available from the environment TODO find a better way of
 * doing this
 * 
 * @author ampie
 * 
 */
public final class CustomVariableScopeInstance extends VariableScopeInstance {
	private static final long serialVersionUID = 6026168560982471308L;
	private ControllableItemInstanceImpl<?, ?> node;

	public CustomVariableScopeInstance(ControllableItemInstanceImpl<?, ?> nodeImpl) {
		this.node = nodeImpl;
	}

	@Override
	public void setContextId(long contextId) {
		getDelegate().setContextId(contextId);
	}

	@Override
	public ContextInstanceContainer getContextInstanceContainer() {
		return getDelegate().getContextInstanceContainer();
	}

	private VariableScopeInstance getDelegate() {
		VariableScopeInstance result = (VariableScopeInstance) node.getContextInstance(VariableScope.VARIABLE_SCOPE);
		if (result == null) {
			return (VariableScopeInstance) ((ContextableInstance) node.getNodeInstanceContainer()).getContextInstance(VariableScope.VARIABLE_SCOPE);
		}
		return result;
	}

	@Override
	public Context getContext() {
		return getDelegate().getContext();
	}

	@Override
	public String getContextType() {
		return getDelegate().getContextType();
	}

	@Override
	public Object getVariable(String name) {
		if (name.equals("currentEvent")) {
			return SentryInstance.getCurrentEvents().iterator().next();
		}
		if (name.equals("currentEvents")) {
			return SentryInstance.getCurrentEvents();
		}
		VariableScopeInstance delegate = getDelegate();
		if (delegate != null) {
			return delegate.getVariable(name);
		} else {
			// TODO find out when this happens
			return null;
		}
	}

	@Override
	public ProcessInstance getProcessInstance() {
		return getDelegate().getProcessInstance();
	}

	@Override
	public Map<String, Object> getVariables() {
		return getDelegate().getVariables();
	}

	@Override
	public void setProcessInstance(ProcessInstance processInstance) {
		getDelegate().setProcessInstance(processInstance);
	}

	@Override
	public void setVariable(String name, Object value) {
		getDelegate().setVariable(name, value);
	}

	@Override
	public void internalSetVariable(String name, Object value) {
		getDelegate().internalSetVariable(name, value);
	}

	@Override
	public VariableScope getVariableScope() {
		return getDelegate().getVariableScope();
	}

	@Override
	public void setContextInstanceContainer(ContextInstanceContainer contextInstanceContainer) {
		getDelegate().setContextInstanceContainer(contextInstanceContainer);
	}
}