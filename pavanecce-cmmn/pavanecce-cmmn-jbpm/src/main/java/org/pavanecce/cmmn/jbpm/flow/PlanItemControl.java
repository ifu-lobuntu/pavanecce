package org.pavanecce.cmmn.jbpm.flow;

import java.io.Serializable;

import org.jbpm.workflow.core.Constraint;

public class PlanItemControl implements Serializable, CMMNElement {

	private static final long serialVersionUID = 166L;
	private String elementId;
	private Constraint automaticActivationRule;
	private Constraint requiredRule;
	private Constraint repetionRule;
	public String getElementId() {
		return elementId;
	}
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}
	public Constraint getAutomaticActivationRule() {
		return automaticActivationRule;
	}
	public void setAutomaticActivationRule(Constraint automaticActivationRule) {
		this.automaticActivationRule = automaticActivationRule;
	}
	public Constraint getRequiredRule() {
		return requiredRule;
	}
	public void setRequiredRule(Constraint requiredRule) {
		this.requiredRule = requiredRule;
	}
	public Constraint getRepetionRule() {
		return repetionRule;
	}
	public void setRepetionRule(Constraint repetionRule) {
		this.repetionRule = repetionRule;
	}
	
}
