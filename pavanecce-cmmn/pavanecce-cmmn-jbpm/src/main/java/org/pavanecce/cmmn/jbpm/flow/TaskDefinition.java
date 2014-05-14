package org.pavanecce.cmmn.jbpm.flow;

import java.util.List;

import org.drools.core.process.core.Work;

public interface TaskDefinition extends PlanItemDefinition {
	List<CaseParameter> getInputs();

	List<CaseParameter> getOutputs();

	void addOutputParameter(CaseParameter cp);

	void addInputParameter(CaseParameter cp);

	Work getWork();

}
