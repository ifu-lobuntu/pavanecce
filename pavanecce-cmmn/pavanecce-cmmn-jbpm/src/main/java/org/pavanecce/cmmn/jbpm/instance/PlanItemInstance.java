package org.pavanecce.cmmn.jbpm.instance;

import org.pavanecce.cmmn.jbpm.flow.PlanItem;

public interface PlanItemInstance {
	PlanItemState getPlanItemState();

	PlanItem getPlanItem();

	void setPlanItemState(PlanItemState s);

	void setLastBusyState(PlanItemState s);

	void enable();

	void disable();

	void reenable();

	void start();

	void manualStart();

	void reactivate();

	void suspend();

	void resume();

	void terminate();

	void exit();

	void complete();

	void parentSuspend();

	void parentResume();

	void parentTerminate();

	void create();

	void fault();

	void occur();

	void close();

	PlanItemState getLastBusyState();

	CaseInstance getCaseInstance();

}
