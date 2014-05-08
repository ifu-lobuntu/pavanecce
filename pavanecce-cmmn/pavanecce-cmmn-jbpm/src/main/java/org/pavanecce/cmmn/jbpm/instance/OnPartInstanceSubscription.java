package org.pavanecce.cmmn.jbpm.instance;

import java.util.Collection;

import org.drools.core.spi.ProcessContext;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;

public class OnPartInstanceSubscription implements CaseFileItemSubscriptionInfo {
	OnPartInstance source;
	private CaseParameter subscribingParameter;

	public OnPartInstanceSubscription(OnPartInstance source, CaseParameter caseParameter) {
		super();
		this.subscribingParameter = caseParameter;
		this.source = source;
	}

	public OnPartInstance getSource() {
		return source;
	}

	@Override
	public String getItemName() {
		return ((CaseFileItemSubscriptionInfo) source.getOnPart()).getItemName();
	}

	@Override
	public CaseFileItemTransition getTransition() {
		return ((CaseFileItemSubscriptionInfo) source.getOnPart()).getTransition();
	}

	@Override
	public String getRelatedItemName() {
		return ((CaseFileItemSubscriptionInfo) source.getOnPart()).getRelatedItemName();
	}

	public CaseParameter getSubscribingParameter() {
		return subscribingParameter;
	}
	public boolean meetsBindingRefinementCriteria(Object o){
		if(subscribingParameter.getBindingRefinementEvaluator()==null){
			return true;
		}else{
			Object val = readBindingRefinement();
			if(val instanceof Collection){
				return ((Collection<?>) val).contains(o);
			}else if(val!=null){
				return val.equals(o);
			}else{
				//Only creates allow null "old" value
				return getTransition()==CaseFileItemTransition.CREATE;
			}
		}
	}

	protected Object readBindingRefinement()  {
		try {
			ProcessContext processContext = new ProcessContext(source.getProcessInstance().getKnowledgeRuntime());
			processContext.setNodeInstance(source);
			processContext.setProcessInstance(source.getProcessInstance());
			Object val = subscribingParameter.getBindingRefinementEvaluator().evaluate(processContext);
			return val;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
