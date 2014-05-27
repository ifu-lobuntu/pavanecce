package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collection;

import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.kie.api.definition.process.Node;

public interface PlanItemContainer extends NodeContainer, PlanningTableContainer {

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

	void setPlanningTable(PlanningTable planningTable);

	PlanningTable getPlanningTable();

	Node superGetNode(long id);
}
