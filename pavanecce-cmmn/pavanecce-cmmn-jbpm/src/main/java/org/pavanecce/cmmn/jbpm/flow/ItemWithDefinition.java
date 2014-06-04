package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.Node;

public interface ItemWithDefinition<T extends PlanItemDefinition> extends CMMNElement, Node {
	T getDefinition();

	PlanItemControl getItemControl();

	PlanItemControl getEffectiveItemControl();

	String getName();

	String getEffectiveName();

	PlanItemContainer getPlanItemContainer();

	String getPlanItemEventName();

}
