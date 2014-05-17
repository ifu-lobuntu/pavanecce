package org.pavanecce.cmmn.jbpm.instance.impl;

import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.jbpm.workflow.instance.node.StateNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
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

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		calcIsRequired();
		super.internalTrigger(from, type);
	}

	@Override
	protected void triggerCompleted(String type, boolean remove) {
		if (getPlanElementState() == PlanElementState.AVAILABLE || (getPlanElementState() == PlanElementState.COMPLETED && canRepeat())) {
			occur();
			super.triggerCompleted(type, false);
		}
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