package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.node.CompositeNode;
import org.kie.api.definition.process.Node;

public class StagePlanItem extends CompositeNode implements PlanItem {
	private static final long serialVersionUID = -4998194330899363230L;
	private String elementId;
	private PlanItemInfo info = new PlanItemInfo();

	public StagePlanItem(PlanItemInfo info) {
		this.info = info;
	}

	protected Stage getStage() {
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
}
