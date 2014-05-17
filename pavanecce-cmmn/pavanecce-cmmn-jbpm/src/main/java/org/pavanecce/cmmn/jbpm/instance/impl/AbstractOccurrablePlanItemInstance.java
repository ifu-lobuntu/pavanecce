package org.pavanecce.cmmn.jbpm.instance.impl;

import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.instance.OccurrablePlanItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;

public abstract class AbstractOccurrablePlanItemInstance<T extends PlanItemDefinition> extends AbstractPlanItemInstance<T> implements OccurrablePlanItemInstanceLifecycle<T> {

	private static final long serialVersionUID = -3451322686745364562L;

	public AbstractOccurrablePlanItemInstance() {
		super();
	}

	@Override
	public void internalSetRequired(boolean readBoolean) {
		this.isCompletionRequired = readBoolean;
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