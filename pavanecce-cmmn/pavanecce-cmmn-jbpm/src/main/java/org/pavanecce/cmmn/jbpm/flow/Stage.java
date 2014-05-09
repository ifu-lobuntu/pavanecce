package org.pavanecce.cmmn.jbpm.flow;

import java.util.ArrayList;
import java.util.Collection;

import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;

public class Stage extends CompositeContextNode implements PlanItemDefinition, PlanItemContainer {

	private static final long serialVersionUID = 3123425777169912160L;
	private String elementId;
	private boolean autoComplete;
	private Collection<PlanItemInfo> planItemInfo = new ArrayList<PlanItemInfo>();
	private StartNode defaultStart;
	private Split defaultSplit;
	private EndNode defaultEnd;
	private Join defaultJoin;
	private Case theCase;

	@Override
	public Case getCase() {
		return this.theCase;
	}

	public void setCase(Case theCase) {
		this.theCase = theCase;
	}

	@Override
	public StartNode getDefaultStart() {
		return defaultStart;
	}

	@Override
	public void setDefaultStart(StartNode defaultStart) {
		this.defaultStart = defaultStart;
	}

	@Override
	public Split getDefaultSplit() {
		return defaultSplit;
	}

	@Override
	public void setDefaultSplit(Split defaultSplit) {
		this.defaultSplit = defaultSplit;
	}

	@Override
	public EndNode getDefaultEnd() {
		return defaultEnd;
	}

	@Override
	public void setDefaultEnd(EndNode defaultEnd) {
		this.defaultEnd = defaultEnd;
	}

	@Override
	public Join getDefaultJoin() {
		return defaultJoin;
	}

	@Override
	public void setDefaultJoin(Join defaultJoin) {
		this.defaultJoin = defaultJoin;
	}

	@Override
	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}



	@Override
	public void addPlanItemInfo(PlanItemInfo d) {
		planItemInfo.add(d);
	}

	@Override
	public Collection<PlanItemInfo> getPlanItemInfo() {
		return planItemInfo;
	}

	public boolean isAutoComplete() {
		return autoComplete;
	}

	public void setAutoComplete(boolean autoComplete) {
		this.autoComplete = autoComplete;
	}

}
