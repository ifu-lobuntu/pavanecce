package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collection;

import org.drools.core.process.core.Work;

public interface TaskDefinition extends PlanItemDefinition {
	Collection<CaseParameter> getInputs();

	Collection<CaseParameter> getOutputs();

	void addOutputParameter(CaseParameter cp);

	void addInputParameter(CaseParameter cp);

	Work getWork();

	boolean isBlocking();

}
