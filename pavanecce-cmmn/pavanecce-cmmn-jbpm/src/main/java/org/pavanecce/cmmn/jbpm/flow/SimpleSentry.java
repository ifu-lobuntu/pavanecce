package org.pavanecce.cmmn.jbpm.flow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.node.CatchLinkNode;
import org.kie.api.definition.process.Connection;

public class SimpleSentry extends CatchLinkNode implements Sentry {
	private static final long serialVersionUID = -3568385234236274366L;
	private List<OnPart> onParts = new ArrayList<OnPart>();
	private String elementId;
	private Constraint condition;
	private Set<PlanItem> planItemsExiting = new HashSet<PlanItem>();


	
	@Override
	public void addOnPart(OnPart onPart) {
		this.onParts.add(onPart);
	}

	@Override
	public void setCondition(Constraint condition) {
		this.condition = condition;
	}
	@Override
	public Constraint getCondition() {
		return condition;
	}

	@Override
	public void validateAddIncomingConnection(String type, Connection connection) {
		super.validateAddIncomingConnection(type, connection);
	}

	@Override
	public List<OnPart> getOnParts() {
		return onParts;
	}

	@Override
	public String getElementId() {
		return elementId;
	}

	@Override
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public void addPlanItemExiting(PlanItem planItem) {
		planItemsExiting.add(planItem);
	}

	@Override
	public Set<PlanItem> getPlanItemsExiting() {
		return planItemsExiting;
	}

}
