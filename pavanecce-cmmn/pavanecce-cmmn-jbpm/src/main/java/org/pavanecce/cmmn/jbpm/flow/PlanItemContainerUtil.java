package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.Node;

public class PlanItemContainerUtil {
	public static Node getNode(PlanItemContainer pic, long nodeId) {
		Node result = null;
		try {
			result = (Node) pic.superGetNode(nodeId);
		} catch (Exception e) {
		}
		if (result == null && pic.getPlanningTable() != null) {
			result = (Node) pic.getPlanningTable().getNode(nodeId);
		}
		if (result == null) {
			for (PlanItemInfo<?> pi : pic.getPlanItemInfo()) {
				if (pi.getDefinition() instanceof HumanTask) {
					HumanTask ht = (HumanTask) pi.getDefinition();
					if (ht.getPlanningTable() != null) {
						result = (Node) ht.getPlanningTable().getNode(nodeId);
						if (result != null) {
							break;
						}
					}
				}
			}
		}
		return result;
	}
}
