package org.pavanecce.cmmn.jbpm.flow;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.workflow.core.node.SubProcessNode;

public class CaseTask extends SubProcessNode implements TaskDefinition {
	private static final long serialVersionUID = 7495168121066617656L;
	List<CaseParameter> inputs = new ArrayList<CaseParameter>();
	List<CaseParameter> outputs = new ArrayList<CaseParameter>();
	List<ParameterMapping> mappings = new ArrayList<ParameterMapping>();
	String elementId;
	private boolean blocking;

	@Override
	public List<CaseParameter> getInputs() {
		return inputs;
	}

	@Override
	public List<CaseParameter> getOutputs() {
		return outputs;
	}

	@Override
	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public void addOutputParameter(CaseParameter cp) {
		this.outputs.add(cp);
	}

	@Override
	public void addInputParameter(CaseParameter cp) {
		this.inputs.add(cp);

	}

	public void setBlocking(boolean b) {
		this.blocking = b;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void addParameterMapping(ParameterMapping cp) {
		mappings.add(cp);

	}
	public List<ParameterMapping> getParameterMappings() {
		return mappings;
	}
	public void mapParameters() {
		for (ParameterMapping parameterMapping : this.mappings) {
			if (!findMapping(this.inputs, parameterMapping)) {
				findMapping(inputs, parameterMapping);
			}
		}
	}

	protected boolean findMapping(List<CaseParameter> inputs2, ParameterMapping parameterMapping) {
		for (CaseParameter caseParameter : inputs2) {
			if (parameterMapping.getSourceRef().equals(caseParameter.getElementId())) {
				parameterMapping.setSourceParameter(caseParameter);
				return true;
			}
		}
		return false;
	}
}
