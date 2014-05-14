package org.pavanecce.cmmn.jbpm.instance;

import static org.pavanecce.cmmn.jbpm.flow.PlanItemTransition.*;

import org.pavanecce.cmmn.jbpm.event.PlanItemEvent;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.instance.impl.PlanItemInstanceUtil;

public enum PlanElementState {
	AVAILABLE() {
		@Override
		public PlanItemTransition[] getSupportedTransitions(CaseElementLifecycle pii) {
			if (pii instanceof CaseInstanceLifecycle) {
				return new PlanItemTransition[0];
			} else if (pii instanceof OccurrablePlanItemInstanceLifecycle) {
				return new PlanItemTransition[] { SUSPEND, TERMINATE, PARENT_SUSPEND, PARENT_TERMINATE, OCCUR };
			} else {
				return new PlanItemTransition[] { ENABLE, START, SUSPEND, PARENT_SUSPEND, PARENT_TERMINATE, EXIT };
			}
		}

	},
	SUSPENDED() {
		public PlanItemTransition[] getSupportedTransitions(CaseElementLifecycle pii) {
			if (pii instanceof CaseInstanceLifecycle) {
				return new PlanItemTransition[] { REACTIVATE, CLOSE };
			} else if (pii instanceof OccurrablePlanItemInstanceLifecycle) {
				return new PlanItemTransition[] { TERMINATE, PARENT_TERMINATE, OCCUR };
			} else {
				return new PlanItemTransition[] { PARENT_RESUME, RESUME, EXIT };
			}
		}
	},
	COMPLETED() {
		public PlanItemTransition[] getSupportedTransitions(CaseElementLifecycle pii) {
			if (pii instanceof CaseInstanceLifecycle) {
				return new PlanItemTransition[] { REACTIVATE, CLOSE };
			} else if (pii instanceof OccurrablePlanItemInstanceLifecycle) {
				return new PlanItemTransition[] {};
			} else {
				return new PlanItemTransition[] {};
			}
		}
	},
	TERMINATED() {
		public PlanItemTransition[] getSupportedTransitions(CaseElementLifecycle pii) {
			if (pii instanceof CaseInstanceLifecycle) {
				return new PlanItemTransition[] { REACTIVATE, CLOSE };
			} else if (pii instanceof OccurrablePlanItemInstanceLifecycle) {
				return new PlanItemTransition[] {};
			} else {
				return new PlanItemTransition[] {};
			}
		}
	},
	CLOSED() {
		public PlanItemTransition[] getSupportedTransitions(CaseElementLifecycle pii) {
			return new PlanItemTransition[] {};
		}
	},
	FAILED() {
		public PlanItemTransition[] getSupportedTransitions(CaseElementLifecycle pii) {
			if (pii instanceof CaseInstanceLifecycle) {
				return new PlanItemTransition[] { REACTIVATE, CLOSE };
			} else if (pii instanceof OccurrablePlanItemInstanceLifecycle) {
				return new PlanItemTransition[] {};
			} else {
				return new PlanItemTransition[] { REACTIVATE, EXIT };
			}
		}
	},
	ACTIVE() {
		public PlanItemTransition[] getSupportedTransitions(CaseElementLifecycle pii) {
			if (pii instanceof CaseInstanceLifecycle) {
				return new PlanItemTransition[] { COMPLETE, TERMINATE, FAULT, SUSPEND };
			} else if (pii instanceof OccurrablePlanItemInstanceLifecycle) {
				return new PlanItemTransition[] {};
			} else {
				return new PlanItemTransition[] { SUSPEND, PARENT_SUSPEND, EXIT, TERMINATE, COMPLETE, FAULT };
			}
		}
	},
	ENABLED() {
		public PlanItemTransition[] getSupportedTransitions(CaseElementLifecycle pii) {
			if (pii instanceof CaseInstanceLifecycle) {
				return new PlanItemTransition[] {};
			} else if (pii instanceof OccurrablePlanItemInstanceLifecycle) {
				return new PlanItemTransition[] {};
			} else {
				return new PlanItemTransition[] { DISABLE, MANUAL_START, PARENT_SUSPEND, TERMINATE, EXIT };
			}
		}

	},
	DISABLED {
		public PlanItemTransition[] getSupportedTransitions(CaseElementLifecycle pii) {
			if (pii instanceof CaseInstanceLifecycle) {
				return new PlanItemTransition[] {};
			} else if (pii instanceof OccurrablePlanItemInstanceLifecycle) {
				return new PlanItemTransition[] {};
			} else {
				return new PlanItemTransition[] { REENABLE, PARENT_SUSPEND, EXIT };
			}
		}
	},
	NONE;
	private void validateTransition(CaseElementLifecycle pi, PlanItemTransition t) {
		for (PlanItemTransition pit : getSupportedTransitions(pi)) {
			if (t == pit) {
				return;
			}
		}
		throw new IllegalPlanItemStateException(this, t);
	}

