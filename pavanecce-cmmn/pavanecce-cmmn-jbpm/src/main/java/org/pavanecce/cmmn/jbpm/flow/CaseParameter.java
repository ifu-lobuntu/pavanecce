package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;

public class CaseParameter implements Serializable, CMMNElement {
	private static final long serialVersionUID = -2726481569205195638L;
	private String id;
	private String bindingRef;
	private String name;
	private CaseFileItem variable;
	private BindingRefinement bindingRefinement;

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

	public CaseFileItem getBoundVariable() {
		return variable;
	}

	public void setBoundVariable(CaseFileItem variable) {
		this.variable = variable;
	}

	public BindingRefinement getBindingRefinement() {
		return bindingRefinement;
	}

	public void setBindingRefinement(BindingRefinement bindingRefinement) {
		this.bindingRefinement = bindingRefinement;
	}

}
