package org.pavanecce.cmmn.jbpm.lifecycle;

import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.PlanItemInstanceFactoryNodeInstance;

/**
 * An interface representing all node instances that could have a history state inside it, and reacts to parentResume
 * and parentSuspend to manipulate the history state. An obvious sub-interface is the {@link ControllableItemInstance},
 * but the {@link PlanItemInstanceFactoryNodeInstance} is also required to store the history state
 */
public interface PlanItemInstanceLifecycleWithHistory<T extends PlanItemDefinition> extends PlanItemInstance<T> {
	/**
	 * Exit happens from within the process, when the parent stage resumes. If there is a task associated with this
	 * element, this task needs to be resumed too, triggering the resume
	 */

	void parentSuspend();

	/**
	 * Exit happens from within the process, when the parent stage resumes. If there is a task associated with this
	 * element, this task needs to be resumed too, triggering the resume
	 */
	void parentResume();

	/**
	 * Exit happens from within the process, when exitCriteria become active. If there is a task associated with this
	 * element, this task needs to be exited too, triggering exit when the exist successfully
	 */
	void exit();

	void setLastBusyState(PlanElementState s);

	PlanElementState getLastBusyState();

	boolean isComplexLifecycle();

}
