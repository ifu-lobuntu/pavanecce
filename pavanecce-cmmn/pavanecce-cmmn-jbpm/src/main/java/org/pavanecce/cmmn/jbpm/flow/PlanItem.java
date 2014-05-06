package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.Node;

public interface PlanItem extends CMMNElement, Node {

	public abstract void setElementId(String elementId);

	public abstract String getElementId();

	PlanItemInfo getPlanInfo();
}
