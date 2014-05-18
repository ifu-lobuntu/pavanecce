package org.pavanecce.cmmn.jbpm.instance;

import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;

public interface OccurrablePlanItemInstanceLifecycle <T extends  PlanItemDefinition > extends PlanItemInstanceLifecycle <T>{
	void occur();

	boolean isCompletionRequired();

	void internalSetRequired(boolean readBoolean);
}
