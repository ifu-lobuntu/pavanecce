package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;

public class CaseFileItemDefinition implements Serializable, CMMNElement {

	private static final long serialVersionUID = -4667585177916762919L;
	private CaseFileItemDefinitionType definitionType;
	private String structureRef;
	private String id;

	public CaseFileItemDefinition(String id) {
		this.id = id;
	}

	public CaseFileItemDefinitionType getDefinitionType() {
		return definitionType;
	}

	public void setDefinitionType(CaseFileItemDefinitionType definitionType) {
		this.definitionType = definitionType;
	}

	public String getStructureRef() {
		return structureRef;
	}

	public void setStructureRef(String structureRef) {
		this.structureRef = structureRef;
	}

	@Override
	public String getElementId() {
		return id;
	}
}
