package org.pavanecce.cmmn.jbpm;

import java.io.Serializable;

public class ApplicableDiscretionaryItem implements Serializable {

	private static final long serialVersionUID = 112312312312L;
	private String discretionaryItemId;
	private String planItemName;
	private boolean isRepeatable;
	private boolean isActivatedManually;
	private boolean hasEntryCriteria;

	public ApplicableDiscretionaryItem() {
		super();
	}

	public ApplicableDiscretionaryItem(String discretionaryItemId, String planItemName) {
		super();
		this.discretionaryItemId = discretionaryItemId;
		this.planItemName = planItemName;
	}

	public String getDiscretionaryItemId() {
		return discretionaryItemId;
	}

	public void setDiscretionaryItemId(String discretionaryItemId) {
		this.discretionaryItemId = discretionaryItemId;
	}

	public String getPlanItemName() {
		return planItemName;
	}

	public void setPlanItemName(String planItemName) {
		this.planItemName = planItemName;
	}

	public boolean isRepeatable() {
		return isRepeatable;
	}

	public void setRepeatable(boolean isRepeatable) {
		this.isRepeatable = isRepeatable;
	}

	public boolean isActivatedManually() {
		return isActivatedManually;
	}

	public void setActivatedManually(boolean isActivatedAutomatically) {
		this.isActivatedManually = isActivatedAutomatically;
	}

	public boolean isHasEntryCriteria() {
		return hasEntryCriteria;
	}

	public void setHasEntryCriteria(boolean hasEntryCriteria) {
		this.hasEntryCriteria = hasEntryCriteria;
	}

}