	public final boolean isBusyState() {
		return this == ACTIVE || this == DISABLED || this == DISABLED || this == AVAILABLE;
	}

	public void enable(ControllablePlanItemInstanceLifecycle<?> pi) {
		validateTransition(pi, ENABLE);
		signalEvent(pi, PlanItemTransition.ENABLE);
		pi.setPlanElementState(ENABLED);
	}

	private void signalEvent(PlanItemInstanceLifecycle pi, PlanItemTransition transition) {
		String eventToTrigger = OnPart.getType(pi.getPlanItemName(), transition);
		Object eventObject = null;
		if (pi instanceof CaseElementLifecycleWithTask) {
			eventObject = ((CaseElementLifecycleWithTask) pi).getTask();
		}
		if (eventObject == null) {
			eventObject = new Object();
		}
		PlanItemEvent event = new PlanItemEvent(pi.getPlanItemName(), transition, eventObject);
		pi.getCaseInstance().signalEvent(eventToTrigger, event);
	}

	public void disable(ControllablePlanItemInstanceLifecycle<?> pi) {
		validateTransition(pi, DISABLE);
		signalEvent(pi, PlanItemTransition.DISABLE);
		pi.setPlanElementState(DISABLED);
	}

	public void reenable(ControllablePlanItemInstanceLifecycle<?> pi) {
		validateTransition(pi, REENABLE);
		signalEvent(pi, PlanItemTransition.REENABLE);
		pi.setPlanElementState(ENABLED);
	}

	public void start(ControllablePlanItemInstanceLifecycle<?> pi) {
		validateTransition(pi, START);
		signalEvent(pi, PlanItemTransition.START);
		pi.setPlanElementState(ACTIVE);
	}

	public void manualStart(ControllablePlanItemInstanceLifecycle<?> pi) {
		validateTransition(pi, MANUAL_START);
		signalEvent(pi, PlanItemTransition.MANUAL_START);
		pi.setPlanElementState(ACTIVE);
	}

	public void reactivate(CaseElementLifecycle pi) {
		validateTransition(pi, REACTIVATE);
		if (pi instanceof PlanItemInstanceLifecycle) {
			signalEvent((PlanItemInstanceLifecycle) pi, PlanItemTransition.REACTIVATE);
		}
		pi.setPlanElementState(ACTIVE);
	}

	public void suspend(CaseElementLifecycle pi) {
		validateTransition(pi, SUSPEND);
		if (pi instanceof PlanItemInstanceLifecycle) {
			signalEvent((PlanItemInstanceLifecycle) pi, PlanItemTransition.SUSPEND);
		}
		pi.setPlanElementState(SUSPENDED);
		if (pi instanceof PlanItemInstanceContainer) {
			for (PlanItemInstanceLifecycle child : ((PlanItemInstanceContainer) pi).getChildren()) {
				if (child.getPlanElementState().isBusyState()) {
					if (child instanceof OccurrablePlanItemInstanceLifecycle && child.getPlanElementState() == AVAILABLE) {
						((OccurrablePlanItemInstanceLifecycle) child).resume();
					} else if (child instanceof ControllablePlanItemInstanceLifecycle) {
						((ControllablePlanItemInstanceLifecycle<?>) child).parentSuspend();
					}
				}
			}
		}
	}

	public void resume(PlanItemInstanceLifecycle pi) {
		validateTransition(pi, RESUME);
		signalEvent(pi, PlanItemTransition.RESUME);
		if (pi instanceof ControllablePlanItemInstanceLifecycle) {
			pi.setPlanElementState(ACTIVE);
		} else {
			pi.setPlanElementState(AVAILABLE);
		}
		if (pi instanceof PlanItemInstanceContainer) {
			for (PlanItemInstanceLifecycle child : ((PlanItemInstanceContainer) pi).getChildren()) {
				if (child.getPlanElementState() == SUSPENDED) {
					if (child instanceof ControllablePlanItemInstanceLifecycle) {
						((ControllablePlanItemInstanceLifecycle<?>) child).parentResume();
					} else if (child instanceof OccurrablePlanItemInstanceLifecycle) {
						((OccurrablePlanItemInstanceLifecycle) child).resume();
					}
				}
			}
		}
	}

