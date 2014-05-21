package org.pavanecce.cmmn.jbpm.planning;

import java.io.Serializable;

public class ApplicableDiscretionaryItem implements Serializable {
	private static final long serialVersionUID = 5715302812688586256L;
	private String discretionaryItemId;
	private String planItemName;
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
	public ApplicableDiscretionaryItem(String discretionaryItemId, String planItemName) {
		super();
		this.discretionaryItemId = discretionaryItemId;
		this.planItemName = planItemName;
	}
	public ApplicableDiscretionaryItem() {
		super();
	}
	
}
