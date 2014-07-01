package org.pavanecce.cmmn.jbpm.infra;

import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.CREATE;
import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.DELETE;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.kie.internal.runtime.KnowledgeRuntime;
import org.pavanecce.cmmn.jbpm.event.AbstractCaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.event.CaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.ExpressionUtil;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.OnPartInstance;

public class OnPartInstanceSubscription extends AbstractCaseFileItemSubscriptionInfo implements CaseFileItemSubscriptionInfo {
	CaseFileItemOnPart source;
	private Set<CaseParameter> subscribingParameters = new HashSet<CaseParameter>();
	private InternalKnowledgeRuntime kr;
	private String caseKey;
	private long processInstanceId;

	public OnPartInstanceSubscription(OnPartInstance source, CaseParameter caseParameter) {
		super();
		// InternalRuntimeManager manager = (InternalRuntimeManager)
		// source.getCaseInstance().getKnowledgeRuntime().getEnvironment().get("RuntimeManager");
		this.subscribingParameters.add(caseParameter);
		kr = source.getProcessInstance().getKnowledgeRuntime();
		this.source = (CaseFileItemOnPart) source.getOnPart();
		this.caseKey = source.getCaseInstance().getCase().getCaseKey();
		this.processInstanceId = source.getCaseInstance().getId();
	}

	public CaseFileItemOnPart getSource() {
		return (CaseFileItemOnPart) source;
	}

	@Override
	public String getItemName() {
		return getSource().getSourceCaseFileItem().getName();
	}

	@Override
	public CaseFileItemTransition getTransition() {
		return getSource().getStandardEvent();
	}

	@Override
	public String getRelatedItemName() {
		return getSource().getRelatedCaseFileItem() != null ? getSource().getRelatedCaseFileItem().getName() : null;
	}

	public boolean meetsBindingRefinementCriteria(Object o, CaseInstance caseInstance) {
		Set<CaseParameter> subscribingParameters2 = this.subscribingParameters;
		if (getSource().getStandardEvent() == CREATE || getSource().getStandardEvent() == DELETE) {
			return true; // TODO Can't make assumptions about whether the process state contains the new/old
							// object
		}
		for (CaseParameter caseParameter : subscribingParameters2) {
			if (caseParameter.getBindingRefinement() == null || !caseParameter.getBindingRefinement().isValid()) {
				return true;
			} else {
				Object val = ExpressionUtil.readFromBindingRefinement(caseParameter, caseInstance, null);
				if (caseParameter.getBoundVariable().isCollection()) {
					if (val instanceof Collection && ((Collection<?>) val).contains(o)) {
						return true;
					}
					if (val != null && val.equals(o)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void addParameter(CaseParameter parameter) {
		this.subscribingParameters.add(parameter);
	}

	public CaseFileItem getVariable() {
		return getSource().getSourceCaseFileItem();
	}

	private static boolean isInstance(Object source, String stringType) {
		try {
			return Class.forName(stringType).isInstance(source);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isListeningTo(Object target, CaseFileItemTransition t) {
		if (t == getTransition()) {
			if (getVariable().isCollection()) {
				CollectionDataType dt = (CollectionDataType) getVariable().getType();
				String elementClassName = dt.getElementClassName();
				if (isInstance(target, elementClassName)) {
					return true;
				}
			} else {
				String stringType = getVariable().getType().getStringType();
				if (isInstance(target, stringType)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getCaseKey() {
		return caseKey;

	}

	@Override
	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public KnowledgeRuntime getKnowledgeRuntime() {
		return kr;
	}

}
