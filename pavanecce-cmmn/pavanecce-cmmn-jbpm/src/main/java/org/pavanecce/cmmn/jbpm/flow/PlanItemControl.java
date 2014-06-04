package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;

import org.jbpm.workflow.core.Constraint;

public class PlanItemControl implements Serializable, CMMNElement {

	private static final long serialVersionUID = 166L;
	private String elementId;
	private Constraint manualActivationRule;
	private Constraint requiredRule;
	private Constraint repetionRule;

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public Constraint getManualActivationRule() {
		return manualActivationRule;
	}

	public void setManualActivationRule(Constraint automaticActivationRule) {
		this.manualActivationRule = automaticActivationRule;
	}

	public Constraint getRequiredRule() {
		return requiredRule;
	}

	public void setRequiredRule(Constraint requiredRule) {
		this.requiredRule = requiredRule;
	}

	public Constraint getRepetitionRule() {
		return repetionRule;
	}

	public void setRepetitionRule(Constraint repetionRule) {
		this.repetionRule = repetionRule;
	}

}
