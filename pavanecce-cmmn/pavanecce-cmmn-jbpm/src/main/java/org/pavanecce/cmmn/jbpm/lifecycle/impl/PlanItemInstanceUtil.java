package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Collection;
import java.util.HashMap;

import org.drools.core.process.core.Work;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.drools.core.spi.ProcessContext;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.jbpm.process.instance.impl.ReturnValueEvaluator;
import org.jbpm.workflow.instance.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.DiscretionaryItem;
import org.pavanecce.cmmn.jbpm.flow.ItemWithDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;
import org.pavanecce.cmmn.jbpm.lifecycle.ControllableItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.ItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementLifecycleWithTask;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstanceContainerLifecycle;

class PlanItemInstanceUtil {
	public static boolean canComplete(PlanItemInstanceContainerLifecycle container) {
		Collection<? extends ItemInstanceLifecycle<?>> nodeInstances = container.getChildren();
		for (ItemInstanceLifecycle<?> nodeInstance : nodeInstances) {
			if (nodeInstance instanceof PlanItemInstanceFactoryNodeInstance && ((PlanItemInstanceFactoryNodeInstance<?>) nodeInstance).isPlanItemInstanceStillRequired()) {
				return false;
			} else if (nodeInstance instanceof MilestonePlanItemInstance && ((MilestonePlanItemInstance) nodeInstance).isCompletionStillRequired()) {
				return false;
			} else if (nodeInstance instanceof ControllableItemInstanceLifecycle && ((ControllableItemInstanceLifecycle<?>) nodeInstance).isCompletionStillRequired()) {
				return false;
			}

		}
		return true;

	}

	public static HashMap<String, Object> buildParameters(Work work, NodeInstance contextNodeInstance, TaskDefinition taskDefinition) {
		HashMap<String, Object> parameters = new HashMap<String, Object>(work.getParameters());
		CaseInstance caseInstance = (CaseInstance) contextNodeInstance.getProcessInstance();
		for (CaseParameter cp : taskDefinition.getInputs()) {
			ReturnValueEvaluator el = cp.getBindingRefinementEvaluator();
			if (el != null) {
				try {
					ProcessContext ctx = new ProcessContext(caseInstance.getKnowledgeRuntime());
					ctx.setNodeInstance(contextNodeInstance);
					ctx.setProcessInstance(caseInstance);
					parameters.put(cp.getName(), el.evaluate(ctx));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				CaseFileItem variable = cp.getBoundVariable();
				VariableScopeInstance varContext = (VariableScopeInstance) contextNodeInstance.resolveContextInstance(VariableScope.VARIABLE_SCOPE, variable.getName());
				parameters.put(cp.getName(), varContext.getVariable(variable.getName()));
			}
		}
		if (caseInstance.getCaseOwner() != null) {
			parameters.put(Case.INITIATOR, caseInstance.getCaseOwner());
		} else if (caseInstance.getInitiator() != null) {
			parameters.put(Case.INITIATOR, caseInstance.getInitiator());
		}
		return parameters;
	}

	public static WorkItemImpl createWorkItem(Work work, org.jbpm.workflow.instance.NodeInstance contextNodeInstance, PlanItemDefinition definition) {
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setName(work.getName());
		workItem.setProcessInstanceId(contextNodeInstance.getProcessInstance().getId());
		workItem.setParameters(new HashMap<String, Object>(work.getParameters()));
		if (definition instanceof TaskDefinition) {
			workItem.getParameters().putAll(buildParameters(work, contextNodeInstance, (TaskDefinition) definition));
		}
		CaseInstance caseInstance = (CaseInstance) contextNodeInstance.getProcessInstance();
		String deploymentId = (String) caseInstance.getKnowledgeRuntime().getEnvironment().get("deploymentId");
		workItem.setDeploymentId(deploymentId);
		workItem.setParameter(PlanElementLifecycleWithTask.COMMENT, definition.getDescription());
		if (contextNodeInstance.getNodeInstanceContainer() instanceof PlanElementLifecycleWithTask) {
			long parentWorkItemId = ((PlanElementLifecycleWithTask) contextNodeInstance.getNodeInstanceContainer()).getWorkItemId();
			if (parentWorkItemId >= 0) {
				workItem.setParameter(PlanElementLifecycleWithTask.PARENT_WORK_ITEM_ID, parentWorkItemId);
			}
		}

		return workItem;
	}

	public static Boolean isRequired(ItemWithDefinition<?> planItem, org.jbpm.workflow.instance.NodeInstance contextNodeInstance) {
		Boolean isPlanItemInstanceRequired; 
		if (planItem.getItemControl() != null && planItem.getItemControl().getRequiredRule() instanceof ConstraintEvaluator) {
			ConstraintEvaluator constraintEvaluator = (ConstraintEvaluator) planItem.getItemControl().getRequiredRule();
			isPlanItemInstanceRequired = constraintEvaluator.evaluate( contextNodeInstance, null, constraintEvaluator);
		} else {
			isPlanItemInstanceRequired = Boolean.FALSE;
		}
		return isPlanItemInstanceRequired;
	}

}
