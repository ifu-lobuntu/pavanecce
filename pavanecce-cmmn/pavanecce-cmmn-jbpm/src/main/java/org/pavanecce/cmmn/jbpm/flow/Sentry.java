package org.pavanecce.cmmn.jbpm.flow;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.node.Join;
import org.kie.api.definition.process.Connection;

public class Sentry extends Join implements CMMNElement {
	private static final long serialVersionUID = -3568385090236274366L;
	private List<OnPart> onParts = new ArrayList<OnPart>();
	private String elementId;
	private Constraint condition;
	private PlanItem<?> planItemExiting;
	private PlanItem<?> planItemEntering;
	private boolean exitsCase;
	public Sentry() {
		setType(TYPE_AND);
	}

	public Constraint getCondition() {
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

	@Override
	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public void setPlanItemExiting(PlanItem<?> planItem) {
		planItemExiting = planItem;
	}

	public PlanItem<?> getPlanItemExiting() {
		return planItemExiting;
	}

	public PlanItem<?> getPlanItemEntering() {
		return planItemEntering;
	}

	public void setPlanItemEntering(PlanItem<?> planItemEntering) {
		this.planItemEntering = planItemEntering;
	}

	public PlanItemContainer getPlanItemContainer() {
		if(planItemEntering!=null){
			return planItemEntering.getPlanItemContainer();
		}
		if(planItemExiting!=null){
			return planItemExiting.getPlanItemContainer();
		}
		return null;
	}

	public boolean isExitsCase() {
		return exitsCase;
	}

	public void setExitsCase(boolean exitsCase) {
		this.exitsCase = exitsCase;
	}

}
