package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collections;
import java.util.List;

import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.DataAssociation;

public class Stage extends CompositeContextNode implements PlanItemDefinition{
	private static final long serialVersionUID = 3123425777169912160L;
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

}
