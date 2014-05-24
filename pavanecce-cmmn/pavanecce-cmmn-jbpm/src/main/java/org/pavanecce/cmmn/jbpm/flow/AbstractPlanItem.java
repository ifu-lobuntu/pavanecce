package org.pavanecce.cmmn.jbpm.flow;

import java.util.List;

import org.kie.api.definition.process.Connection;

public class AbstractPlanItem<T extends PlanItemDefinition> extends AbstractItem implements PlanItem<T> {

	private static final long serialVersionUID = -528614791490955918L;
	private String elementId;
	private PlanItemInfo<T> planInfo;
	private PlanItemContainer planItemContainer;
	private String description;

	public AbstractPlanItem() {
		super();
	}

	public AbstractPlanItem(PlanItemInfo<T> planInfo) {
		super();
		this.planInfo = planInfo;
	}

	@Override
	public final Connection getFrom() {
		final List<Connection> list = getIncomingConnections(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE);
		if (list.size() == 1) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public final void validateAddIncomingConnection(final String type, final Connection connection) {
		if (type == null) {
			throw new IllegalArgumentException("Connection type cannot be null");
		}
		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}
		if (!org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE.equals(type)) {
			throw new IllegalArgumentException("This type of node only accepts default incoming connection type!");
		}

	}

	@Override
	public final T getDefinition() {
		return planInfo.getDefinition();
	}

	public final PlanItemContainer getPlanItemContainer() {
		return planItemContainer;
	}

	public final void setPlanItemContainer(PlanItemContainer planItemContainer) {
		this.planItemContainer = planItemContainer;
	}

	@Override
	public final PlanItemControl getItemControl() {
		return planInfo.getItemControl();
	}

	public final String getDescription() {
		return this.description;
	}

	public final void setDescription(String s) {
		this.description = s;
	}

	public final String getElementId() {
		return elementId;
	}

	public final void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public final PlanItemInfo<T> getPlanInfo() {
		return planInfo;
	}

	@Override
	public PlanItemControl getEffectiveItemControl() {
		if (getItemControl() == null) {
			return getDefinition().getDefaultControl();
		} else {
			return getItemControl();
		}
	}

	@Override
	public String getPlanItemEventName() {
		return getEffectiveName();
	}

	@Override
	public String getEffectiveName() {
		return getName();
	}

}
