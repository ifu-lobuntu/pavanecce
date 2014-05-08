package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collections;
import java.util.List;

import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.EventNode;

public class EventListener extends EventNode implements PlanItemDefinition {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2391269457943140229L;
	private String elementId;

	@Override
	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

}
