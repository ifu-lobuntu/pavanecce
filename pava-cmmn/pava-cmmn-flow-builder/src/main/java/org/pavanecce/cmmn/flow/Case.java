package org.pavanecce.cmmn.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.CompositeNode;
import org.kie.api.definition.process.Node;

public class Case extends RuleFlowProcess {
	private List<CaseParameter> caseParameters=new ArrayList<CaseParameter>();
	private Collection<PlanItemDefinition> planItemDefinitions=new ArrayList<PlanItemDefinition>();
	private Collection<Role> roles=new ArrayList<Role>();

	public void addParameter(CaseParameter cp) {
		caseParameters.add(cp);
	}
	public List<CaseParameter> getCaseParameters() {
		return caseParameters;
	}
	public Set<OnCaseFileItemPart> findCaseFileItemOnPartsFor(CaseFileItem item) {
		Node[] nodes = getNodes();
		Set<OnCaseFileItemPart> onCaseFileItemParts = new HashSet<OnCaseFileItemPart>();
		findCaseFileITemOnPartsFor(item, nodes, onCaseFileItemParts);
		return onCaseFileItemParts;

	}
	private void findCaseFileITemOnPartsFor(CaseFileItem item, Node[] nodes, Set<OnCaseFileItemPart> onCaseFileItemParts) {
		for (Node node : nodes) {
			if (node instanceof Sentry) {
				Sentry sentry = (Sentry) node;
				for (OnPart onPart : sentry.getOnParts()) {
					if (onPart instanceof OnCaseFileItemPart) {
						OnCaseFileItemPart part = (OnCaseFileItemPart) onPart;
						if (part.getCaseFileItem().getElementId().equals(item.getElementId())) {
							onCaseFileItemParts.add(part);
						}
					}
				}
			}else if(node instanceof CompositeNode){
				findCaseFileITemOnPartsFor(item, ((CompositeNode) node).getNodes(), onCaseFileItemParts);
			}
		}
	}
	public String getCaseKey() {
		return super.getPackageName()+super.getId()+super.getVersion();
	}
	public void addPlanItemDefinition(PlanItemDefinition d){
		planItemDefinitions.add(d);
	}
	public Collection<PlanItemDefinition> getPlanItemDefinitions() {
		return planItemDefinitions;
	}
	public Collection<Role> getRoles() {
		return roles;
	}
	public void addRole(Role r){
		roles.add(r);
	}
}
