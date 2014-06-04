package org.pavanecce.cmmn.jbpm.event;

public abstract class AbstractCaseFileItemSubscriptionInfo implements CaseFileItemSubscriptionInfo {
	boolean active = true;

	public AbstractCaseFileItemSubscriptionInfo() {
		super();
	}

	@Override
	public void invalidate() {
		active = false;
	}

	@Override
	public void validate() {
		active = true;
	}

	@Override
	public boolean isValid() {
		return active;
	}

	@Override
	public int hashCode() {
		return getItemName().hashCode();
	}

	@Override
	public boolean isEquivalent(CaseFileItemSubscriptionInfo other) {
		return equals(other);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof CaseFileItemSubscriptionInfo) {
			CaseFileItemSubscriptionInfo other = (CaseFileItemSubscriptionInfo) obj;
			if (other.getProcessInstanceId() == getProcessInstanceId() && other.getCaseKey().equals(getCaseKey()) && other.getItemName().equals(getItemName())
					&& other.getTransition() == getTransition()) {
				if (other.getRelatedItemName() != null) {
					return other.getRelatedItemName().equals(getRelatedItemName());
				} else {
					return getRelatedItemName() == null;
				}
			}
		}
		return false;
	}

	public String getIdentifier() {
		return getProcessInstanceId() + getCaseKey() + getItemName() + getTransition() + getRelatedItemName();
	}

}