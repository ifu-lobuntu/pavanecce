package org.pavanecce.cmmn.jbpm.instance;

public interface CaseInstanceLifecycle extends CaseElementLifecycle, CaseElementLifecycleWithTask, PlanItemInstanceContainerLifecycle{
	void close();

}
