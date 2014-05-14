package org.pavanecce.cmmn.jbpm.instance.impl;

import org.jbpm.workflow.instance.node.JoinInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.instance.PlanItemInstanceContainer;

public class DefaultJoinInstance extends JoinInstance {
	private static final long serialVersionUID = -8715207082336857538L;

	@Override
	public void internalTrigger(NodeInstance from, String type) {
		super.internalTrigger(from, type);
		if (getNodeInstanceContainer() instanceof PlanItemInstanceContainer) {
			PlanItemInstanceContainer piic=(PlanItemInstanceContainer) getNodeInstanceContainer();
			if(piic.canComplete()  && piic.getPlanItemContainer().isAutoComplete()){
				super.triggerCompleted(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE, true);
			}
		}
	}

	public void triggerCompleted() {
		triggerCompleted(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE, false);
	}

}
