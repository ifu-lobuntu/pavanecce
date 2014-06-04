package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Definitions implements Serializable {

	private static final long serialVersionUID = 3235326449898795391L;
	private List<CaseFileItemDefinition> caseFileItemDefinitions = new ArrayList<CaseFileItemDefinition>();
	private String targetNamespace;

	public List<CaseFileItemDefinition> getCaseFileItemDefinitions() {
		return caseFileItemDefinitions;
	}

	public void setCaseFileItemDefinitions(List<CaseFileItemDefinition> caseFileItemDefinitions) {
		this.caseFileItemDefinitions = caseFileItemDefinitions;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}
}
