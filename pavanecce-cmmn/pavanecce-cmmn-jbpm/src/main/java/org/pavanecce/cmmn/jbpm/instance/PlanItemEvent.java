package org.pavanecce.cmmn.jbpm.instance;

import org.kie.api.task.model.Task;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;

public class PlanItemEvent  extends CaseEvent{
	private String caseFileItemName;
	private PlanItemTransition transition;
	private Task value;
	public PlanItemEvent(String caseFileItemName, PlanItemTransition transition, Task value) {
		super();
		this.caseFileItemName = caseFileItemName;
		this.transition = transition;
		this.value = value;
	}
	public String getCaseFileItemName() {
		return caseFileItemName;
	}
	public PlanItemTransition getTransition() {
		return transition;
	}
	public Task getValue() {
		return value;
	}
	
}
