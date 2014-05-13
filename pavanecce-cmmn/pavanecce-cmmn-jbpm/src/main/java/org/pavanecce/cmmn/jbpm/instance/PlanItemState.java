package org.pavanecce.cmmn.jbpm.instance;

import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.jbpm.workflow.instance.NodeInstance;
import org.pavanecce.cmmn.jbpm.event.PlanItemEvent;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemControl;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;

import static org.pavanecce.cmmn.jbpm.flow.PlanItemTransition.*;

public enum PlanItemState {
	AVAILABLE() {
		@Override
		public PlanItemTransition[] getSupportedTransitions(PlanItemInstance pii) {
			if (pii instanceof CaseInstance) {
				return new PlanItemTransition[0];
			} else if (isMilestoneOrEvent(pii)) {
				return new PlanItemTransition[] { SUSPEND, TERMINATE, PARENT_SUSPEND, PARENT_TERMINATE, OCCUR };
			} else {
				return new PlanItemTransition[] { ENABLE, START, SUSPEND, PARENT_SUSPEND, PARENT_TERMINATE, EXIT };
			}
		}

		public boolean isBusyState() {
			return true;
		}
	},
	SUSPENDED() {
		public PlanItemTransition[] getSupportedTransitions(PlanItemInstance pii) {
			if (pii instanceof CaseInstance) {
				return new PlanItemTransition[] { REACTIVATE, CLOSE };
			} else if (isMilestoneOrEvent(pii)) {
				return new PlanItemTransition[] { TERMINATE, PARENT_TERMINATE, OCCUR };
			} else {
				return new PlanItemTransition[] { PARENT_RESUME, RESUME, EXIT };
			}
		}
	},
	COMPLETED() {
		public PlanItemTransition[] getSupportedTransitions(PlanItemInstance pii) {
			if (pii instanceof CaseInstance) {
				return new PlanItemTransition[] { REACTIVATE, CLOSE };
			} else if (isMilestoneOrEvent(pii)) {
				return new PlanItemTransition[] {};
			} else {
				return new PlanItemTransition[] {};
			}
		}
	},
	TERMINATED() {
		public PlanItemTransition[] getSupportedTransitions(PlanItemInstance pii) {
			if (pii instanceof CaseInstance) {
				return new PlanItemTransition[] { REACTIVATE, CLOSE };
			} else if (isMilestoneOrEvent(pii)) {
				return new PlanItemTransition[] {};
			} else {
				return new PlanItemTransition[] {};
			}
		}
	},
	CLOSED() {
		public PlanItemTransition[] getSupportedTransitions(PlanItemInstance pii) {
			return new PlanItemTransition[] {};
		}
	},
	FAILED() {
		public PlanItemTransition[] getSupportedTransitions(PlanItemInstance pii) {
			if (pii instanceof CaseInstance) {
				return new PlanItemTransition[] { REACTIVATE, CLOSE };
			} else if (isMilestoneOrEvent(pii)) {
				return new PlanItemTransition[] {};
			} else {
				return new PlanItemTransition[] { REACTIVATE, EXIT };
			}
		}
	},
	ACTIVE() {
		public PlanItemTransition[] getSupportedTransitions(PlanItemInstance pii) {
			if (pii instanceof CaseInstance) {
				return new PlanItemTransition[] { COMPLETE, TERMINATE, FAULT, SUSPEND };
			} else if (isMilestoneOrEvent(pii)) {
				return new PlanItemTransition[] {};
			} else {
				return new PlanItemTransition[] { SUSPEND, PARENT_SUSPEND, EXIT, TERMINATE, COMPLETE, FAULT };
			}
		}

		@Override
		public boolean isBusyState() {
			return true;
		}
	},
	ENABLED() {
		public PlanItemTransition[] getSupportedTransitions(PlanItemInstance pii) {
			if (pii instanceof CaseInstance) {
				return new PlanItemTransition[] {};
			} else if (isMilestoneOrEvent(pii)) {
				return new PlanItemTransition[] {};
			} else {
				return new PlanItemTransition[] { DISABLE, MANUAL_START, PARENT_SUSPEND, TERMINATE, EXIT };
			}
		}

		@Override
		public boolean isBusyState() {
			return true;
		}
	},
	DISABLED {
		public PlanItemTransition[] getSupportedTransitions(PlanItemInstance pii) {
			if (pii instanceof CaseInstance) {
				return new PlanItemTransition[] {};
			} else if (isMilestoneOrEvent(pii)) {
				return new PlanItemTransition[] {};
			} else {
				return new PlanItemTransition[] { REENABLE, PARENT_SUSPEND, EXIT };
			}
		}

		@Override
		public boolean isBusyState() {
			return true;
		}
	},
	NONE;
	private void validateTransition(PlanItemInstance pi, PlanItemTransition t) {
		for (PlanItemTransition pit : getSupportedTransitions(pi)) {
			if (t == pit) {
				return;
			}
		}
		throw new IllegalPlanItemStateException(this, t);
	}

	public boolean isBusyState() {
		return false;
	}

	public void enable(PlanItemInstance pi) {
		validateTransition(pi, ENABLE);
		signalEvent(pi, PlanItemTransition.ENABLE);
		pi.setPlanItemState(ENABLED);
	}

