package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collection;

import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;

public interface PlanItemContainer extends NodeContainer {

	public void addPlanItemInfo(PlanItemInfo d);

	public Collection<PlanItemInfo> getPlanItemInfo();

	void setDefaultStart(StartNode n);

	StartNode getDefaultStart();

	void setDefaultSplit(Split n);

	Split getDefaultSplit();

	void setDefaultEnd(EndNode n);

	EndNode getDefaultEnd();

	void setDefaultJoin(Join n);

	Join getDefaultJoin();
	
	Case getCase();
}
