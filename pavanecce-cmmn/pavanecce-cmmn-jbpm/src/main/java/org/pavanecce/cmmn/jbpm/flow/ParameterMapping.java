package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;

import org.jbpm.workflow.core.Constraint;

public class ParameterMapping implements Serializable, CMMNElement {
	private static final long serialVersionUID = -3627421388193991961L;
	private String elementId;
	private String sourceRef;
	private CaseParameter sourceParameter;
	private String targetRef;
	private Constraint transformation;
	
	public String getSourceRef() {
		return sourceRef;
	}

	public void setSourceRef(String sourceRef) {
		this.sourceRef = sourceRef;
	}

	public CaseParameter getSourceParameter() {
		return sourceParameter;
	}

	public void setSourceParameter(CaseParameter sourceParameter) {
		this.sourceParameter = sourceParameter;
	}

	public String getTargetRef() {
		return targetRef;
	}

	public void setTargetRef(String targetRef) {
		this.targetRef = targetRef;
	}

	@Override
	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public Constraint getTransformation() {
		return transformation;
	}

	public void setTransformation(Constraint transformation) {
		this.transformation = transformation;
	}

}
