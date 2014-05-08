package org.pavanecce.cmmn.jbpm.instance;

import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;


public interface CaseFileItemSubscriptionInfo {

	String getItemName();

	CaseFileItemTransition getTransition();

	String getRelatedItemName();
	

}
