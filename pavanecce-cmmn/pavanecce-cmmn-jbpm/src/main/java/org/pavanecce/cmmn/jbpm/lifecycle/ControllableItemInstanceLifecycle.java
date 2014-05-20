package org.pavanecce.cmmn.jbpm.lifecycle;

import org.drools.core.process.instance.WorkItem;
import org.kie.api.runtime.process.NodeInstance;
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

	void noteInstantiation();

	void internalTriggerWithoutInstantiation(NodeInstance from, String type, WorkItem wi);

}
