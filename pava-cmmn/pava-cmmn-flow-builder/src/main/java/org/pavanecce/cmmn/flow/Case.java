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
	/**
	 * 
	 */
	private static final long serialVersionUID = -2253866933695827108L;
	private List<CaseParameter> inputParameters=new ArrayList<CaseParameter>();
	private List<CaseParameter> outputParameters=new ArrayList<CaseParameter>();
	private Collection<PlanItemDefinition> planItemDefinitions=new ArrayList<PlanItemDefinition>();
	private Collection<Role> roles=new ArrayList<Role>();

	public void addInputParameter(CaseParameter cp) {
		inputParameters.add(cp);
	}
	public List<CaseParameter> getInputParameters() {
		return inputParameters;
	}
	public void addOutputParameter(CaseParameter cp) {
		inputParameters.add(cp);
	}
	public List<CaseParameter> getOutputParameters() {
		return outputParameters;
	}
	public Set<CaseFileItemOnPart> findCaseFileItemOnPartsFor(CaseFileItem item) {
		Node[] nodes = getNodes();
		Set<CaseFileItemOnPart> onCaseFileItemParts = new HashSet<CaseFileItemOnPart>();
		findCaseFileITemOnPartsFor(item, nodes, onCaseFileItemParts);
		return onCaseFileItemParts;

	}
	private void findCaseFileITemOnPartsFor(CaseFileItem item, Node[] nodes, Set<CaseFileItemOnPart> onCaseFileItemParts) {
		for (Node node : nodes) {
			if (node instanceof Sentry) {
				Sentry sentry = (Sentry) node;
				for (OnPart onPart : sentry.getOnParts()) {
					if (onPart instanceof CaseFileItemOnPart) {
						CaseFileItemOnPart part = (CaseFileItemOnPart) onPart;
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
