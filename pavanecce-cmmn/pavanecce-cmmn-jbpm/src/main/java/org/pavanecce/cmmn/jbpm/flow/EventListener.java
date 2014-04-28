package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.node.EventNode;

public class EventListener extends EventNode implements PlanItemDefinition {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2391269457943140229L;
	private String elementId;

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}
}
