package org.pavanecce.cmmn.jbpm.flow;

import java.util.Map.Entry;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.workflow.core.node.CompositeNode;
import org.kie.api.definition.process.Node;

public class StagePlanItem extends CompositeNode implements PlanItem {
	private static final long serialVersionUID = -4998194330899363230L;
	private String elementId;
	private PlanItemInfo info = new PlanItemInfo();

	public StagePlanItem(PlanItemInfo info) {
		this.info = info;
	}

	public Stage getStage() {
		return (Stage) info.getDefinition();
	}

	@Override
	public PlanItemInfo getPlanInfo() {
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
}