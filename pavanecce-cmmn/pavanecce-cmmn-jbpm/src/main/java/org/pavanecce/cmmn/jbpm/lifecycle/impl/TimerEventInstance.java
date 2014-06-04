package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Calendar;
import java.util.Date;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.spi.ProcessContext;
import org.jbpm.process.core.timer.BusinessCalendar;
import org.jbpm.process.core.timer.DateTimeUtils;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.impl.ReturnValueConstraintEvaluator;
import org.jbpm.process.instance.impl.ReturnValueEvaluator;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.node.TimerNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.TimerEvent;
import org.pavanecce.cmmn.jbpm.flow.TimerEventPlanItem;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerEventInstance extends OccurrablePlanItemInstanceImpl<TimerEvent, TimerEventPlanItem> implements Creatable {

	private static final long serialVersionUID = 3034509023L;

	private static final Logger logger = LoggerFactory.getLogger(TimerNodeInstance.class);

	private long timerInstanceId;
	private TimerInstance timerInstance;

	public TimerEventInstance() {
		super.internalSetCompletionRequired(false);
		planElementState = PlanElementState.INITIAL;
	}

	public void signalEvent(String type, Object event) {
		if ("timerTriggered".equals(type)) {
			TimerInstance timer = (TimerInstance) event;
			if (timer.getId() == getTimerInstanceId() && canOccur()) {
				setPlanElementState(PlanElementState.AVAILABLE);
				occur();
			}
		}
	}

	@Override
	public void ensureCreationIsTriggered() {
		if (super.getPlanElementState() == PlanElementState.INITIAL) {
			super.create();
		}
	}

	public long getTimerInstanceId() {
		return timerInstanceId;
	}

	public void triggerCompleted() {
		((org.jbpm.workflow.instance.NodeInstanceContainer) getNodeInstanceContainer()).setCurrentLevel(getLevel());
		triggerCompleted(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE, false);
	}

	public void internalSetTimerInstanceId(long timerId) {
		this.timerInstanceId = timerId;
	}

	public void internalTrigger(NodeInstance from, String type) {
		if (!org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE.equals(type)) {
			throw new IllegalArgumentException("A TimerNode only accepts default incoming connections!");
		}
		InternalKnowledgeRuntime kruntime = getProcessInstance().getKnowledgeRuntime();
		timerInstance = createTimerInstance(kruntime);
		((InternalProcessRuntime) kruntime.getProcessRuntime()).getTimerManager().registerTimer(timerInstance, (ProcessInstance) getProcessInstance());
		timerInstanceId = timerInstance.getId();
	}

	protected TimerInstance createTimerInstance(InternalKnowledgeRuntime kruntime) {
		Constraint te = getPlanItem().getPlanInfo().getDefinition().getTimerExpression();
		String expression = te.getConstraint();
		TimerInstance timerInstance = new TimerInstance();
		timerInstance.setPeriod(0);
		timerInstance.setDelay(0);
		if (te instanceof ReturnValueConstraintEvaluator) {
			ReturnValueEvaluator evaluator = ((ReturnValueConstraintEvaluator) te).getReturnValueEvaluator();
			ProcessContext ctx = new ProcessContext(getProcessInstance().getKnowledgeRuntime());
			ctx.setProcessInstance(getProcessInstance());
			ctx.setNodeInstance(this);
			Object val = null;
			try {
				val = evaluator.evaluate(ctx);
			} catch (Exception e) {
				logger.error("Could not evaluate timer expression", e);
				// now what?
			}
			if (val instanceof Calendar) {
				Date date = ((Calendar) val).getTime();
				timerInstance.setDelay(date.getTime() - System.currentTimeMillis());
			} else if (val instanceof Date) {
				timerInstance.setDelay(((Date) val).getTime() - System.currentTimeMillis());
			} else if (val instanceof Number) {
				timerInstance.setDelay(((Number) val).longValue() - System.currentTimeMillis());
			} else if (val instanceof String) {
				expression = (String) val;
			}
		}
		if (timerInstance.getDelay() == 0 && expression != null) {
			if (DateTimeUtils.isPeriod(expression)) {
				long dur = DateTimeUtils.parseDuration(expression);
				timerInstance.setDelay(dur);
			} else if (DateTimeUtils.isRepeatable(expression)) {
				long[] dur = DateTimeUtils.parseRepeatableDateTime(expression);
				timerInstance.setRepeatLimit((int) dur[0]);
				timerInstance.setDelay(dur[1]);
				timerInstance.setPeriod(dur[2]);
			} else if (kruntime != null && kruntime.getEnvironment().get("jbpm.business.calendar") != null) {
				BusinessCalendar businessCalendar = (BusinessCalendar) kruntime.getEnvironment().get("jbpm.business.calendar");
				timerInstance.setDelay(businessCalendar.calculateBusinessTimeAsDuration(expression));
			} else {
				// MVEL
				// now what?
			}
		}
		timerInstance.setTimerId(getPlanItem().getId());
		return timerInstance;
	}

	public String[] getEventTypes() {
		return new String[] { "timerTriggered" };
	}

	public void cancel() {
		((InternalProcessRuntime) getProcessInstance().getKnowledgeRuntime().getProcessRuntime()).getTimerManager().cancelTimer(timerInstanceId);
		super.cancel();
	}

	public void addEventListeners() {
		super.addEventListeners();
		addTimerListener();
	}

	public void removeEventListeners() {
		super.removeEventListeners();
		((WorkflowProcessInstance) getProcessInstance()).removeEventListener("timerTriggered", this, false);
	}

	public TimerInstance getTimerInstance() {
		return timerInstance;
	}

}
