package org.pavanecce.cmmn.jbpm.lifecycle;

import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;

public interface OccurrablePlanItemInstance<T extends PlanItemDefinition> extends PlanItemInstance<T> {
	void occur();

	boolean isCompletionRequired();

	void internalSetRequired(boolean readBoolean);

	PlanItem<T> getPlanItem();
}
