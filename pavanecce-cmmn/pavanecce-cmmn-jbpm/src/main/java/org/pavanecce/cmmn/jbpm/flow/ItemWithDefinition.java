package org.pavanecce.cmmn.jbpm.flow;

public interface ItemWithDefinition<T extends PlanItemDefinition> extends CMMNElement {
	T getDefinition();

	PlanItemControl getItemControl();

	PlanItemControl getEffectiveItemControl();

	String getName();

	String getEffectiveName();

	PlanItemContainer getPlanItemContainer();

	String getPlanItemEventName();

}
