package org.pavanecce.cmmn.jbpm.lifecycle;

import org.pavanecce.cmmn.jbpm.flow.ItemWithDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;

public interface PlanItemInstanceLifecycle<T extends PlanItemDefinition> extends PlanElementLifecycle {

	void resume();

	void parentTerminate();

	ItemWithDefinition<T> getItem();

}
