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
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.kie.api.definition.process.Node;

public class Stage extends AbstractPlanItemDefinition implements PlanItemContainer {

	private static final long serialVersionUID = 3123425777169912160L;
	private boolean autoComplete;
	private Collection<PlanItemInfo<?>> planItemInfo = new ArrayList<PlanItemInfo<?>>();
	private StartNode defaultStart;
	private DefaultSplit defaultSplit;
	private EndNode defaultEnd;
	private DefaultJoin defaultJoin;

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
	public Node superGetNode(long id) {
		return super.getNode(id);
	}

	@Override
	public StartNode getDefaultStart() {
		return defaultStart;
	}

	@Override
	public Node getNode(long id) {
		Node node = super.getNode(id);
		if (node == null && getPlanningTable() != null) {
			node = getPlanningTable().getNode(id);
			if (node == null) {
				for (PlanItemInfo<?> pii : getPlanItemInfo()) {
					if (pii.getDefinition() instanceof HumanTask) {
						HumanTask ht = (HumanTask) pii.getDefinition();
						if (ht.getPlanningTable() != null) {
							node = ht.getPlanningTable().getNode(id);
							if (node != null) {
								break;
							}
						}
					}
				}
			}
		}
		return node;
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

	public void setPlanningTable(PlanningTable planningTable) {
		this.planningTable = planningTable;
		planningTable.setPlanItemContainer(this);
	}

	public PlanningTable getPlanningTable() {
		return planningTable;
	}

}
