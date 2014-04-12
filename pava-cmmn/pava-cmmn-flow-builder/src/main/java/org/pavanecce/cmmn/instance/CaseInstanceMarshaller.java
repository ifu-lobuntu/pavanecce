package org.pavanecce.cmmn.instance;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.MarshallerWriteContext;
import org.drools.core.marshalling.impl.PersisterEnums;
import org.jbpm.marshalling.impl.AbstractProcessInstanceMarshaller;
import org.jbpm.marshalling.impl.RuleFlowProcessInstanceMarshaller;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;

public class CaseInstanceMarshaller extends AbstractProcessInstanceMarshaller {
	private static final int SENTRY_INSTANCE = 176;

	@Override
	protected WorkflowProcessInstanceImpl createProcessInstance() {
		return new RuleFlowProcessInstance();
	}

	@Override
	protected NodeInstanceImpl readNodeInstanceContent(int nodeType, ObjectInputStream stream, MarshallerReaderContext context,
			WorkflowProcessInstance processInstance) throws IOException {
		NodeInstanceImpl nodeInstance = null;
		switch (nodeType) {
		case SENTRY_INSTANCE:
			SentryInstance sentryInstance = new SentryInstance();
			nodeInstance=sentryInstance;
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
		} else {
			super.writeNodeInstanceContent(stream, nodeInstance, context);
		}
	}

}
