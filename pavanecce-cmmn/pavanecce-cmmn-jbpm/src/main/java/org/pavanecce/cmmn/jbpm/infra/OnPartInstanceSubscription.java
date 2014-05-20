package org.pavanecce.cmmn.jbpm.infra;

import static org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.drools.core.spi.ProcessContext;
import org.pavanecce.cmmn.jbpm.event.CaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.ocm.AbstractCaseFileItemSubscriptionInfo;

public class OnPartInstanceSubscription extends AbstractCaseFileItemSubscriptionInfo implements CaseFileItemSubscriptionInfo {
	CaseFileItemOnPart source;
	private Set<CaseParameter> subscribingParameters = new HashSet<CaseParameter>();
	private String caseKey;
	private long processInstanceId;

	public OnPartInstanceSubscription(String caseKey, long processInstanceId, CaseFileItemOnPart source, CaseParameter caseParameter) {
		super();
		this.caseKey = caseKey;
		this.subscribingParameters.add(caseParameter);
		this.source = source;
		this.processInstanceId = processInstanceId;
	}

	public CaseFileItemOnPart getSource() {
		return source;
	}

	@Override
	public String getItemName() {
		return source.getSourceCaseFileItem().getName();
	}

	@Override
	public CaseFileItemTransition getTransition() {
		return source.getStandardEvent();
	}

	@Override
	public String getRelatedItemName() {
		return source.getRelatedCaseFileItem() != null ? source.getRelatedCaseFileItem().getName() : null;
	}

	public boolean meetsBindingRefinementCriteria(Object o, CaseInstance caseInstance) {
		Set<CaseParameter> subscribingParameters2 = this.subscribingParameters;
		if (source.getStandardEvent() == CREATE || source.getStandardEvent() == DELETE) {
			return true;// TODO Can't make assumptions about whether the process state contains the new/old
						// object
		}
		for (CaseParameter caseParameter : subscribingParameters2) {
			if (caseParameter.getBindingRefinementEvaluator() == null) {
				return true;
			} else {
				Object val = readBindingRefinement(caseParameter, caseInstance);
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

	protected Object readBindingRefinement(CaseParameter caseParameter, CaseInstance caseInstance) {
		try {
			ProcessContext processContext = new ProcessContext(caseInstance.getKnowledgeRuntime());
			processContext.setNodeInstance(caseInstance.getFirstNodeInstance(source.getId()));
			processContext.setProcessInstance(caseInstance);
			Object val = caseParameter.getBindingRefinementEvaluator().evaluate(processContext);
			return val;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void addParameter(CaseParameter parameter) {
		this.subscribingParameters.add(parameter);

	}

	public CaseFileItem getVariable() {
		return source.getSourceCaseFileItem();
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
		return this.caseKey;
	}

	@Override
	public long getProcessInstanceId() {
		return this.processInstanceId;
	}



}
