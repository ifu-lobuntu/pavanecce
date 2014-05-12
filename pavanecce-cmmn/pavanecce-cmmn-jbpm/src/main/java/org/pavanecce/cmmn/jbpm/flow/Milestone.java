package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.node.StateBasedNode;

public class Milestone extends StateBasedNode implements PlanItemDefinition {

	private static final long serialVersionUID = -1756918726860177355L;
	private String elementId;
	private PlanItemControl defaultControl;
	@Override
	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public PlanItemControl getDefaultControl() {
		return defaultControl;
	}

	public void setDefaultControl(PlanItemControl defaultControl) {
		this.defaultControl = defaultControl;
	}

}
