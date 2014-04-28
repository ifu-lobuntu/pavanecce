package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.node.CompositeContextNode;

public class Stage extends CompositeContextNode implements PlanItemDefinition{
	private static final long serialVersionUID = 3123425777169912160L;
	private String elementId;

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

}
