package org.pavanecce.cmmn.jbpm.instance;

import org.pavanecce.cmmn.jbpm.instance.impl.CaseInstance;

public interface PlanElementLifecycle {
	void create();

	void suspend();

	void terminate();

	void setPlanElementState(PlanElementState s);

	PlanElementState getPlanElementState();
	
	CaseInstance getCaseInstance();

}