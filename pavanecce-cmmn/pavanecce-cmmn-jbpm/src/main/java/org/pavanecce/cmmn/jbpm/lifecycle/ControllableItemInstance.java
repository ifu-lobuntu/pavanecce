package org.pavanecce.cmmn.jbpm.lifecycle;

import org.drools.core.process.instance.WorkItem;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;

/**
 * This interface represents the lifecycle of all PlanItems whose lifecycle can be controlled by a case owner/user. As
 * such, all of its implementing classes would have Task associated with it which can be controlled, to some extent at
 * least, by the task's owner
 * 
 */
public interface ControllableItemInstance<T extends PlanItemDefinition> extends PlanItemInstanceLifecycleWithHistory<T>, PlanElementLifecycleWithTask {

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

	boolean isActivatedManually();

}
