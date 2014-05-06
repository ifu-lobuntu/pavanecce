package org.pavanecce.cmmn.jbpm.instance;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.MarshallerWriteContext;
import org.drools.core.marshalling.impl.PersisterEnums;
import org.jbpm.marshalling.impl.AbstractProcessInstanceMarshaller;
import org.jbpm.process.core.context.exclusive.ExclusiveGroup;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.context.exclusive.ExclusiveGroupInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.jbpm.workflow.instance.node.DynamicNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;

public class CaseInstanceMarshaller extends AbstractProcessInstanceMarshaller {
	private static final int SENTRY_INSTANCE = 176;
	private static final int ON_PART_INSTANCE = 177;
	private static final int HUMAN_TASK_PLAN_ITEM_INSTANCE = 178;
	private static final int STAGE_PLAN_ITEM_INSTANCE = 179;

	@Override
	protected WorkflowProcessInstanceImpl createProcessInstance() {
		return new CaseInstance();
	}

	@Override
	protected NodeInstanceImpl readNodeInstanceContent(int nodeType, ObjectInputStream stream, MarshallerReaderContext context, WorkflowProcessInstance processInstance) throws IOException {
		NodeInstanceImpl nodeInstance = null;
		switch (nodeType) {
		case SENTRY_INSTANCE:
			SentryInstance sentryInstance = new SentryInstance();
			nodeInstance = sentryInstance;
			break;
		case ON_PART_INSTANCE:
			OnPartInstance onPartInstance = new OnPartInstance();
			nodeInstance = onPartInstance;
			break;
		case HUMAN_TASK_PLAN_ITEM_INSTANCE:
			HumanTaskPlanItemInstance planItemInstance = new HumanTaskPlanItemInstance();
			planItemInstance.internalSetWorkItemId(stream.readLong());
			int nbTimerInstances = stream.readInt();
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
			nodeInstance = new StagePlanItemInstance();
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
		} else if (nodeInstance instanceof OnPartInstance) {
			stream.writeShort(ON_PART_INSTANCE);
		} else if (nodeInstance instanceof HumanTaskPlanItemInstance) {
			stream.writeShort(HUMAN_TASK_PLAN_ITEM_INSTANCE);
			stream.writeLong(((HumanTaskPlanItemInstance) nodeInstance).getWorkItemId());
			List<Long> timerInstances = ((HumanTaskPlanItemInstance) nodeInstance).getTimerInstances();
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
