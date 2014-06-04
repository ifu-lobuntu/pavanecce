package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.node.StateNode;

public class AbstractPlanItemDefinition extends StateNode implements PlanItemDefinition {

	private static final long serialVersionUID = -528614791490955918L;
	private String elementId;
	private String description;
	private PlanItemControl defaultControl;
	private Case theCase;

	public AbstractPlanItemDefinition() {
		super();
	}

	public final String getDescription() {
		return this.description;
	}

	public final void setDescription(String s) {
		this.description = s;
	}

	public final String getElementId() {
		return elementId;
	}

	public final void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public final PlanItemControl getDefaultControl() {
		return this.defaultControl;
	}

	@Override
	public final void setDefaultControl(PlanItemControl c) {
		this.defaultControl = c;

	}

	public final Case getCase() {
		return theCase;
	}

	public final void setCase(Case theCase) {
		this.theCase = theCase;
	}

}
