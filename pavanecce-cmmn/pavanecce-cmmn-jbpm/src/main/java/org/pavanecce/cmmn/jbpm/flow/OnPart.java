package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.event.EventFilter;
import org.jbpm.process.core.event.EventTypeFilter;
import org.jbpm.workflow.core.node.EventNode;
import org.pavanecce.cmmn.jbpm.event.CaseEvent;

public abstract class OnPart extends EventNode implements Serializable, CMMNElement {
	private static final long serialVersionUID = 1403604027414552700L;

	@Override
	public abstract String getType();

	@Override
	public List<EventFilter> getEventFilters() {
		EventTypeFilter f = new EventTypeFilter();
		f.setType(getType());
		return Arrays.asList((EventFilter) f);

	}

	public abstract Enum<?> getStandardEvent();

	@Override
	public String getElementId() {
		return getName();
	}

	@Override
	public boolean acceptsEvent(String type, Object event) {
		return type.equals(getType());
	}

	@Override
	public String getVariableName() {
		// /TODO qualify name with sentry name
		return getName() + "Var";
	}

	public static String getType(String fileItemName, Enum<?> transition) {
		return "On" + transition.name() + "Of" + fileItemName;
	}

	public Variable getVariable() {
		ContextContainer contextContainer = (ContextContainer) getNodeContainer();
		VariableScope vs = (VariableScope) contextContainer.getDefaultContext(VariableScope.VARIABLE_SCOPE);
		Variable var = vs.findVariable(getVariableName());
		if (var == null) {
			vs.getVariables().add(var = new Variable());
			var.setName(getVariableName());
		}
		return var;
	}

	public abstract CaseEvent createEvent(Object peek);

	public OnPart copy() {
		return null;
	}
}
