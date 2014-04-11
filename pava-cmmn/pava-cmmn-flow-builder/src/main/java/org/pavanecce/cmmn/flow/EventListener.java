package org.pavanecce.cmmn.flow;

import org.jbpm.workflow.core.node.EventNode;

public class EventListener extends EventNode implements PlanItemDefinition {
	private String elementId;

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}
}
