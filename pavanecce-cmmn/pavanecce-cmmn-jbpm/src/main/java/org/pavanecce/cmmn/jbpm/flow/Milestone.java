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

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}
	@Override
	public List<DataAssociation> getInAssociations() {
		return Collections.emptyList();
	}

	@Override
	public List<CaseParameter> getInputs() {
		return Collections.emptyList();
	}
	

}
