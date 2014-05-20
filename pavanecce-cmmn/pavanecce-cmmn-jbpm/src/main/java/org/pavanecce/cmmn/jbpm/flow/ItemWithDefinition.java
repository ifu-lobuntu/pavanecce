package org.pavanecce.cmmn.jbpm.flow;

public interface ItemWithDefinition <T extends PlanItemDefinition> extends CMMNElement {
	T getDefinition();
	PlanItemControl getItemControl();
	String getName();
}
