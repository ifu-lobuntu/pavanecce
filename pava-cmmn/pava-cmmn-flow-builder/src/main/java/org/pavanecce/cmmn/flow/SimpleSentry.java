package org.pavanecce.cmmn.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.CatchLinkNode;
import org.kie.api.definition.process.Connection;

public class SimpleSentry extends CatchLinkNode implements Sentry {
	private static final long serialVersionUID = -3568385234236274366L;
	private List<OnPart> onParts = new ArrayList<OnPart>();
	private String elementId;
	private Constraint condition;
	private Set<PlanItem> planItemsExiting = new HashSet<PlanItem>();

	@Override
	public Constraint getConstraint(Connection connection) {
		return condition;
	}

	public void addOnPart(OnPart onPart) {
		this.onParts.add(onPart);
	}

	public void setCondition(Constraint condition) {
		this.condition = condition;
	}

	@Override
	public void validateAddIncomingConnection(String type, Connection connection) {
		super.validateAddIncomingConnection(type, connection);
	}

	public List<OnPart> getOnParts() {
		return onParts;
	}

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public void addPlanItemExiting(PlanItem planItem) {
		planItemsExiting.add(planItem);
	}

	public Set<PlanItem> getPlanItemsExiting() {
		return planItemsExiting;
	}

}
