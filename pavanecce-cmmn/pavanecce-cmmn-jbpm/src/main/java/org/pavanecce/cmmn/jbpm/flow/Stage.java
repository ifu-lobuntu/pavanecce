package org.pavanecce.cmmn.jbpm.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.datatype.impl.type.StringDataType;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.StateNode;

public class Stage extends StateNode implements PlanItemDefinition, PlanItemContainer {

	private static final long serialVersionUID = 3123425777169912160L;
	private String elementId;
	private boolean autoComplete;
	private Collection<PlanItemInfo<?>> planItemInfo = new ArrayList<PlanItemInfo<?>>();
	private StartNode defaultStart;
	private DefaultSplit defaultSplit;
	private EndNode defaultEnd;
	private DefaultJoin defaultJoin;
	private Case theCase;
	private PlanItemControl defaultControl;
	private PlanningTable planningTable;


	public Work getWork() {
		Work work = new WorkImpl();
		work.setName("Human Task");
		Set<ParameterDefinition> parameterDefinitions = new HashSet<ParameterDefinition>();
		parameterDefinitions.add(new ParameterDefinitionImpl("TaskName", new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("ActorId", new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("Priority", new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("Comment", new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("Skippable", new StringDataType()));
		parameterDefinitions.add(new ParameterDefinitionImpl("Content", new StringDataType()));
		return work;
	}

	@Override
	public Case getCase() {
		return this.theCase;
	}

	public void setCase(Case theCase) {
		this.theCase = theCase;
	}

	@Override
	public StartNode getDefaultStart() {
		return defaultStart;
	}

	@Override
	public void setDefaultStart(StartNode defaultStart) {
		this.defaultStart = defaultStart;
	}

	@Override
	public DefaultSplit getDefaultSplit() {
		return defaultSplit;
	}

	@Override
	public void setDefaultSplit(DefaultSplit defaultSplit) {
		this.defaultSplit = defaultSplit;
	}

	@Override
	public EndNode getDefaultEnd() {
		return defaultEnd;
	}

	@Override
	public void setDefaultEnd(EndNode defaultEnd) {
		this.defaultEnd = defaultEnd;
	}

	@Override
	public DefaultJoin getDefaultJoin() {
		return defaultJoin;
	}

	@Override
	public void setDefaultJoin(DefaultJoin defaultJoin) {
		this.defaultJoin = defaultJoin;
	}

	@Override
	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public void addPlanItemInfo(PlanItemInfo<?> d) {
		planItemInfo.add(d);
	}

	@Override
	public Collection<PlanItemInfo<?>> getPlanItemInfo() {
		return planItemInfo;
	}

	public boolean isAutoComplete() {
		return autoComplete;
	}

	public void setAutoComplete(boolean autoComplete) {
		this.autoComplete = autoComplete;
	}

	public PlanItemControl getDefaultControl() {
		return defaultControl;
	}

	public void setDefaultControl(PlanItemControl defaultControl) {
		this.defaultControl = defaultControl;
	}

	public void setPlanningTable(PlanningTable planningTable) {
		this.planningTable=planningTable;
	}

	public PlanningTable getPlanningTable() {
		return planningTable;
	}

}
