package org.pavanecce.cmmn.jbpm.instance;

import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;

public interface ControllablePlanItemInstanceLifecycle<T extends PlanItemDefinition> extends PlanItemInstanceLifecycleWithHistory<T>, PlanElementLifecycleWithTask {


	void enable();

	void disable();

	void reenable();

	void start();

	void manualStart();

	void reactivate();

	void resume();

	void exit();

	void complete();

	void fault();

	boolean isCompletionRequired();

	void internalSetCompletionRequired(boolean b);

	boolean isCompletionStillRequired();
}
