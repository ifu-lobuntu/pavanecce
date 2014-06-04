package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Collections;
import java.util.List;

import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.instance.node.StateNodeInstance;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.DiscretionaryItem;
import org.pavanecce.cmmn.jbpm.flow.ItemWithDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInstanceFactoryNode;
import org.pavanecce.cmmn.jbpm.flow.Stage;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.ControllableItemInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.OccurrablePlanItemInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstanceLifecycleWithHistory;

/**
 * This class represents the lifecycle of controllablePlanInstances prior to instantiation of the PlanItem in question
 * 
 */
public class PlanItemInstanceFactoryNodeInstance<T extends PlanItemDefinition> extends StateNodeInstance implements PlanItemInstanceLifecycleWithHistory<T>,
		Creatable {

	public static class EmulatedPlanItemInstanceFactoryNode extends PlanItemInstanceFactoryNode {
		private static final long serialVersionUID = -7156500421241207274L;
		private final PlanItemInstanceFactoryNode node;

		public EmulatedPlanItemInstanceFactoryNode(PlanItemInstanceFactoryNode node) {
			this.node = node;
		}

		@Override
		public List<Connection> getOutgoingConnections(String type) {
			return getDefaultOutgoingConnections();
		}

		@Override
		public List<Connection> getDefaultOutgoingConnections() {
			Connection conn = new ConnectionImpl(node, CONNECTION_DEFAULT_TYPE, (Node) this.node.getItemToInstantiate(), CONNECTION_DEFAULT_TYPE) {
				private static final long serialVersionUID = 1L;

				@Override
				public void connect() {
				}
			};
			return Collections.singletonList(conn);
		}

	}

	private static final long serialVersionUID = -5291618101988431033L;
	private Boolean isPlanItemInstanceRequired;
	private boolean hasPlanItemBeenInstantiated = false;
	private Boolean isRepeating;
	private boolean isIncludedByDiscretion = false;
	private PlanElementState planElementState = PlanElementState.INITIAL;
	private PlanElementState lastBusyState = PlanElementState.NONE;
	private transient boolean beingTriggered = false;

	public PlanItemInstanceFactoryNodeInstance() {
	}

	public PlanElementState getPlanElementState() {
		return planElementState;
	}

	public void setPlanElementState(PlanElementState planElementState) {
		this.planElementState = planElementState;
	}

	public boolean isDiscretionary() {
		return getItem() instanceof DiscretionaryItem;
	}

	@Override
	public void ensureCreationIsTriggered() {
		if (planElementState == PlanElementState.INITIAL) {
			create();
			setLastBusyState(getPlanElementState());
		}
	}

	@Override
	public boolean isComplexLifecycle() {
		PlanItemDefinition def = getItem().getDefinition();
		return def instanceof TaskDefinition || def instanceof Stage;
	}

	@Override
	public PlanItemInstanceFactoryNode getNode() {
		final PlanItemInstanceFactoryNode node = (PlanItemInstanceFactoryNode) super.getNode();
		if (beingTriggered && node.getItemToInstantiate() instanceof DiscretionaryItem && isIncludedByDiscretion) {
			// Fake an outgoing connectiong
			return new EmulatedPlanItemInstanceFactoryNode(node);
		}
		return node;
	}

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		if (!isInactive() && !isExcludedByDiscretion() && (isRepeating() || !isHasPlanItemBeenInstantiatedYet())) {
			super.internalTrigger(from, type);
			hasPlanItemBeenInstantiated = true;
			beingTriggered = true;
			triggerCompleted(NodeImpl.CONNECTION_DEFAULT_TYPE, false);
			beingTriggered = false;
			setLastBusyState(getPlanElementState());
		}
	}

	private boolean isExcludedByDiscretion() {
		return isDiscretionary() && !isIncludedByDiscretion();
	}

	private boolean isInactive() {
		return planElementState == PlanElementState.SUSPENDED || planElementState == PlanElementState.TERMINATED;
	}

	@Override
	protected void triggerNodeInstance(org.jbpm.workflow.instance.NodeInstance nodeInstance, String type) {
		if (nodeInstance instanceof OccurrablePlanItemInstance) {
			((OccurrablePlanItemInstance<?>) nodeInstance).internalSetRequired(isPlanItemInstanceRequired);
		} else {
			((ControllableItemInstance<?>) nodeInstance).internalSetCompletionRequired(isPlanItemInstanceRequired);
		}
		super.triggerNodeInstance(nodeInstance, type);
	}

	public boolean isPlanItemInstanceRequired() {
		return isPlanItemInstanceRequired;
	}

	public void internalSetPlanItemInstanceRequired(boolean isPlanItemInstanceRequired) {
		this.isPlanItemInstanceRequired = isPlanItemInstanceRequired;
	}

	public boolean isPlanItemInstanceStillRequired() {
		if (getItem() instanceof TaskDefinition && !((TaskDefinition) getItem()).isBlocking()) {
			return false;
		} else if (isExcludedByDiscretion()) {
			return false;
		} else if (hasPlanItemBeenInstantiated) {
			return false;
		} else if (isPlanItemInstanceRequired == null) {
			// still initializing
			return true;
		} else {
			return isPlanItemInstanceRequired;

		}
	}

	public boolean isHasPlanItemBeenInstantiatedYet() {
		return hasPlanItemBeenInstantiated;
	}

	public void internalSetHasPlanItemInstanceBeenInstantiated(boolean val) {
		this.hasPlanItemBeenInstantiated = val;
	}

	public CaseInstance getCaseInstance() {
		return (CaseInstance) getProcessInstance();
	}

	public void suspend() {
		planElementState.suspend(this);
	}

	@Override
	public void create() {
		ItemWithDefinition<T> planItem = getItem();
		PlanItemInstanceFactoryNodeInstance<T> contextNodeInstance = this;
		this.isPlanItemInstanceRequired = ExpressionUtil.isRequired(planItem, contextNodeInstance);
		isRepeating = ExpressionUtil.isRepeating(this, planItem);
		hasPlanItemBeenInstantiated = false;
		planElementState.create(contextNodeInstance);
	}

	public boolean isRepeating() {
		return isRepeating;
	}

	@Override
	public void resume() {
		planElementState.resume(this);

	}

	@Override
	public void terminate() {
		planElementState.terminate(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ItemWithDefinition<T> getItem() {
		return (ItemWithDefinition<T>) getNode().getItemToInstantiate();
	}

	@Override
	public void parentSuspend() {
		planElementState.parentSuspend(this);
	}

	@Override
	public void parentResume() {
		planElementState.parentResume(this);
	}

	@Override
	public void setLastBusyState(PlanElementState s) {
		this.lastBusyState = s;
	}

	@Override
	public PlanElementState getLastBusyState() {
		return this.lastBusyState;
	}

	public void internalSetRepeating(boolean readBoolean) {
		this.isRepeating = readBoolean;
	}

	@Override
	public void parentTerminate() {
		if (isComplexLifecycle()) {
			throw new IllegalStateException("Complex planItemInstances do not suppoer to parentTerminate");
		} else {
			planElementState.parentTerminate(this);
		}
	}

	@Override
	public void exit() {
		if (isComplexLifecycle()) {
			planElementState.exit(this);
		} else {
			throw new IllegalStateException("Occurrable planItemInstances do not suppoer to exit");
		}
	}

	public boolean isIncludedByDiscretion() {
		return isIncludedByDiscretion;
	}

	public void setIncludedByDiscretion(boolean isIncludedByDiscretion) {
		this.isIncludedByDiscretion = isIncludedByDiscretion;
	}

}
