package org.pavanecce.cmmn.jbpm.flow;


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

}