	private void signalEvent(PlanItemInstance pi, PlanItemTransition transition) {
		String eventToTrigger = OnPart.getType(pi.getPlanItemName(), transition);
		Object eventObject = null;
		if (pi instanceof HumanControlledPlanItemInstance) {
			eventObject = ((HumanControlledPlanItemInstance) pi).getTask();
		}
		if(eventObject==null){
			eventObject=new Object();
		}
		PlanItemEvent event = new PlanItemEvent(pi.getPlanItemName(), transition, eventObject);
		pi.getCaseInstance().signalEvent(eventToTrigger, event);
	}

	public void disable(PlanItemInstance pi) {
		validateTransition(pi, DISABLE);
		signalEvent(pi, PlanItemTransition.DISABLE);
		pi.setPlanItemState(DISABLED);
	}

	public void reenable(PlanItemInstance pi) {
		validateTransition(pi, REENABLE);
		signalEvent(pi, PlanItemTransition.REENABLE);
		pi.setPlanItemState(ENABLED);
	}

	public void start(PlanItemInstance pi) {
		validateTransition(pi, START);
		signalEvent(pi, PlanItemTransition.START);
		pi.setPlanItemState(ACTIVE);
	}

	public void manualStart(PlanItemInstance pi) {
		validateTransition(pi, MANUAL_START);
		signalEvent(pi, PlanItemTransition.MANUAL_START);
		pi.setPlanItemState(ACTIVE);
	}

	public void reactivate(PlanItemInstance pi) {
		validateTransition(pi, REACTIVATE);
		signalEvent(pi, PlanItemTransition.REACTIVATE);
		pi.setPlanItemState(ACTIVE);
	}

	public void suspend(PlanItemInstance pi) {
		validateTransition(pi, SUSPEND);
		signalEvent(pi, PlanItemTransition.SUSPEND);
		pi.setPlanItemState(SUSPENDED);
		if (pi instanceof PlanItemInstanceContainer) {
			for (PlanItemInstance child : ((PlanItemInstanceContainer) pi).getChildren()) {
				child.parentSuspend();
			}
		}
	}

	public void resume(PlanItemInstance pi) {
		validateTransition(pi, RESUME);
		signalEvent(pi, PlanItemTransition.RESUME);
		pi.setPlanItemState(ACTIVE);
		if (pi instanceof PlanItemInstanceContainer) {
			for (PlanItemInstance child : ((PlanItemInstanceContainer) pi).getChildren()) {
				child.parentResume();
			}
		}
	}

	public void terminate(PlanItemInstance pi) {
		validateTransition(pi, TERMINATE);
		signalEvent(pi, PlanItemTransition.TERMINATE);
		pi.setPlanItemState(TERMINATED);
		if (pi instanceof PlanItemInstanceContainer) {
			for (PlanItemInstance child : ((PlanItemInstanceContainer) pi).getChildren()) {
				if (isMilestoneOrEvent(child)) {
					child.parentTerminate();
				}
			}
		}
	}

	public void exit(PlanItemInstance pi) {
		validateTransition(pi, EXIT);
		signalEvent(pi, PlanItemTransition.EXIT);
		pi.setPlanItemState(TERMINATED);
		PlanItemInstanceUtil.exitPlanItem(pi);
	}

	public void complete(PlanItemInstance pi) {
		validateTransition(pi, COMPLETE);
		signalEvent(pi, PlanItemTransition.COMPLETE);
		pi.setPlanItemState(COMPLETED);
		pi.getCaseInstance().markSubscriptionsForUpdate();
	}

	public void parentSuspend(PlanItemInstance pi) {
		validateTransition(pi, PARENT_SUSPEND);
		signalEvent(pi, PlanItemTransition.PARENT_SUSPEND);
		pi.setLastBusyState(pi.getPlanItemState());
		pi.setPlanItemState(SUSPENDED);
	}

	public void parentResume(PlanItemInstance pi) {
		validateTransition(pi, PARENT_RESUME);
		signalEvent(pi, PlanItemTransition.PARENT_RESUME);
		pi.setPlanItemState(pi.getLastBusyState());
	}

	public void parentTerminate(PlanItemInstance pi) {
		validateTransition(pi, PARENT_TERMINATE);
		signalEvent(pi, PlanItemTransition.PARENT_TERMINATE);
		pi.setLastBusyState(pi.getPlanItemState());
		pi.setPlanItemState(TERMINATED);
	}

	public void create(PlanItemInstance pi) {
		validateTransition(pi, CREATE);
		signalEvent(pi, PlanItemTransition.CREATE);
		pi.setPlanItemState(AVAILABLE);
	}

	public void fault(PlanItemInstance pi) {
		validateTransition(pi, FAULT);
		signalEvent(pi, PlanItemTransition.FAULT);
		pi.setPlanItemState(FAILED);
	}

	public void occur(PlanItemInstance pi) {
		validateTransition(pi, OCCUR);
		signalEvent(pi, PlanItemTransition.OCCUR);
		pi.setPlanItemState(COMPLETED);
	}

	public void close(PlanItemInstance pi) {
		validateTransition(pi, CLOSE);
		signalEvent(pi, PlanItemTransition.CLOSE);
		pi.setPlanItemState(CLOSED);
	}

	public PlanItemTransition[] getSupportedTransitions(PlanItemInstance pii) {
		return new PlanItemTransition[0];
	}

	private static boolean isMilestoneOrEvent(PlanItemInstance pi) {
		return pi instanceof MilestonePlanItemInstance || pi instanceof UserEventPlanItemInstance || pi instanceof TimerEventPlanItemInstance;
	}
}
