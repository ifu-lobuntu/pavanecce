package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collection;
import java.util.Set;

import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.Node;

public interface Sentry extends Node, CMMNElement {

	void addPlanItemExiting(PlanItem planItem);

	Collection<? extends OnPart> getOnParts();

	void addOnPart(OnPart part);

	Set<PlanItem> getPlanItemsExiting();

	void setElementId(String value);

	Constraint getCondition();

	void setCondition(Constraint c);

}
