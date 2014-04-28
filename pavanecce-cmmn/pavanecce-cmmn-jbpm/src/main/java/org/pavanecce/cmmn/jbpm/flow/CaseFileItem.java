package org.pavanecce.cmmn.jbpm.flow;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.process.core.context.variable.Variable;

public class CaseFileItem extends Variable implements CMMNElement {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8248796967737773161L;
	private String id;
	private String definitionRef;
	private CaseFileItemDefinition definition;
	private boolean isCollection;
	private List<CaseFileItem> children = new ArrayList<CaseFileItem>();
	private List<CaseFileItem> targets = new ArrayList<CaseFileItem>();
	private List<String> targetRefs;

	public String getElementId() {
		return id;
	}

	public List<CaseFileItem> getChildren() {
		return children;
	}

	public List<CaseFileItem> getTargets() {
		return targets;
	}
	public void addTarget(CaseFileItem t){
		targets.add(t);
	}

	public void addChild(CaseFileItem c) {
		children.add(c);
	}

	public void setElementId(String id) {
		this.id = id;
	}

	public void setDefinitionRef(String value) {
		this.definitionRef = value;
	}

	public String getDefinitionRef() {
		return definitionRef;
	}

	public CaseFileItemDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(CaseFileItemDefinition definition) {
		this.definition = definition;
	}

	public boolean isCollection() {
		return isCollection;
	}

	public void setCollection(boolean isCollection) {
		this.isCollection = isCollection;
	}

	public void setTargetRefs(List<String> asList) {
		this.targetRefs = asList;

	}

	public List<String> getTargetRefs() {
		return targetRefs;
	}
}
