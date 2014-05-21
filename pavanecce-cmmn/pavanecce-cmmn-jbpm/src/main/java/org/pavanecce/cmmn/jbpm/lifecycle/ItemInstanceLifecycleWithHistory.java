package org.pavanecce.cmmn.jbpm.lifecycle;

import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.PlanItemInstanceFactoryNodeInstance;

/**
 * An interface representing all node instances that could have a history state inside it, and reacts to parentResume
 * and parentSuspend to manipulate the history state. An obvious sub-interface is the
 * {@link ControllableItemInstanceLifecycle}, but the {@link PlanItemInstanceFactoryNodeInstance} is also required to
 * store the history state
 * 
 * @author ampie
 * 
 * @param <T>
 */
public interface ItemInstanceLifecycleWithHistory<T extends PlanItemDefinition> extends ItemInstanceLifecycle<T> {
	void parentSuspend();

	void parentResume();

	void exit();

	void setLastBusyState(PlanElementState s);

	PlanElementState getLastBusyState();

	boolean isComplexLifecycle();

}
