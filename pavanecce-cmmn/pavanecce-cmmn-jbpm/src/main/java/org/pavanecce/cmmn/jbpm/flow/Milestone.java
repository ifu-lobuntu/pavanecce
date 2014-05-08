package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collections;
import java.util.List;

import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.MilestoneNode;

public class Milestone extends MilestoneNode implements PlanItemDefinition {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1756918726860177355L;
	private String elementId;

	@Override
	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}


}
