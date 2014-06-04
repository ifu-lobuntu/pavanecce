package org.pavanecce.cmmn.jbpm.flow;

import org.drools.core.process.core.Work;

public interface TaskItemWithDefinition<T extends PlanItemDefinition> extends ItemWithDefinition<T> {
	Work getWork();

}
