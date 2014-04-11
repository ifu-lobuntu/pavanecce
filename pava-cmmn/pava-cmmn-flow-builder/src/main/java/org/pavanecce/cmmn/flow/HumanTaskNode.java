package org.pavanecce.cmmn.flow;

import java.util.HashSet;
import java.util.Set;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.datatype.impl.type.StringDataType;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;

public class HumanTaskNode extends TaskNode {
	private String performerRef;
	private boolean isBlocking;
	public HumanTaskNode() {
		Work work = new WorkImpl();
		work.setName("Human Task");
		Set<ParameterDefinition> parameterDefinitions = new HashSet<ParameterDefinition>();
		parameterDefinitions.add(new ParameterDefinitionImpl("TaskName",
				new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("ActorId",
				new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("Priority",
				new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("Comment",
				new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("Skippable",
				new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("Content",
				new StringDataType()));
		// TODO: initiator
		// TODO: attachments
		// TODO: deadlines
		// TODO: delegates
		// TODO: recipients
		// TODO: ...
		work.setParameterDefinitions(parameterDefinitions);
		setWork(work);
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
}
