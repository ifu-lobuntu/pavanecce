package org.pavanecce.cmmn.jbpm.infra;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.MarshallerWriteContext;
import org.drools.core.marshalling.impl.PersisterEnums;
import org.jbpm.marshalling.impl.AbstractProcessInstanceMarshaller;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.jbpm.workflow.instance.node.JoinInstance;
import org.jbpm.workflow.instance.node.SubProcessNodeInstance;
import org.jbpm.workflow.instance.node.TimerNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.pavanecce.cmmn.jbpm.instance.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.CaseTaskPlanItemInstance;
import org.pavanecce.cmmn.jbpm.instance.HumanTaskPlanItemInstance;
import org.pavanecce.cmmn.jbpm.instance.MilestonePlanItemInstance;
import org.pavanecce.cmmn.jbpm.instance.OnPartInstance;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstance;
import org.pavanecce.cmmn.jbpm.instance.PlanItemState;
import org.pavanecce.cmmn.jbpm.instance.SentryInstance;
import org.pavanecce.cmmn.jbpm.instance.StagePlanItemInstance;
import org.pavanecce.cmmn.jbpm.instance.TimerEventPlanItemInstance;
import org.pavanecce.cmmn.jbpm.instance.UserEventPlanItemInstance;

public class CaseInstanceMarshaller extends AbstractProcessInstanceMarshaller {
	private static final int SENTRY_INSTANCE = 176;
	private static final int ON_PART_INSTANCE = 177;
	private static final int HUMAN_TASK_PLAN_ITEM_INSTANCE = 178;
	private static final int STAGE_PLAN_ITEM_INSTANCE = 179;
	private static final int USER_EVENT_PLAN_ITEM_INSTANCE = 180;
	private static final int TIMER_EVENT_PLAN_ITEM_INSTANCE = 181;
	private static final int MILESTONE_PLAN_ITEM_INSTANCE = 182;
	private static final int CASE_TASK_PLAN_ITEM_INSTANCE = 183;

	@Override
	protected WorkflowProcessInstanceImpl createProcessInstance() {
		return new CaseInstance();
	}

	@Override
	public Object writeProcessInstance(MarshallerWriteContext context, ProcessInstance processInstance) throws IOException {
		Object result = super.writeProcessInstance(context, processInstance);
		if (processInstance instanceof CaseInstance) {
			context.stream.writeInt(((CaseInstance) processInstance).getPlanItemState().ordinal());
		}
		return result;
	}

	@Override
	public ProcessInstance readProcessInstance(MarshallerReaderContext context) throws IOException {
		ObjectInputStream stream = context.stream;
		ProcessInstance read = super.readProcessInstance(context);
		if (read instanceof CaseInstance) {
			((CaseInstance) read).setPlanItemState(PlanItemState.values()[stream.readInt()]);
		}
		return read;
	}

	private void writePlanItemStates(PlanItemInstance pi, ObjectOutputStream stream) throws IOException {
		stream.writeInt(pi.getPlanItemState().ordinal());
		stream.writeInt(pi.getLastBusyState().ordinal());
	}
	private void readPlanItemStates(PlanItemInstance pi, ObjectInputStream stream) throws IOException {
		pi.setPlanItemState(PlanItemState.values()[stream.readInt()]);
		pi.setLastBusyState(PlanItemState.values()[stream.readInt()]);
	}

	@Override
	protected NodeInstanceImpl readNodeInstanceContent(int nodeType, ObjectInputStream stream, MarshallerReaderContext context, WorkflowProcessInstance processInstance) throws IOException {
		NodeInstanceImpl nodeInstance = null;
		switch (nodeType) {
		case SENTRY_INSTANCE:
			SentryInstance sentryInstance = new SentryInstance();
			sentryInstance.setPlanItemInstanceRequired(context.stream.readBoolean());
            int number = stream.readInt();
            if (number > 0) {
                Map<Long, Integer> triggers = new HashMap<Long, Integer>();
                for (int i = 0; i < number; i++) {
                    long l = stream.readLong();
                    int count = stream.readInt();
                    triggers.put(l,
                            count);
                }
                sentryInstance.internalSetTriggers(triggers);
            }
			nodeInstance = sentryInstance;
			break;
		case ON_PART_INSTANCE:
			OnPartInstance onPartInstance = new OnPartInstance();
			nodeInstance = onPartInstance;
			break;
		case MILESTONE_PLAN_ITEM_INSTANCE:
			nodeInstance = new MilestonePlanItemInstance();
			break;
		case CASE_TASK_PLAN_ITEM_INSTANCE:
			nodeInstance = new CaseTaskPlanItemInstance();
			((CaseTaskPlanItemInstance) nodeInstance).internalSetProcessInstanceId(stream.readLong());
			int nbTimerInstances = stream.readInt();
			if (nbTimerInstances > 0) {
				List<Long> timerInstances = new ArrayList<Long>();
				for (int i = 0; i < nbTimerInstances; i++) {
					timerInstances.add(stream.readLong());
				}
				((CaseTaskPlanItemInstance) nodeInstance).internalSetTimerInstances(timerInstances);
			}
			break;
		case USER_EVENT_PLAN_ITEM_INSTANCE:
			nodeInstance = new UserEventPlanItemInstance();
			break;
		case TIMER_EVENT_PLAN_ITEM_INSTANCE:
			nodeInstance = new TimerEventPlanItemInstance();
			((TimerEventPlanItemInstance) nodeInstance).internalSetTimerId(stream.readLong());
			break;
		case HUMAN_TASK_PLAN_ITEM_INSTANCE:
			HumanTaskPlanItemInstance planItemInstance = new HumanTaskPlanItemInstance();
			readPlanItemStates(planItemInstance, stream);
			planItemInstance.internalSetWorkItemId(stream.readLong());
			nbTimerInstances = stream.readInt();
			if (nbTimerInstances > 0) {
				List<Long> timerInstances = new ArrayList<Long>();
				for (int i = 0; i < nbTimerInstances; i++) {
					timerInstances.add(stream.readLong());
				}
				planItemInstance.internalSetTimerInstances(timerInstances);
			}
			nodeInstance = planItemInstance;
			break;
		case STAGE_PLAN_ITEM_INSTANCE:
			StagePlanItemInstance spii = new StagePlanItemInstance();
			nodeInstance = spii;
			readPlanItemStates(spii, stream);
			spii.internalSetWorkItemId(stream.readLong());
			nbTimerInstances = stream.readInt();
			if (nbTimerInstances > 0) {
				List<Long> timerInstances = new ArrayList<Long>();
				for (int i = 0; i < nbTimerInstances; i++) {
					timerInstances.add(stream.readLong());
				}
				((CompositeContextNodeInstance) nodeInstance).internalSetTimerInstances(timerInstances);
			}
			break;
		default:
			nodeInstance = super.readNodeInstanceContent(nodeType, stream, context, processInstance);
		}
		return nodeInstance;
	}

