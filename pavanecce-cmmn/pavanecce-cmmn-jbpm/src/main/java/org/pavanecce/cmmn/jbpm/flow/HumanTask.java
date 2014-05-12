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
import org.jbpm.workflow.core.node.WorkItemNode;

public class HumanTask extends WorkItemNode implements TaskDefinition {
	private String performerRef;
	private Role performer;
	private boolean isBlocking;

	private static final long serialVersionUID = 2502972573721493216L;
	private String elementId;
	private List<CaseParameter> inputParameters = new ArrayList<CaseParameter>();
	private List<CaseParameter> outputParameters = new ArrayList<CaseParameter>();
	private PlanItemControl defaultControl;


	public HumanTask() {
		Work work = new WorkImpl();
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
		setWork(work);

	}

	@Override
	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
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
		Work result = super.getWork();
		// Think about this - case owner??
		result.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, getPerformer().getName());

		return result;
	}

	public PlanItemControl getDefaultControl() {
		return defaultControl;
	}

	public void setDefaultControl(PlanItemControl defaultControl) {
		this.defaultControl = defaultControl;
	}

}
