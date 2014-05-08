package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;

import org.jbpm.process.instance.impl.ReturnValueConstraintEvaluator;
import org.jbpm.process.instance.impl.ReturnValueEvaluator;
import org.jbpm.workflow.core.Constraint;

public class CaseParameter implements Serializable,CMMNElement{
	private static final long serialVersionUID = -2726481569205195638L;
	private String id;
	private String bindingRef;
	private String name;
	private CaseFileItem variable;
	private Constraint bindingRefinement;
	private Constraint bindingRefinementParent;
	@Override
	public String getElementId() {
		return id;
	}
	public void setElementId(String id) {
		this.id = id;
	}
	public String getBindingRef() {
		return bindingRef;
	}
	public void setBindingRef(String bindingRef) {
		this.bindingRef = bindingRef;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public CaseFileItem getVariable() {
		return variable;
	}
	public void setVariable(CaseFileItem variable) {
		this.variable = variable;
	}
	public Constraint getBindingRefinementParent() {
		return bindingRefinementParent;
	}
	public void setBindingRefinementParent(Constraint bindingRefinement) {
		this.bindingRefinementParent = bindingRefinement;
	}
	public Constraint getBindingRefinement() {
		return bindingRefinement;
	}
	public void setBindingRefinement(Constraint bindingRefinement) {
		this.bindingRefinement = bindingRefinement;
	}
	public ReturnValueEvaluator getBindingRefinementEvaluator() {
		if(bindingRefinement instanceof ReturnValueConstraintEvaluator){
			return ((ReturnValueConstraintEvaluator) bindingRefinement).getReturnValueEvaluator();
		}
		return null;
	}
	public ReturnValueEvaluator getBindingRefinementParentEvaluator() {
		if(bindingRefinementParent instanceof ReturnValueConstraintEvaluator){
			return ((ReturnValueConstraintEvaluator) bindingRefinementParent).getReturnValueEvaluator();
		}
		return null;
	}

	
	
	
}
