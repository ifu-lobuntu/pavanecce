package org.pavanecce.cmmn.jbpm.instance;

public abstract class CaseEvent {
	public abstract Enum<?> getTransition();
	public abstract Object getValue();
}
