package org.pavanecce.cmmn.jbpm.instance;

import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;

public interface PlanItemInstanceLifecycleWithHistory<T extends PlanItemDefinition> extends PlanItemInstanceLifecycle<T> {
	void parentSuspend();

	void parentResume();

	void setLastBusyState(PlanElementState s);

	PlanElementState getLastBusyState();

	boolean isComplexLifecycle();

}
