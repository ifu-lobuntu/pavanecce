package org.pavanecce.cmmn.flow;

import java.io.Serializable;

import org.jbpm.process.core.context.variable.Variable;

public class CaseParameter implements Serializable,CMMNElement{
	private static final long serialVersionUID = -2726481569205195638L;
	private String id;
	private String bindingRef;
	private String name;
	private CaseFileItem variable;
	private String bindingRefinement;
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
	public String getBindingRefinement() {
		return bindingRefinement;
	}
	public void setBindingRefinement(String bindingRefinement) {
		this.bindingRefinement = bindingRefinement;
	}
	
	
}
