package org.pavanecce.cmmn.instance;

import org.jbpm.workflow.instance.node.EventNodeInstance;

public class SentryInstance extends EventNodeInstance {
	@Override
	public void signalEvent(String type, Object event) {
		super.signalEvent(type, event);
	}

}
