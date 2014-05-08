package org.pavanecce.cmmn.jbpm.instance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;

public class CaseInstance extends RuleFlowProcessInstance {
	private static final long serialVersionUID = 8715128915363796623L;

	public Case getCase() {
		return (Case) getProcess();
	}

	public Collection<OnPartInstance> findCaseFileItemOnPartInstancesFor(CaseFileItem item) {
		Collection<NodeInstance> nodes = getNodeInstances();
		Map<String, OnPartInstance> onCaseFileItemParts = new HashMap<String, OnPartInstance>();
		findCaseFileItemOnPartsFor(item, nodes, onCaseFileItemParts);
		return onCaseFileItemParts.values();

	}

	private void findCaseFileItemOnPartsFor(CaseFileItem item, Collection<NodeInstance> nodes, Map<String, OnPartInstance> onCaseFileItemParts) {
		for (NodeInstance node : nodes) {
			if (node instanceof OnPartInstance) {
				OnPartInstance onPartInstance = (OnPartInstance) node;
				if (onPartInstance.getOnPart() instanceof CaseFileItemOnPart) {
					CaseFileItemOnPart part = (CaseFileItemOnPart) onPartInstance.getOnPart();
					if (part.getSourceCaseFileItem().getElementId().equals(item.getElementId())) {
						onCaseFileItemParts.put(part.getIdentifier(), onPartInstance);
					}
				}
			} else if (node instanceof StagePlanItemInstance) {
				findCaseFileItemOnPartsFor(item, ((StagePlanItemInstance) node).getNodeInstances(), onCaseFileItemParts);
			}
		}
	}

}
