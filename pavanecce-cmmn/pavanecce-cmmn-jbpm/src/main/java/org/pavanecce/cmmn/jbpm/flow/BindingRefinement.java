package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.process.instance.impl.Action;
import org.jbpm.process.instance.impl.ReturnValueConstraintEvaluator;
import org.jbpm.process.instance.impl.ReturnValueEvaluator;
import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.impl.ConstraintImpl;

public class BindingRefinement {
	private Constraint expression;
	private Constraint parentExpression;
	private DroolsAction setterOnParent;

	public BindingRefinement(ConstraintImpl exp) {
		this.expression = exp;
	}

	public Constraint getParentExpression() {
		return parentExpression;
	}

	public void setParentExpression(Constraint bindingRefinement) {
		this.parentExpression = bindingRefinement;
	}

	public void setSetterOnParent(DroolsAction a) {
		this.setterOnParent = a;
	}

	public Constraint getExpression() {
		return expression;
	}

	public void setExpression(Constraint bindingRefinement) {
		this.expression = bindingRefinement;
	}

	public ReturnValueEvaluator getEvaluator() {
		if (expression instanceof ReturnValueConstraintEvaluator) {
			return ((ReturnValueConstraintEvaluator) expression).getReturnValueEvaluator();
		}
		return null;
	}

	public ReturnValueEvaluator getParentEvaluator() {
		if (parentExpression instanceof ReturnValueConstraintEvaluator) {
			return ((ReturnValueConstraintEvaluator) parentExpression).getReturnValueEvaluator();
		}
		return null;
	}

	public Action getSetterOnParent() {
		if (setterOnParent != null) {
			return (Action) setterOnParent.getMetaData("Action");
		}
		return null;
	}

	public boolean isValid() {
		return expression instanceof ReturnValueConstraintEvaluator;
	}

}
