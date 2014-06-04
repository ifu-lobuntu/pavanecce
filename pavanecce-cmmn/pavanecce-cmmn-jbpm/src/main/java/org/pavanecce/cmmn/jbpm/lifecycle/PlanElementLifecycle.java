package org.pavanecce.cmmn.jbpm.lifecycle;

import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;

public interface PlanElementLifecycle {
	void create();

	void suspend();

	void terminate();

	void setPlanElementState(PlanElementState s);

	PlanElementState getPlanElementState();

	CaseInstance getCaseInstance();

}
