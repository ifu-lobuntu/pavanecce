package org.pavanecce.cmmn.jbpm.lifecycle;

import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;

public interface ItemInstanceLifecycleWithHistory<T extends PlanItemDefinition> extends ItemInstanceLifecycle<T> {
	void parentSuspend();

	void parentResume();

	void exit();

	void setLastBusyState(PlanElementState s);

	PlanElementState getLastBusyState();

	boolean isComplexLifecycle();

}
