package org.pavanecce.cmmn.jbpm.instance;

public interface OccurrablePlanItemInstanceLifecycle extends PlanItemInstanceLifecycle {
	void occur();

	void parentTerminate();

	void resume();
}
