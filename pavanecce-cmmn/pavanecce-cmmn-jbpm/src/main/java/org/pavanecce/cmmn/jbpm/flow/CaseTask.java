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

public class CaseTask extends AbstractPlanItemDefinition implements TaskDefinition {
	private static final long serialVersionUID = 7495168121066617656L;
	List<CaseParameter> inputs = new ArrayList<CaseParameter>();
	List<CaseParameter> outputs = new ArrayList<CaseParameter>();
	List<ParameterMapping> mappings = new ArrayList<ParameterMapping>();
	String elementId;
	private boolean blocking;
	private Work work;
	private String processId;

	public CaseTask() {
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

	public void setWork(Work work) {
		this.work = work;
	}

	@Override
	public Work getWork() {
		Work result = work;
		// Think about this - case owner??
		result.setParameter(PeopleAssignmentHelper.GROUP_ID, "Administrators");
		result.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, "Administrator");

		return result;
	}

	@Override
	public List<CaseParameter> getInputs() {
		return inputs;
	}

	@Override
	public List<CaseParameter> getOutputs() {
		return outputs;
	}

	@Override
	public void addOutputParameter(CaseParameter cp) {
		this.outputs.add(cp);
	}

	@Override
	public void addInputParameter(CaseParameter cp) {
		this.inputs.add(cp);

	}

	public void setBlocking(boolean b) {
		this.blocking = b;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void addParameterMapping(ParameterMapping cp) {
		mappings.add(cp);

	}

	public List<ParameterMapping> getParameterMappings() {
		return mappings;
	}

	public void mapParameters() {
		for (ParameterMapping parameterMapping : this.mappings) {
			if (!findMapping(this.inputs, parameterMapping)) {
				findMapping(inputs, parameterMapping);
			}
		}
	}

	private boolean findMapping(List<CaseParameter> inputs2, ParameterMapping parameterMapping) {
		for (CaseParameter caseParameter : inputs2) {
			if (parameterMapping.getSourceRef().equals(caseParameter.getElementId())) {
				parameterMapping.setSourceParameter(caseParameter);
				return true;
			}
		}
		return false;
	}

	public void setProcessId(String string) {
		this.processId=string;
	}
	public String getProcessId() {
		return processId;
	}

}
