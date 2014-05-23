package org.pavanecce.cmmn.jbpm.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.CompositeNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.kie.api.definition.process.Node;

public class Case extends RuleFlowProcess implements PlanItemContainer {
	private static final long serialVersionUID = -2253866933695827108L;
	public static final String WORK_ITEM = "WorkItem";
	private Map<String, CaseParameter> inputParameters = new HashMap<String, CaseParameter>();
	private Map<String, CaseParameter> outputParameters = new HashMap<String, CaseParameter>();
	private Collection<PlanItemDefinition> planItemDefinitions = new ArrayList<PlanItemDefinition>();
	private Collection<PlanItemInfo<?>> planItemInfo = new ArrayList<PlanItemInfo<?>>();
	private Collection<Role> roles = new ArrayList<Role>();
	private StartNode defaultStart;
	private DefaultSplit defaultSplit;
	private EndNode defaultEnd;
	private DefaultJoin defaultJoin;
	private PlanningTable planningTable;


	@Override
	public StartNode getDefaultStart() {
		return defaultStart;
	}

	@Override
	public void setDefaultStart(StartNode defaultStart) {
		this.defaultStart = defaultStart;
	}

	@Override
	public DefaultSplit getDefaultSplit() {
		return defaultSplit;
	}

	@Override
	public void setDefaultSplit(DefaultSplit defaultSplit) {
		this.defaultSplit = defaultSplit;
	}

	@Override
	public Case getCase() {
		return this;
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
	public void setDefaultJoin(DefaultJoin n) {
		this.defaultJoin = n;
	}

	@Override
	public DefaultJoin getDefaultJoin() {
		return defaultJoin;
	}

	public void addInputParameter(CaseParameter cp) {
		inputParameters.put(cp.getElementId(), cp);
	}

	public Collection<CaseParameter> getInputParameters() {
		return inputParameters.values();
	}

	public void addOutputParameter(CaseParameter cp) {
		inputParameters.put(cp.getElementId(),cp);
	}

	public Collection<CaseParameter> getOutputParameters() {
		return outputParameters.values();
	}
	public CaseParameter getInputParameter(String id){
		return inputParameters.get(id);
	}
	public CaseParameter getOutputParameter(String id){
		return outputParameters.get(id);
	}

	public Collection<CaseFileItemOnPart> findCaseFileItemOnPartsFor(CaseFileItem item) {
		Node[] nodes = getNodes();
		Map<String, CaseFileItemOnPart> onCaseFileItemParts = new HashMap<String, CaseFileItemOnPart>();
		findCaseFileItemOnPartsFor(item, nodes, onCaseFileItemParts);
		return onCaseFileItemParts.values();

	}

	private void findCaseFileItemOnPartsFor(CaseFileItem item, Node[] nodes, Map<String, CaseFileItemOnPart> onCaseFileItemParts) {
		for (Node node : nodes) {
			if (node instanceof Sentry) {
				Sentry sentry = (Sentry) node;
				for (OnPart onPart : sentry.getOnParts()) {
					if (onPart instanceof CaseFileItemOnPart) {
						CaseFileItemOnPart part = (CaseFileItemOnPart) onPart;
						if (part.getSourceCaseFileItem().getElementId().equals(item.getElementId())) {
							onCaseFileItemParts.put(((CaseFileItemOnPart) onPart).getIdentifier(), part);
						}
					}
				}
			} else if (node instanceof CompositeNode) {
				findCaseFileItemOnPartsFor(item, ((CompositeNode) node).getNodes(), onCaseFileItemParts);
			}
		}
	}

	@Override
	public Node getNode(long id) {
		try {
			return super.getNode(id);
		} catch (IllegalArgumentException e) {
			if (getPlanningTable() != null) {
				return getPlanningTable().getNode(id);
			}
		}
		return null;
	}

	public String getCaseKey() {
		return super.getPackageName() + super.getId() + super.getVersion();
	}

	public void addPlanItemDefinition(PlanItemDefinition d) {
		planItemDefinitions.add(d);
		if (d instanceof Stage) {
			((Stage) d).setCase(this);
		}
	}

	@Override
	public void addPlanItemInfo(PlanItemInfo<?> d) {
		planItemInfo.add(d);
	}

	public Collection<PlanItemDefinition> getPlanItemDefinitions() {
		return planItemDefinitions;
	}

	public Collection<Role> getRoles() {
		return roles;
	}

	public void addRole(Role r) {
		roles.add(r);
	}

	@Override
	public Collection<PlanItemInfo<?>> getPlanItemInfo() {
		return this.planItemInfo;
	}

	@Override
	public PlanningTable getPlanningTable() {
		return planningTable;
	}

	@Override
	public void setPlanningTable(PlanningTable planningTable) {
		this.planningTable = planningTable;
		planningTable.setPlanItemContainer(this);
	}
}
