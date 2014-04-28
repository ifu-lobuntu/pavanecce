package org.pavanecce.cmmn.jbpm.instance;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.MarshallerWriteContext;
import org.jbpm.marshalling.impl.AbstractProcessInstanceMarshaller;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;

public class CaseInstanceMarshaller extends AbstractProcessInstanceMarshaller {
	private static final int SENTRY_INSTANCE = 176;
	private static final int ON_PART_INSTANCE = 177;
	private static final int PLAN_ITEM_INSTANCE = 178;

	@Override
	protected WorkflowProcessInstanceImpl createProcessInstance() {
		return new CaseInstance();
	}

	@Override
	protected NodeInstanceImpl readNodeInstanceContent(int nodeType, ObjectInputStream stream, MarshallerReaderContext context,
			WorkflowProcessInstance processInstance) throws IOException {
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
		case PLAN_ITEM_INSTANCE:
			PlanItemInstance planItemInstance = new PlanItemInstance();
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
		} else if (nodeInstance instanceof PlanItemInstance) {
			stream.writeShort(PLAN_ITEM_INSTANCE);
            stream.writeLong(((PlanItemInstance) nodeInstance).getWorkItemId());
            List<Long> timerInstances =
                ((PlanItemInstance) nodeInstance).getTimerInstances();
	        if (timerInstances != null) {
	            stream.writeInt(timerInstances.size());
	            for (Long id : timerInstances) {
	                stream.writeLong(id);
	            }
	        } else {
	            stream.writeInt(0);
	        }
		} else {
			super.writeNodeInstanceContent(stream, nodeInstance, context);
		}
	}

}
