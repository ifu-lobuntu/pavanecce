package org.pavanecce.cmmn.jbpm.instance.impl;

import java.util.HashMap;

import org.drools.core.process.core.Work;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.drools.core.spi.ProcessContext;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.ReturnValueEvaluator;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;

public abstract class TaskPlanItemInstance <T extends TaskDefinition> extends AbstractControllablePlanInstance<T> {

	private static final long serialVersionUID = -2759757105782259528L;

	public TaskPlanItemInstance() {
		super();
	}
	protected abstract Work getWork();

	@Override
	protected void createWorkItem() {
		Work work = getWork();
		workItem = new WorkItemImpl();
		workItem.setName(work.getName());
		workItem.setProcessInstanceId(getProcessInstance().getId());
		HashMap<String, Object> parameters = buildParameters(work);
		workItem.setParameters(parameters);
	}
	protected HashMap<String, Object> buildParameters(Work work) {
		HashMap<String, Object> parameters = new HashMap<String, Object>(work.getParameters());

		for (CaseParameter cp : ((TaskDefinition) getPlanItem().getPlanInfo().getDefinition()).getInputs()) {
			ReturnValueEvaluator el = cp.getBindingRefinementEvaluator();
			if (el != null) {
				try {
					ProcessContext ctx = new ProcessContext(getProcessInstance().getKnowledgeRuntime());
					ctx.setNodeInstance(this);
					parameters.put(cp.getName(), el.evaluate(ctx));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				CaseFileItem variable = cp.getBoundVariable();
				VariableScopeInstance varContext = (VariableScopeInstance) resolveContextInstance(VariableScope.VARIABLE_SCOPE, variable.getName());
				parameters.put(cp.getName(), varContext.getVariable(variable.getName()));
			}
		}
		return parameters;
	}

}