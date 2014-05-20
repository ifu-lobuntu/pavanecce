package org.pavanecce.cmmn.jbpm.lifecycle;

import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;

public interface ControllableItemInstanceLifecycle<T extends PlanItemDefinition> extends ItemInstanceLifecycleWithHistory<T>, PlanElementLifecycleWithTask {

	void enable();

	void disable();

	void reenable();

	void start();

	void manualStart();

	boolean isCompletionRequired();

	void internalSetCompletionRequired(boolean b);

	boolean isCompletionStillRequired();

}
