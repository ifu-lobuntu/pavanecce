package org.pavanecce.cmmn.jbpm.event;

public abstract class CaseEvent {
	public abstract Enum<?> getTransition();

	public abstract Object getValue();
}