	public void terminate(CaseElementLifecycle pi) {
		validateTransition(pi, TERMINATE);
		if (pi instanceof PlanItemInstanceLifecycle) {
			signalEvent((PlanItemInstanceLifecycle) pi, PlanItemTransition.TERMINATE);
		}
		pi.setPlanElementState(TERMINATED);
		if (pi instanceof PlanItemInstanceContainer) {
			for (PlanItemInstanceLifecycle child : ((PlanItemInstanceContainer) pi).getChildren()) {
				if(!child.getPlanElementState().isTerminalState()){
					if(child instanceof OccurrablePlanItemInstanceLifecycle){
						((OccurrablePlanItemInstanceLifecycle) child).parentTerminate();
					}else if(child instanceof ControllablePlanItemInstanceLifecycle){
						((ControllablePlanItemInstanceLifecycle<?>) child).exit();
					}
				}
			}
		}
	}

	public void exit(ControllablePlanItemInstanceLifecycle<?> pi) {
		validateTransition(pi, EXIT);
		signalEvent(pi, PlanItemTransition.EXIT);
		pi.setPlanElementState(TERMINATED);
		PlanItemInstanceUtil.exitPlanItem(pi);
	}

	public void complete(CaseElementLifecycle pi) {
		validateTransition(pi, COMPLETE);
		if (pi instanceof PlanItemInstanceLifecycle) {
			signalEvent((PlanItemInstanceLifecycle) pi, PlanItemTransition.COMPLETE);
		}
		pi.setPlanElementState(COMPLETED);
		pi.getCaseInstance().markSubscriptionsForUpdate();
	}

	public void parentSuspend(ControllablePlanItemInstanceLifecycle<?> pi) {
		validateTransition(pi, PARENT_SUSPEND);
		signalEvent(pi, PlanItemTransition.PARENT_SUSPEND);
		pi.setLastBusyState(pi.getPlanElementState());
		pi.setPlanElementState(SUSPENDED);
	}

	public void parentResume(ControllablePlanItemInstanceLifecycle<?> pi) {
		validateTransition(pi, PARENT_RESUME);
		signalEvent(pi, PlanItemTransition.PARENT_RESUME);
		pi.setPlanElementState(pi.getLastBusyState());
	}

	public void parentTerminate(OccurrablePlanItemInstanceLifecycle pi) {
		validateTransition(pi, PARENT_TERMINATE);
		signalEvent(pi, PlanItemTransition.PARENT_TERMINATE);
		pi.setPlanElementState(TERMINATED);
	}

	public void create(CaseElementLifecycle pi) {
		validateTransition(pi, CREATE);
		if (pi instanceof PlanItemInstanceLifecycle) {
			signalEvent((PlanItemInstanceLifecycle) pi, PlanItemTransition.CREATE);
		}
		pi.setPlanElementState(AVAILABLE);
	}

	public void fault(CaseElementLifecycle pi) {
		validateTransition(pi, FAULT);
		if (pi instanceof PlanItemInstanceLifecycle) {
			signalEvent((PlanItemInstanceLifecycle) pi, PlanItemTransition.FAULT);
		}
		pi.setPlanElementState(FAILED);
	}

	public void occur(OccurrablePlanItemInstanceLifecycle pi) {
		validateTransition(pi, OCCUR);
		signalEvent(pi, PlanItemTransition.OCCUR);
		pi.setPlanElementState(COMPLETED);
	}

	public void close(CaseInstanceLifecycle pi) {
		validateTransition(pi, CLOSE);
		if (pi instanceof PlanItemInstanceLifecycle) {
			signalEvent((PlanItemInstanceLifecycle) pi, PlanItemTransition.CLOSE);
		}
		pi.setPlanElementState(CLOSED);
	}

	public PlanItemTransition[] getSupportedTransitions(CaseElementLifecycle pii) {
		return new PlanItemTransition[0];
	}

	public boolean isTerminalState() {
		return this == CLOSED || this == COMPLETED || this == TERMINATED;
	}

	public boolean isSemiTerminalState() {
		return this == DISABLED || this == FAILED || this == COMPLETED;// the latter only for CaseInstances! TODO
	}

}
