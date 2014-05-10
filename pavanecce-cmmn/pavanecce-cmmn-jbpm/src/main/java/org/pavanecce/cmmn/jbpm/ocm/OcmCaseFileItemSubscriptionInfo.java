package org.pavanecce.cmmn.jbpm.ocm;

import org.apache.jackrabbit.ocm.manager.enumconverter.EnumTypeConverter;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.jbpm.instance.CaseSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.instance.PersistedCaseFileItemSubscriptionInfo;
import org.pavanecce.common.ocm.GrandParentBeanConverterImpl;

@Node(discriminator = false, jcrType = "i:caseFileItemSubscription")
public class OcmCaseFileItemSubscriptionInfo extends AbstractCaseFileItemSubscriptionInfo implements PersistedCaseFileItemSubscriptionInfo {
	@Field(uuid = true)
	private String id;
	@Bean(converter = GrandParentBeanConverterImpl.class)
	private OcmCaseSubscriptionInfo caseSubscription;
	@Field(jcrName = "i:itemName")
	private String itemName;
	@Field(jcrName = "i:transition", converter = EnumTypeConverter.class)
	private CaseFileItemTransition transition;
	@Field(jcrName = "i:processInstanceId")
	private long processInstanceId;
	@Field(jcrName = "i:caseKey")
	private String caseKey;
	@Field(path = true)
	private String path;
	@Field(jcrName = "i:relatedItemName")
	private String relatedItemName;

	public OcmCaseFileItemSubscriptionInfo(OcmCaseSubscriptionInfo caseSubscription) {
		super();
		this.caseSubscription = caseSubscription;
	}

	public OcmCaseFileItemSubscriptionInfo() {
		super();
	}

	public OcmCaseSubscriptionInfo getCaseSubscription() {
		return caseSubscription;
	}

	public String getPath() {
		if (path == null) {
			path = caseSubscription.getPath() + "/caseFileItemSubscriptions/" + processInstanceId + itemName + transition.name();
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

	public void setProcessInstanceId(long processId) {
		this.processInstanceId = processId;
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
	public long getProcessInstanceId() {
		return processInstanceId;
	}

	@Override
	public String getRelatedItemName() {
		return relatedItemName;
	}

	@Override
	public void setRelatedItemName(String relatedItemName) {
		this.relatedItemName = relatedItemName;
	}

	public String getIdentifier() {
		return getCaseSubscription().getId().getClassName() + getCaseSubscription().getId().getId() + super.getIdentifier();
	}
}
