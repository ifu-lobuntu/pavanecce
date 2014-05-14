package org.pavanecce.cmmn.jbpm.instance;

public interface CaseInstanceLifecycle extends CaseElementLifecycle, CaseElementLifecycleWithTask{
	void close();

	void fault();

	void complete();

	void reactivate();

}
