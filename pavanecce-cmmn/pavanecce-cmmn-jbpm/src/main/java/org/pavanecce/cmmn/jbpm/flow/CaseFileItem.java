package org.pavanecce.cmmn.jbpm.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private Map<String, CaseFileItem> targets = new HashMap<String, CaseFileItem>();
	private boolean isUnique = true;
	private boolean isOrdered = false;

	public boolean isUnique() {
		return isUnique;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	public boolean isOrdered() {
		return isOrdered;
	}

	public void setOrdered(boolean isOrdered) {
		this.isOrdered = isOrdered;
	}

	@Override
	public String getElementId() {
		return id;
	}

	public List<CaseFileItem> getChildren() {
		return children;
	}

	public Map<String, CaseFileItem> getTargets() {
		return targets;
	}

	public void putTarget(String id, CaseFileItem t) {
		targets.put(id, t);
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

}
