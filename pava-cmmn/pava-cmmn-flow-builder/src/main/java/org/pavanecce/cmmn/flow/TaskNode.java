package org.pavanecce.cmmn.flow;

import org.jbpm.workflow.core.node.StateBasedNode;
import org.jbpm.workflow.core.node.WorkItemNode;

public class TaskNode extends WorkItemNode implements PlanItemDefinition {
	private String elementId;

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

}
