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
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.pavanecce.cmmn.jbpm.instance.ControllablePlanItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.instance.OccurrablePlanItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.instance.OnPartInstance;
import org.pavanecce.cmmn.jbpm.instance.PlanElementState;
import org.pavanecce.cmmn.jbpm.instance.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.CaseTaskPlanItemInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.DefaultJoinInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.DefaultSplitInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.HumanTaskPlanItemInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.MilestonePlanItemInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.PlanItemInstanceFactoryNodeInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.SentryInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.StagePlanItemInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.TimerEventPlanItemInstance;
import org.pavanecce.cmmn.jbpm.instance.impl.UserEventPlanItemInstance;

public class CaseInstanceMarshaller extends AbstractProcessInstanceMarshaller {
	private static final int SENTRY_INSTANCE = 176;
	private static final int ON_PART_INSTANCE = 177;
	private static final int HUMAN_TASK_PLAN_ITEM_INSTANCE = 178;
	private static final int STAGE_PLAN_ITEM_INSTANCE = 179;
	private static final int USER_EVENT_PLAN_ITEM_INSTANCE = 180;
	private static final int TIMER_EVENT_PLAN_ITEM_INSTANCE = 181;
	private static final int MILESTONE_PLAN_ITEM_INSTANCE = 182;
	private static final int CASE_TASK_PLAN_ITEM_INSTANCE = 183;
	private static final int DEFAULT_JOIN_INSTANCE = 184;
	private static final int PLAN_ITEM_INSTANCE_FACTORY_NODE_INSTANCE = 185;
	private static final int DEFAULT_SPLIT_INSTANCE = 186;

	@Override
	protected WorkflowProcessInstanceImpl createProcessInstance() {
		return new CaseInstance();
	}

	@Override
	public Object writeProcessInstance(MarshallerWriteContext context, ProcessInstance processInstance) throws IOException {
		Object result = super.writeProcessInstance(context, processInstance);
		if (processInstance instanceof CaseInstance) {
			context.stream.writeInt(((CaseInstance) processInstance).getPlanElementState().ordinal());
			context.stream.writeLong(((CaseInstance) processInstance).getWorkItemId());
		}
		return result;
	}

	@Override
	public ProcessInstance readProcessInstance(MarshallerReaderContext context) throws IOException {
		ObjectInputStream stream = context.stream;
		ProcessInstance read = super.readProcessInstance(context);
		if (read instanceof CaseInstance) {
			((CaseInstance) read).setPlanElementState(PlanElementState.values()[stream.readInt()]);
			((CaseInstance) read).setWorkItemId(stream.readLong());
		}
		return read;
	}

	private void writePlanItemStates(ControllablePlanItemInstanceLifecycle<?> pi, ObjectOutputStream stream) throws IOException {
		stream.writeInt(pi.getPlanElementState().ordinal());
		stream.writeInt(pi.getLastBusyState().ordinal());
		stream.writeBoolean(pi.isCompletionRequired());
	}

	private void readPlanItemStates(ControllablePlanItemInstanceLifecycle<?> pi, ObjectInputStream stream) throws IOException {
		pi.setPlanElementState(PlanElementState.values()[stream.readInt()]);
		pi.setLastBusyState(PlanElementState.values()[stream.readInt()]);
		pi.internalSetCompletionRequired(stream.readBoolean());
	}

