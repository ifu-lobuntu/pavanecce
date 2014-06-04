package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collection;
import java.util.HashSet;

import org.kie.api.definition.process.Node;

public class PlanningTable extends TableItem {

	private static final long serialVersionUID = 11515151511L;
	private Collection<ApplicabilityRule> applicabilityRules = new HashSet<ApplicabilityRule>();
	private Collection<TableItem> tableItems = new HashSet<TableItem>();
	private long id;
	private PlanItemContainer planItemContainer;

	@Override
	public Node getNode(long id) {
		for (TableItem ti : tableItems) {
			if (ti.getId() == id) {
				return ti;
			} else if (ti instanceof PlanningTable) {
				Node node = ti.getNode(id);
				if (node != null) {
					return node;
				}
			}
		}
		return super.getNode(id);
	}

	public Collection<ApplicabilityRule> getOwnedApplicabilityRules() {
		return applicabilityRules;
	}

	public void addOwnedApplicabilityRule(ApplicabilityRule applicabilityRule) {
		applicabilityRules.add(applicabilityRule);
	}

	public Collection<TableItem> getTableItems() {
		return tableItems;
	}

	public void addTableItem(TableItem ti) {
		this.tableItems.add(ti);
		ti.setParentTable(this);
	}

	public void setId(long next) {
		this.id = next;
	}

	public long getId() {
		return id;
	}

	public DiscretionaryItem<?> getDiscretionaryItemById(String elementId) {
		for (TableItem ti : getTableItems()) {
			if (ti.getElementId().equals(elementId)) {
				return (DiscretionaryItem<?>) ti;
			} else if (ti instanceof PlanningTable) {
				DiscretionaryItem<?> result = ((PlanningTable) ti).getDiscretionaryItemById(elementId);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public PlanItemContainer getFirstPlanItemContainer() {
		if (planItemContainer == null && getParentTable() != null) {
			return getParentTable().getFirstPlanItemContainer();
		}
		return this.planItemContainer;
	}

	public void setPlanItemContainer(PlanItemContainer planItemContainer) {
		this.planItemContainer = planItemContainer;
	}

}
