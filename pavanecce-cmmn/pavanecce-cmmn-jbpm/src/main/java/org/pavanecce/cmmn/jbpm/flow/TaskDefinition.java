package org.pavanecce.cmmn.jbpm.flow;

import java.util.List;

public interface TaskDefinition extends PlanItemDefinition {
	public List<CaseParameter> getInputs();
	public List<CaseParameter> getOutputs();

}
