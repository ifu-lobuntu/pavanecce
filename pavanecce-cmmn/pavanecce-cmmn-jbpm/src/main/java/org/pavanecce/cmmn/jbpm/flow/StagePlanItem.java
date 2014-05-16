package org.pavanecce.cmmn.jbpm.flow;

import java.util.Map.Entry;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.workflow.core.node.CompositeNode;
import org.kie.api.definition.process.Node;

public class StagePlanItem extends CompositeNode implements PlanItem <Stage>,MultiInstancePlanItem{
	private static final long serialVersionUID = -4998194330899363230L;
	private String elementId;
	private PlanItemInfo<Stage> info;
	private PlanItemContainer planItemContainer;
	private String description;
	private PlanItemInstanceFactoryNode factoryNode;

	public StagePlanItem(PlanItemInfo<Stage> info, PlanItemInstanceFactoryNode planItemInstanceFactoryNode) {
		this.info = info;
		this.factoryNode=planItemInstanceFactoryNode;
	}
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String s){
		this.description=s;
	}

	public Stage getStage() {
		return (Stage) info.getDefinition();
	}

	@Override
	public PlanItemInfo<Stage> getPlanInfo() {
		return info;
	}

	@Override
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public String getElementId() {
		return elementId;
	}

	@Override
	public Node[] getNodes() {
		return getStage().getNodes();
	}

	@Override
	public Node getNode(long id) {
		return getStage().getNode(id);
	}

	public Work getWork() {
		WorkImpl work = new WorkImpl();
		Work sourceWork = getStage().getWork();
		work.setName(sourceWork.getName());
		for (ParameterDefinition pd : sourceWork.getParameterDefinitions()) {
			work.addParameterDefinition(new ParameterDefinitionImpl(pd.getName(), pd.getType()));
		}
		for (Entry<String, Object> entry : sourceWork.getParameters().entrySet()) {
			work.setParameter(entry.getKey(), entry.getValue());
		}
		work.setParameter("NodeName", getName());
		return work;
	}

	public PlanItemContainer getPlanItemContainer() {
		return planItemContainer;
	}

	public void setPlanItemContainer(PlanItemContainer planItemContainer) {
		this.planItemContainer = planItemContainer;
	}
	@Override
	public PlanItemInstanceFactoryNode getFactoryNode() {
		return factoryNode;
	}
}
