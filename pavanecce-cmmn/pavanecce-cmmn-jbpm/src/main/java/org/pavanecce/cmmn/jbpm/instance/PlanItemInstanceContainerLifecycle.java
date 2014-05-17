package org.pavanecce.cmmn.jbpm.instance;

public interface PlanItemInstanceContainerLifecycle extends PlanElementLifecycleWithTask {
	public abstract void fault();

	public abstract void complete();

	public abstract void reactivate();

}