	@Override
	protected void writeNodeInstanceContent(ObjectOutputStream stream, NodeInstance nodeInstance, MarshallerWriteContext context) throws IOException {
		if (nodeInstance instanceof SentryInstance) {
			stream.writeShort(SENTRY_INSTANCE);
			stream.writeBoolean(((SentryInstance) nodeInstance).isPlanItemInstanceRequired());
            Map<Long, Integer> triggers = ((JoinInstance) nodeInstance).getTriggers();
            stream.writeInt(triggers.size());
            List<Long> keys = new ArrayList<Long>(triggers.keySet());
            Collections.sort(keys,
                    new Comparator<Long>() {

                        public int compare(Long o1,
                                Long o2) {
                            return o1.compareTo(o2);
                        }
                    });
            for (Long key : keys) {
                stream.writeLong(key);
                stream.writeInt(triggers.get(key));
            }

		} else if (nodeInstance instanceof OnPartInstance) {
			stream.writeShort(ON_PART_INSTANCE);
		} else if (nodeInstance instanceof MilestonePlanItemInstance) {
			stream.writeShort(MILESTONE_PLAN_ITEM_INSTANCE);
		} else if (nodeInstance instanceof CaseTaskPlanItemInstance) {
			stream.writeShort(CASE_TASK_PLAN_ITEM_INSTANCE);
			stream.writeLong(((CaseTaskPlanItemInstance) nodeInstance).getProcessInstanceId());
			List<Long> timerInstances = ((CaseTaskPlanItemInstance) nodeInstance).getTimerInstances();
			if (timerInstances != null) {
				stream.writeInt(timerInstances.size());
				for (Long id : timerInstances) {
					stream.writeLong(id);
				}
			} else {
				stream.writeInt(0);
			}
		} else if (nodeInstance instanceof UserEventPlanItemInstance) {
			stream.writeShort(USER_EVENT_PLAN_ITEM_INSTANCE);
		} else if (nodeInstance instanceof TimerEventPlanItemInstance) {
			stream.writeShort(TIMER_EVENT_PLAN_ITEM_INSTANCE);
			stream.writeLong(((TimerNodeInstance) nodeInstance).getTimerId());
		} else if (nodeInstance instanceof HumanTaskPlanItemInstance) {
			stream.writeShort(HUMAN_TASK_PLAN_ITEM_INSTANCE);
			HumanTaskPlanItemInstance hpii = (HumanTaskPlanItemInstance)nodeInstance;
			writePlanItemStates(hpii, stream);
			stream.writeLong(hpii.getWorkItemId());
			List<Long> timerInstances = hpii.getTimerInstances();
			if (timerInstances != null) {
				stream.writeInt(timerInstances.size());
				for (Long id : timerInstances) {
					stream.writeLong(id);
				}
			} else {
				stream.writeInt(0);
			}
		} else if (nodeInstance instanceof StagePlanItemInstance) {
			stream.writeShort(STAGE_PLAN_ITEM_INSTANCE);
			StagePlanItemInstance compositeNodeInstance = (StagePlanItemInstance) nodeInstance;
			writePlanItemStates(compositeNodeInstance, stream);
			stream.writeLong(compositeNodeInstance.getWorkItemId());
			List<Long> timerInstances = compositeNodeInstance.getTimerInstances();
			if (timerInstances != null) {
				stream.writeInt(timerInstances.size());
				for (Long id : timerInstances) {
					stream.writeLong(id);
				}
			} else {
				stream.writeInt(0);
			}
			List<NodeInstance> nodeInstances = new ArrayList<NodeInstance>(compositeNodeInstance.getNodeInstances());
			Collections.sort(nodeInstances, new Comparator<NodeInstance>() {

				@Override
				public int compare(NodeInstance o1, NodeInstance o2) {
					return (int) (o1.getId() - o2.getId());
				}
			});
			for (NodeInstance subNodeInstance : nodeInstances) {
				stream.writeShort(PersisterEnums.NODE_INSTANCE);
				writeNodeInstance(context, subNodeInstance);
			}
			stream.writeShort(PersisterEnums.END);
		} else {
			super.writeNodeInstanceContent(stream, nodeInstance, context);
		}
	}

}
