package org.pavanecce.cmmn.instance;

import java.util.List;

import org.jbpm.workflow.instance.node.EventNodeInstance;
import org.kie.api.runtime.process.EventListener;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.flow.OnPart;
import org.pavanecce.cmmn.flow.Sentry;

public class SentryInstance extends EventNodeInstance implements EventListener {
	@Override
	public void signalEvent(String type, Object event) {
		super.signalEvent(type, event);
		CaseFileItemEvent caseFileItemEvent=(CaseFileItemEvent) event;
		//TODO store the new value
	}
	@Override
	public void internalTrigger(NodeInstance from, String type) {
		super.internalTrigger(from, type);
		Sentry s = this.getSentry();
//		List<OnPart> onParts = s.getOnParts();
//		for (OnPart onPart : onParts) {
//			getProcessInstance().addEventListener(onPart.getType(getProcessInstance().getId()), this, true);
//		}
	}
	private Sentry getSentry() {
		return (Sentry) getEventNode();
	}
	@Override
	public String[] getEventTypes() {
		return new String[]{"not used"};
	}
}
