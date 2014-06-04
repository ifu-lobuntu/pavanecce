package org.pavanecce.cmmn.jbpm.flow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.datatype.impl.type.StringDataType;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;

public class HumanTask extends AbstractPlanItemDefinition implements TaskDefinition, PlanningTableContainer {
	private String performerRef;
	private Role performer;
	private boolean isBlocking;

	private static final long serialVersionUID = 2502972573721493216L;
	private List<CaseParameter> inputParameters = new ArrayList<CaseParameter>();
	private List<CaseParameter> outputParameters = new ArrayList<CaseParameter>();
	private PlanningTable planningTable;
	private Work work;

	public HumanTask() {
		work = new WorkImpl();
		work.setName("Human Task");
		Set<ParameterDefinition> parameterDefinitions = new HashSet<ParameterDefinition>();
		parameterDefinitions.add(new ParameterDefinitionImpl("TaskName", new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("ActorId", new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("Priority", new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("Comment", new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("Skippable", new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("Content", new StringDataType()));
		// TODO: initiator
		// TODO: attachments
		// TODO: deadlines
		// TODO: delegates
		// TODO: recipients
		// TODO: ...
		work.setParameterDefinitions(parameterDefinitions);

	}

	public void addOutputParameter(CaseParameter cp) {
		this.outputParameters.add(cp);
	}

	public void addInputParameter(CaseParameter cp) {
		this.inputParameters.add(cp);
	}

	@Override
	public List<CaseParameter> getOutputs() {
		return outputParameters;
	}

	@Override
	public List<CaseParameter> getInputs() {
		return inputParameters;
	}

	public String getPerformerRef() {
		return performerRef;
	}

	public void setPerformerRef(String performerRef) {
		this.performerRef = performerRef;
	}

	public boolean isBlocking() {
		return isBlocking;
	}

	public void setBlocking(boolean isBlocking) {
		this.isBlocking = isBlocking;
	}

	public Role getPerformer() {
		return performer;
	}

	public void setPerformer(Role performer) {
		this.performer = performer;
	}

	@Override
	public Work getWork() {
		Work result = work;
		if (getPerformer() != null) {
			result.setParameter(PeopleAssignmentHelper.GROUP_ID, getPerformer().getName());
		}
		return result;
	}

	public void setPlanningTable(PlanningTable item) {
		this.planningTable = item;
	}

	public PlanningTable getPlanningTable() {
		return planningTable;
	}
}
