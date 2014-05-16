package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collection;

import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;

public interface PlanItemContainer extends NodeContainer {

	public void addPlanItemInfo(PlanItemInfo<?> d);

	public Collection<PlanItemInfo<?>> getPlanItemInfo();

	void setDefaultStart(StartNode n);

	StartNode getDefaultStart();

	void setDefaultSplit(DefaultSplit n);

	DefaultSplit getDefaultSplit();

	void setDefaultEnd(EndNode n);

	EndNode getDefaultEnd();

	void setDefaultJoin(DefaultJoin n);

	DefaultJoin getDefaultJoin();
	
	Case getCase();
	
	boolean isAutoComplete();

	public abstract void setPlanningTable(PlanningTable planningTable);

	public abstract PlanningTable getPlanningTable();
}
