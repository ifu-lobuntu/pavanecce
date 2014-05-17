package org.pavanecce.cmmn.jbpm.instance;

public interface CaseInstanceLifecycle extends PlanElementLifecycleWithTask, PlanItemInstanceContainerLifecycle{
	void close();

}
