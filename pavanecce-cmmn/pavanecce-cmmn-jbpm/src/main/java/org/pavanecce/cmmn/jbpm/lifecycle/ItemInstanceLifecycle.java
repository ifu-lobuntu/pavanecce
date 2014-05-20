package org.pavanecce.cmmn.jbpm.lifecycle;

import org.pavanecce.cmmn.jbpm.flow.PlanItemControl;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;

public interface ItemInstanceLifecycle<T extends PlanItemDefinition> extends PlanElementLifecycle {

	void resume();

	void parentTerminate();

	String getItemName();

	T getPlanItemDefinition();

	PlanItemControl getItemControl();

}
