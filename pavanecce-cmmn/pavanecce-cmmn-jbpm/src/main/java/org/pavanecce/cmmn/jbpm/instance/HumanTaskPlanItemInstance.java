package org.pavanecce.cmmn.jbpm.instance;

import org.drools.core.process.instance.WorkItem;
import org.drools.core.spi.ProcessContext;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.ReturnValueEvaluator;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;

public class HumanTaskPlanItemInstance extends WorkItemNodeInstance implements PlanItemInstanceWithTask{
	private static final long serialVersionUID = 3200294767777991641L;

	@Override
	public ContextInstance resolveContextInstance(String contextId, Object param) {

		final ContextInstance result = super.resolveContextInstance(contextId, param);
		if (contextId.equals(VariableScope.VARIABLE_SCOPE)) {
			//TODO make caseParameters available??
			return new CustomVariableScopeInstance(result);
		}
		return result;
	}

	@Override
	protected WorkItem createWorkItem(WorkItemNode workItemNode) {
		WorkItem result = super.createWorkItem(workItemNode);
		PlanItem pi = (PlanItem) getNode();
		for (CaseParameter cp : ((TaskDefinition) pi.getPlanInfo().getDefinition()).getInputs()) {
			ReturnValueEvaluator el = cp.getBindingRefinementEvaluator();
			if (el != null) {
				try {
					ProcessContext ctx = new ProcessContext(getProcessInstance().getKnowledgeRuntime());
					ctx.setNodeInstance(this);
					result.setParameter(cp.getName(), el.evaluate(ctx));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				CaseFileItem variable = cp.getBoundVariable();
				VariableScopeInstance varContext = (VariableScopeInstance) resolveContextInstance(VariableScope.VARIABLE_SCOPE, variable.getName());
				result.setParameter(cp.getName(), varContext.getVariable(variable.getName()));
			}
		}
		return result;
	}
	@Override
	public void triggerCompleted() {
		super.triggerCompleted();
		((CaseInstance)getProcessInstance()).markSubscriptionsForUpdate();
	}
	@Override
	public void cancel() {
		super.cancel();
		((CaseInstance)getProcessInstance()).markSubscriptionsForUpdate();
	}
	@Override
	public void internalTrigger(NodeInstance from, String type) {
		super.internalTrigger(from, type);
		((CaseInstance)getProcessInstance()).markSubscriptionsForUpdate();
	}

}
