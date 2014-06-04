package org.pavanecce.cmmn.jbpm.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.datatype.impl.type.StringDataType;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.kie.api.definition.process.Process;

public class CaseTask extends AbstractPlanItemDefinition implements TaskDefinition {
	private static final long serialVersionUID = 7495168121066617656L;
	Map<String, CaseParameter> inputs = new HashMap<String, CaseParameter>();
	Map<String, CaseParameter> outputs = new HashMap<String, CaseParameter>();
	List<ParameterMapping> mappings = new ArrayList<ParameterMapping>();
	List<ParameterMapping> outputMappings = new ArrayList<ParameterMapping>();
	List<ParameterMapping> inputMappings = new ArrayList<ParameterMapping>();
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
		work.setParameterDefinitions(parameterDefinitions);
	}

	public void setWork(Work work) {
		this.work = work;
	}

	@Override
	public Work getWork() {
		Work result = work;
		work.setParameter("NodeName", getName());
		result.setParameter(PeopleAssignmentHelper.GROUP_ID, "Administrators");
		result.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, "Administrator");
		return result;
	}

	@Override
	public Collection<CaseParameter> getInputs() {
		return inputs.values();
	}

	@Override
	public Collection<CaseParameter> getOutputs() {
		return outputs.values();
	}

	@Override
	public void addOutputParameter(CaseParameter cp) {
		this.outputs.put(cp.getElementId(), cp);
	}

	@Override
	public void addInputParameter(CaseParameter cp) {
		this.inputs.put(cp.getElementId(), cp);

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
		for (ParameterMapping pm : this.mappings) {
			CaseParameter sourceParameter = inputs.get(pm.getSourceRef());
			if (sourceParameter != null) {
				pm.setSourceParameter(sourceParameter);
				inputMappings.add(pm);
			} else {
				CaseParameter cp = outputs.get(pm.getTargetRef());
				if (cp == null) {
					throw new IllegalStateException("A parameter mapping  must either map to or from a Case/ProcessTask parameter: " + pm.getTargetRef());
				}
				pm.setTargetParameter(cp);
				outputMappings.add(pm);
			}
		}
	}

	public void setProcessId(String string) {
		this.processId = string;
	}

	public String getProcessId() {
		return processId;
	}

	public List<ParameterMapping> prepareInputMappings(Process process) {
		if (process instanceof Case) {
			for (ParameterMapping pm : inputMappings) {
				CaseParameter cp = ((Case) process).getInputParameter(pm.getTargetParameterId());
				if (cp != null) {
					pm.setTargetParameterName(cp.getName());
				}
			}
		}
		return inputMappings;
	}

	public List<ParameterMapping> prepareOutputMappings(Process process) {
		if (process instanceof Case) {
			for (ParameterMapping pm : outputMappings) {
				CaseParameter cp = ((Case) process).getOutputParameter(pm.getSourceParameterId());
				if (cp != null) {
					pm.setSourceParameterName(cp.getName());
				}
			}
		}
		return outputMappings;
	}

}
