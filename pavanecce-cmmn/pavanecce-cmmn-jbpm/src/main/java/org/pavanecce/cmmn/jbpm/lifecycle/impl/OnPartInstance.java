package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Stack;

import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.node.EventNodeInstance;
import org.pavanecce.cmmn.jbpm.event.CaseEvent;
import org.pavanecce.cmmn.jbpm.event.CaseFileItemEvent;
import org.pavanecce.cmmn.jbpm.event.PlanItemEvent;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemOnPart;

public class OnPartInstance extends EventNodeInstance {

	private static final long serialVersionUID = 377496036154216192L;

	public OnPart getOnPart() {
		return (OnPart) getEventNode();
	}

	@Override
	public void signalEvent(String type, Object event) {
		if (event instanceof CaseFileItemEvent && getOnPart() instanceof CaseFileItemOnPart) {
			CaseFileItemOnPart onPart = (CaseFileItemOnPart) getOnPart();
			CaseFileItemEvent e = (CaseFileItemEvent) event;
			if (onPart.getSourceCaseFileItem().getName().equals(e.getCaseFileItemName()) && e.getTransition() == onPart.getStandardEvent()) {
				getEventStack().push(((CaseFileItemEvent) event).getValue());
				triggerCompleted();
			}
		} else if (event instanceof PlanItemEvent && getOnPart() instanceof PlanItemOnPart) {
			PlanItemOnPart onPart = (PlanItemOnPart) getOnPart();
			PlanItemEvent e = (PlanItemEvent) event;
			if (e.getPlanItemName().equals(onPart.getSourcePlanItem().getName()) && e.getTransition() == onPart.getStandardEvent()) {
				getEventStack().push(((PlanItemEvent) event).getValue());
				triggerCompleted();

			}
		}
	}

	public Object popEvent() {
		return getEventStack().pop();
	}

	@SuppressWarnings("unchecked")
	private Stack<Object> getEventStack() {
		String variableName = getOnPart().getVariableName();
		VariableScopeInstance variableScopeInstance = (VariableScopeInstance) resolveContextInstance(VariableScope.VARIABLE_SCOPE, getOnPart()
				.getVariableName());
		if (variableScopeInstance == null) {
			throw new IllegalArgumentException("Could not find variable for event node: " + variableName);
		}
		Stack<Object> variable = (Stack<Object>) variableScopeInstance.getVariable(variableName);
		if (variable == null) {
			variableScopeInstance.setVariable(variableName, variable = new Stack<Object>());
		}
		return variable;
	}

	@Override
	public void triggerCompleted() {
		((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).setCurrentLevel(getLevel());
		triggerCompleted(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE, false);
	}

	public CaseEvent getCaseEvent() {
		return getOnPart().createEvent(getEventStack().peek());
	}

	public CaseInstance getCaseInstance() {
		return (CaseInstance) getProcessInstance();
	}

}
