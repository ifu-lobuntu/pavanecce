package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.Node;

public interface PlanItem<T extends PlanItemDefinition> extends CMMNElement, Node {

	public abstract void setElementId(String elementId);

	@Override
	public abstract String getElementId();

	PlanItemInfo<T> getPlanInfo();

}
