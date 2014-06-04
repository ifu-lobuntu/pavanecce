package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.kie.api.definition.process.Node;

public class StagePlanItem extends AbstractPlanItem<Stage> implements MultiInstancePlanItem, PlanItemContainer, TaskItemWithDefinition<Stage> {
	private static final long serialVersionUID = -4998194330899363230L;
	private PlanItemInstanceFactoryNode factoryNode;

	private StartNode defaultStart;
	private DefaultSplit defaultSplit;
	private EndNode defaultEnd;
	private DefaultJoin defaultJoin;
	private PlanningTable planningTable;
	private boolean autoComplete;

	public StagePlanItem() {
	}

	public StagePlanItem(PlanItemInfo<Stage> info, PlanItemInstanceFactoryNode planItemInstanceFactoryNode) {
		super(info);
		this.factoryNode = planItemInstanceFactoryNode;
	}

	@Override
	public Node superGetNode(long id) {
		return super.getNode(id);
	}

	public StartNode getDefaultStart() {
		return defaultStart;
	}

	public void setDefaultStart(StartNode defaultStart) {
		this.defaultStart = defaultStart;
	}

	@Override
	public Node getNode(long id) {
		return PlanItemContainerUtil.getNode(this, id);
	}

	public DefaultSplit getDefaultSplit() {
		return defaultSplit;
	}

	public void setDefaultSplit(DefaultSplit defaultSplit) {
		this.defaultSplit = defaultSplit;
	}

	public EndNode getDefaultEnd() {
		return defaultEnd;
	}

	public void setDefaultEnd(EndNode defaultEnd) {
		this.defaultEnd = defaultEnd;
	}

	public DefaultJoin getDefaultJoin() {
		return defaultJoin;
	}

	public void setDefaultJoin(DefaultJoin defaultJoin) {
		this.defaultJoin = defaultJoin;
	}

	public PlanningTable getPlanningTable() {
		return planningTable;
	}

	public void setPlanningTable(PlanningTable planningTable) {
		this.planningTable = planningTable;
	}

	public void copyFromStage() {
		HashMap<Object, Object> copiedState = new HashMap<Object, Object>();
		Stage stage = getDefinition();
		copiedState.put(stage, this);
		copy(copiedState, stage, this);
		this.autoComplete = stage.isAutoComplete();
		this.defaultStart = copy(copiedState, stage.getDefaultStart());
		this.defaultSplit = copy(copiedState, stage.getDefaultSplit());
		this.defaultJoin = copy(copiedState, stage.getDefaultJoin());
		this.defaultEnd = copy(copiedState, stage.getDefaultEnd());
		this.planningTable = copy(copiedState, stage.getPlanningTable());
	}

	public Work getWork() {
		WorkImpl work = new WorkImpl();
		Work sourceWork = getDefinition().getWork();
		work.setName(sourceWork.getName());
		for (ParameterDefinition pd : sourceWork.getParameterDefinitions()) {
			work.addParameterDefinition(new ParameterDefinitionImpl(pd.getName(), pd.getType()));
		}
		for (Entry<String, Object> entry : sourceWork.getParameters().entrySet()) {
			work.setParameter(entry.getKey(), entry.getValue());
		}
		work.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, TableItem.getPlannerRoles(this));
		work.setParameter(PeopleAssignmentHelper.GROUP_ID, TableItem.getPlannerRoles(this));
		work.setParameter("NodeName", getName());
		return work;
	}

	@Override
	public PlanItemInstanceFactoryNode getFactoryNode() {
		return factoryNode;
	}

	@Override
	public void addPlanItemInfo(PlanItemInfo<?> d) {
	}

	@Override
	public Collection<PlanItemInfo<?>> getPlanItemInfo() {
		return this.getDefinition().getPlanItemInfo();
	}

	@Override
	public Case getCase() {
		return this.getDefinition().getCase();
	}

	@Override
	public boolean isAutoComplete() {
		return autoComplete;
	}
}
