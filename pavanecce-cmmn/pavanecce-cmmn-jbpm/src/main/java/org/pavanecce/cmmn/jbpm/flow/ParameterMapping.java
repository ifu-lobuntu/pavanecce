package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;

import org.jbpm.process.instance.impl.ReturnValueConstraintEvaluator;
import org.jbpm.process.instance.impl.ReturnValueEvaluator;
import org.jbpm.workflow.core.Constraint;

public class ParameterMapping implements Serializable, CMMNElement {
	private static final long serialVersionUID = -3627421388193991961L;
	private String elementId;
	private String sourceRef;
	private CaseParameter sourceParameter;
	private String targetRef;
	private Constraint transformation;
	private String targetParameterName;
	private CaseParameter targetParameter;
	private String sourceParameterName;

	public String getSourceRef() {
		return sourceRef;
	}

	public void setSourceRef(String sourceRef) {
		this.sourceRef = sourceRef;
	}

	public CaseParameter getSourceParameter() {
		return sourceParameter;
	}

	public CaseParameter getTargetParameter() {
		return targetParameter;
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

	public ReturnValueEvaluator getTransformer() {
		if (transformation instanceof ReturnValueConstraintEvaluator) {
			return ((ReturnValueConstraintEvaluator) transformation).getReturnValueEvaluator();
		} else {
			return null;
		}
	}

	public void setTransformation(Constraint transformation) {
		this.transformation = transformation;
	}

	public String getTargetParameterName() {
		if (targetParameter != null) {
			return targetParameter.getName();
		}
		if (targetParameterName == null) {
			return getTargetParameterId();
		}
		return targetParameterName;
	}

	public String getSourceParameterName() {
		if (sourceParameter != null) {
			return sourceParameter.getName();
		}
		if (sourceParameterName == null) {
			return getSourceParameterId();
		}
		return sourceParameterName;
	}

	public void setTargetParameterName(String targetParameterName) {
		this.targetParameterName = targetParameterName;
	}

	public void setSourceParameterName(String sourceParameterName) {
		this.sourceParameterName = sourceParameterName;
	}

	public String getSourceParameterId() {
		String[] split = sourceRef.split("\\#");
		return split[split.length - 1];
	}

	public String getTargetParameterId() {
		String[] split = targetRef.split("\\#");
		return split[split.length - 1];
	}

	public void setTargetParameter(CaseParameter cp) {
		this.targetParameter = cp;

	}

	public void setSourceParameter(CaseParameter sourceParameter) {
		this.sourceParameter = sourceParameter;
	}

}