	@Override
	protected NodeInstanceImpl readNodeInstanceContent(int nodeType, ObjectInputStream stream, MarshallerReaderContext context, WorkflowProcessInstance processInstance) throws IOException {
		NodeInstanceImpl nodeInstance = null;
		switch (nodeType) {
		case PLAN_ITEM_INSTANCE_FACTORY_NODE_INSTANCE:
			PlanItemInstanceFactoryNodeInstance piifni = new PlanItemInstanceFactoryNodeInstance();
			piifni.internalSetPlanItemInstanceRequired(context.stream.readBoolean());
			piifni.internalSetPlanItemInstanceStillRequired(context.stream.readBoolean());
			nodeInstance = piifni;
			break;
		case SENTRY_INSTANCE:
			SentryInstance sentryInstance = new SentryInstance();
			int number = stream.readInt();
			if (number > 0) {
				Map<Long, Integer> triggers = new HashMap<Long, Integer>();
				for (int i = 0; i < number; i++) {
					long l = stream.readLong();
					int count = stream.readInt();
					triggers.put(l, count);
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
			readOccurrablePlanItemInstance(stream, (MilestonePlanItemInstance) nodeInstance);
			break;
		case CASE_TASK_PLAN_ITEM_INSTANCE:
			nodeInstance = new CaseTaskPlanItemInstance();
			((CaseTaskPlanItemInstance) nodeInstance).internalSetProcessInstanceId(stream.readLong());
			((CaseTaskPlanItemInstance) nodeInstance).internalSetWorkItemId(stream.readLong());
			readPlanItemStates(((CaseTaskPlanItemInstance) nodeInstance), stream);
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
			readOccurrablePlanItemInstance(stream, (UserEventPlanItemInstance) nodeInstance);
			break;
		case TIMER_EVENT_PLAN_ITEM_INSTANCE:
			nodeInstance = new TimerEventPlanItemInstance();
			readOccurrablePlanItemInstance(stream, (TimerEventPlanItemInstance) nodeInstance);
			((TimerEventPlanItemInstance) nodeInstance).internalSetTimerInstanceId(stream.readLong());
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
		case DEFAULT_SPLIT_INSTANCE:
			nodeInstance = new DefaultSplitInstance();
			break;
		case DEFAULT_JOIN_INSTANCE:
			nodeInstance = new DefaultJoinInstance();
			number = stream.readInt();
			if (number > 0) {
				Map<Long, Integer> triggers = new HashMap<Long, Integer>();
				for (int i = 0; i < number; i++) {
					long l = stream.readLong();
					int count = stream.readInt();
					triggers.put(l, count);
				}
				((JoinInstance) nodeInstance).internalSetTriggers(triggers);
			}
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

	protected void readOccurrablePlanItemInstance(ObjectInputStream stream, OccurrablePlanItemInstanceLifecycle<?> nodeInstance) throws IOException {
		nodeInstance.internalSetRequired(stream.readBoolean());
		nodeInstance.setPlanElementState(PlanElementState.values()[stream.readInt()]);
	}

	@Override
	protected void writeNodeInstanceContent(ObjectOutputStream stream, NodeInstance nodeInstance, MarshallerWriteContext context) throws IOException {
		if (nodeInstance instanceof SentryInstance) {
			stream.writeShort(SENTRY_INSTANCE);
			Map<Long, Integer> triggers = ((JoinInstance) nodeInstance).getTriggers();
			stream.writeInt(triggers.size());
			List<Long> keys = new ArrayList<Long>(triggers.keySet());
			Collections.sort(keys, new Comparator<Long>() {

				public int compare(Long o1, Long o2) {
					return o1.compareTo(o2);
				}
			});
			for (Long key : keys) {
				stream.writeLong(key);
				stream.writeInt(triggers.get(key));
			}

		} else if (nodeInstance instanceof PlanItemInstanceFactoryNodeInstance) {
			stream.writeShort(PLAN_ITEM_INSTANCE_FACTORY_NODE_INSTANCE);
			stream.writeBoolean(((PlanItemInstanceFactoryNodeInstance) nodeInstance).isPlanItemInstanceRequired());
			stream.writeBoolean(((PlanItemInstanceFactoryNodeInstance) nodeInstance).isPlanItemInstanceStillRequired());
		} else if (nodeInstance instanceof OnPartInstance) {
			stream.writeShort(ON_PART_INSTANCE);
		} else if (nodeInstance instanceof MilestonePlanItemInstance) {
			stream.writeShort(MILESTONE_PLAN_ITEM_INSTANCE);
			writeOccurrableNodeInstance(stream, (MilestonePlanItemInstance) nodeInstance);
		} else if (nodeInstance instanceof CaseTaskPlanItemInstance) {
			stream.writeShort(CASE_TASK_PLAN_ITEM_INSTANCE);
			stream.writeLong(((CaseTaskPlanItemInstance) nodeInstance).getProcessInstanceId());
			stream.writeLong(((CaseTaskPlanItemInstance) nodeInstance).getWorkItemId());
			writePlanItemStates((CaseTaskPlanItemInstance) nodeInstance, stream);
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
			writeOccurrableNodeInstance(stream, (UserEventPlanItemInstance) nodeInstance);
		} else if (nodeInstance instanceof TimerEventPlanItemInstance) {
			stream.writeShort(TIMER_EVENT_PLAN_ITEM_INSTANCE);
			writeOccurrableNodeInstance(stream, (TimerEventPlanItemInstance) nodeInstance);
			stream.writeLong(((TimerEventPlanItemInstance) nodeInstance).getTimerInstanceId());
		} else if (nodeInstance instanceof HumanTaskPlanItemInstance) {
			stream.writeShort(HUMAN_TASK_PLAN_ITEM_INSTANCE);
			HumanTaskPlanItemInstance hpii = (HumanTaskPlanItemInstance) nodeInstance;
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
		} else if (nodeInstance instanceof DefaultSplitInstance) {
			stream.writeShort(DEFAULT_SPLIT_INSTANCE);
		} else if (nodeInstance instanceof DefaultJoinInstance) {
			stream.writeShort(DEFAULT_JOIN_INSTANCE);
			Map<Long, Integer> triggers = ((DefaultJoinInstance) nodeInstance).getTriggers();
			stream.writeInt(triggers.size());
			List<Long> keys = new ArrayList<Long>(triggers.keySet());
			Collections.sort(keys, new Comparator<Long>() {

				public int compare(Long o1, Long o2) {
					return o1.compareTo(o2);
				}
			});
			for (Long key : keys) {
				stream.writeLong(key);
				stream.writeInt(triggers.get(key));
			}
		} else {
			super.writeNodeInstanceContent(stream, nodeInstance, context);
		}
	}

	protected void writeOccurrableNodeInstance(ObjectOutputStream stream, OccurrablePlanItemInstanceLifecycle<?> nodeInstance) throws IOException {
		stream.writeBoolean(nodeInstance.isCompletionRequired());
		stream.writeInt(nodeInstance.getPlanElementState().ordinal());
	}

}
