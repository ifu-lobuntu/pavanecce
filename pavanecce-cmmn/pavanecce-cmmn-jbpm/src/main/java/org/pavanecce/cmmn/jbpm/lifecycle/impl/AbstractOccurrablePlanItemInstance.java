package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import org.pavanecce.cmmn.jbpm.flow.ItemWithDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemControl;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.OccurrablePlanItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;

public abstract class AbstractOccurrablePlanItemInstance<T extends PlanItemDefinition ,X extends ItemWithDefinition<T>> extends AbstractItemInstance<T,X> implements OccurrablePlanItemInstanceLifecycle<T> {

	private static final long serialVersionUID = -3451322686745364562L;

	public AbstractOccurrablePlanItemInstance() {
		super();
	}

	@Override
	public void internalSetRequired(boolean readBoolean) {
		this.isCompletionRequired = readBoolean;
	}

	@Override
	public T getPlanItemDefinition() {
		return getPlanItem().getPlanInfo().getDefinition();
	}

	@Override
	public String getItemName() {
		return getPlanItem().getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public PlanItem<T> getPlanItem() {
		return (PlanItem<T>) getNode();
	}

	@Override
	public PlanItemControl getItemControl() {
		if (getPlanItem().getPlanInfo().getItemControl() != null) {
			return getPlanItem().getPlanInfo().getItemControl();
		} else {
			return getPlanItem().getPlanInfo().getDefinition().getDefaultControl();
		}
	}

	public boolean canOccur() {
		return getPlanElementState() == PlanElementState.AVAILABLE || (getPlanElementState() == PlanElementState.COMPLETED);
	}

	public void internalSetRequired(Boolean isPlanItemInstanceRequired) {
		this.isCompletionRequired = isPlanItemInstanceRequired;
	}

	@Override
	public void create() {
		planElementState.create(this);
	}

	@Override
	public void occur() {
		planElementState.occur(this);
	}

	@Override
	public void parentTerminate() {
		planElementState.parentTerminate(this);

	}

}