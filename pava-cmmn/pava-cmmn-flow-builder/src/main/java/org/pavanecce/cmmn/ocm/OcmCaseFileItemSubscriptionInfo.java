package org.pavanecce.cmmn.ocm;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;
import org.pavanecce.cmmn.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.instance.CaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.instance.CaseSubscriptionInfo;

@Node(discriminator = false, jcrType = "i:caseFileItemSubscription")
public class OcmCaseFileItemSubscriptionInfo implements CaseFileItemSubscriptionInfo {
	@Field(uuid = true)
	private String id;
	@Bean(converter=GrandParentBeanConverterImpl.class)
	private OcmCaseSubscriptionInfo caseSubscription;
	@Field(jcrName = "i:itemName")
	private String itemName;
	@Field(jcrName = "i:transition",converter=EnumConverter.class)
	private CaseFileItemTransition transition;
	@Field(jcrName = "i:processId")
	private long processId;
	@Field(jcrName = "i:caseKey")
	private String caseKey;
	private String path;

	public OcmCaseFileItemSubscriptionInfo(OcmCaseSubscriptionInfo caseSubscription) {
		super();
		this.caseSubscription=caseSubscription;
	}

	public OcmCaseFileItemSubscriptionInfo() {
		super();
	}
	@Override
	public OcmCaseSubscriptionInfo getCaseSubscription() {
		return caseSubscription;
	}

	public String getPath() {
		if(path==null){
			path=caseSubscription.getPath() + "/caseFileItemSubscriptions/" +processId+itemName+transition.name();
		}
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public void setCaseSubscription(CaseSubscriptionInfo<?> caseSubscription) {
		this.caseSubscription = (OcmCaseSubscriptionInfo) caseSubscription;
	}
	

	public void setCaseSubscription(OcmCaseSubscriptionInfo caseSubscription) {
		this.caseSubscription = caseSubscription;
	}

	@Override
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	@Override
	public void setTransition(CaseFileItemTransition transition) {
		this.transition = transition;
	}

	@Override
	public void setProcessId(long processId) {
		this.processId = processId;
	}

	@Override
	public void setCaseKey(String caseKey) {
		this.caseKey = caseKey;
	}

	public String getId() {
		return id;
	}

	@Override
	public String getCaseKey() {
		return caseKey;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getItemName() {
		return itemName;
	}

	@Override
	public CaseFileItemTransition getTransition() {
		return transition;
	}

	@Override
	public long getProcessId() {
		return processId;
	}

}
