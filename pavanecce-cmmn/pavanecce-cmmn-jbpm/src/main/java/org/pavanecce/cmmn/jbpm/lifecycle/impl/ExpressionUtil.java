package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.drools.core.process.core.Work;
import org.drools.core.spi.ProcessContext;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.Action;
import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.pavanecce.cmmn.jbpm.flow.ApplicabilityRule;
import org.pavanecce.cmmn.jbpm.flow.BindingRefinement;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.ItemWithDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItemControl;
import org.pavanecce.cmmn.jbpm.flow.TableItem;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;

public class ExpressionUtil {
	/****** PlanItemControl *****/
	public static boolean isActivatedManually(NodeInstance ni, ItemWithDefinition<?> item)  {
		boolean isActivatedManually = true;
		PlanItemControl itemControl = item.getEffectiveItemControl();
		if (itemControl != null && itemControl.getManualActivationRule() instanceof ConstraintEvaluator) {
			ConstraintEvaluator ev = (ConstraintEvaluator) itemControl.getManualActivationRule();
			isActivatedManually = ev.evaluate((org.jbpm.workflow.instance.NodeInstance) ni, null, ev);
		}
		return isActivatedManually;
	}

	public static boolean isRepeating(NodeInstance contextNodeInstance, ItemWithDefinition<?> item) {
		PlanItemControl itemControl = item.getEffectiveItemControl();
		if (itemControl != null && itemControl.getRepetitionRule() instanceof ConstraintEvaluator) {
			ConstraintEvaluator constraintEvaluator = (ConstraintEvaluator) itemControl.getRepetitionRule();
			return constraintEvaluator.evaluate((org.jbpm.workflow.instance.NodeInstance) contextNodeInstance, null, constraintEvaluator);
		} else {
			return false;
		}

	}

	public static Boolean isRequired(ItemWithDefinition<?> item, org.jbpm.workflow.instance.NodeInstance contextNodeInstance) {
		Boolean isPlanItemInstanceRequired;
		PlanItemControl itemControl = item.getEffectiveItemControl();
		if (itemControl != null && itemControl.getRequiredRule() instanceof ConstraintEvaluator) {
			ConstraintEvaluator constraintEvaluator = (ConstraintEvaluator) itemControl.getRequiredRule();
			isPlanItemInstanceRequired = constraintEvaluator.evaluate(contextNodeInstance, null, constraintEvaluator);
		} else {
			isPlanItemInstanceRequired = Boolean.FALSE;
		}
		return isPlanItemInstanceRequired;
	}

	/** CaseParameter **/
	public static HashMap<String, Object> buildInputParameters(Work work, NodeInstance contextNodeInstance, TaskDefinition taskDefinition) {
		HashMap<String, Object> parameters = new HashMap<String, Object>(work.getParameters());
		for (CaseParameter cp : taskDefinition.getInputs()) {
			BindingRefinement br = cp.getBindingRefinement();
			if (br != null && br.isValid()) {
				try {
					ProcessContext ctx = createContext(contextNodeInstance);
					parameters.put(cp.getName(), br.getEvaluator().evaluate(ctx));
				} catch (Exception e) {
					throw interpret(e);
				}
			} else {
				CaseFileItem variable = cp.getBoundVariable();
				VariableScopeInstance varContext = (VariableScopeInstance) ((org.jbpm.workflow.instance.NodeInstance) contextNodeInstance).resolveContextInstance(VariableScope.VARIABLE_SCOPE, variable.getName());
				parameters.put(cp.getName(), varContext.getVariable(variable.getName()));
			}
		}
		return parameters;
	}

	public static Object readFromBindingRefinement(CaseParameter cp, CaseInstance ci, NodeInstance ni) {
		Object variable = ci.getVariable(cp.getBoundVariable().getName());
		BindingRefinement br = cp.getBindingRefinement();
		if (br != null && br.isValid()) {
			ProcessContext processContext = createContext(ci, ni);
			try {
				variable = br.getEvaluator().evaluate(processContext);
			} catch (Exception e) {
				throw interpret(e);
			}
		}
		return variable;
	}

	@SuppressWarnings("unchecked")
	public static void writeToBindingRefinement(TaskPlanItemInstance<?, ?> tpi, CaseParameter cp, Object val) {
		Object refinedTarget = readFromBindingRefinement(cp, tpi.getCaseInstance(), tpi);
		if (refinedTarget instanceof Collection) {
			if (val instanceof Collection) {
				((Collection<Object>) refinedTarget).addAll((Collection<Object>) val);
			} else {
				((Collection<Object>) refinedTarget).add(val);
			}
		} else {
			Action setterOnParent = cp.getBindingRefinement().getSetterOnParent();
			if (setterOnParent != null) {
				try {
					ProcessContext pc = createContext(tpi);
					setterOnParent.execute(pc);
				} catch (Exception e) {
					throw interpret(e);
				}

			}
		}
	}

	/*ApplicabilityRule*/
	public static boolean isApplicable(TableItem ti, org.kie.api.runtime.process.NodeInstance ni) {
		if (ti.getApplicabilityRules().isEmpty()) {
			return true;
		} else {
			Collection<ApplicabilityRule> values = ti.getApplicabilityRules().values();
			// Implemented as AND
			for (ApplicabilityRule ar : values) {
				if (ar.getCondition() instanceof ConstraintEvaluator) {
					ConstraintEvaluator ce = (ConstraintEvaluator) ar.getCondition();
					if (!ce.evaluate((org.jbpm.workflow.instance.NodeInstance) ni, null, ce)) {
						return false;
					}
				}
			}
			return true;
		}
	}


	@SuppressWarnings("unchecked")
	public static void populateSubscriptionsActivatedByParameter(SubscriptionContext sc, CaseParameter caseParameter) {
		Map<CaseFileItem, Collection<Object>> parentSubscriptions = sc.getParentSubscriptions();
		Collection<Object> subscriptions = sc.getSubscriptions();
		CaseInstance processInstance = sc.getProcessInstance();
		if (caseParameter.getBindingRefinement() == null || !caseParameter.getBindingRefinement().isValid()) {
			Object var = processInstance.getVariable(caseParameter.getBoundVariable().getName());
			if (var instanceof Collection) {
				subscriptions.addAll((Collection<? extends Object>) var);
			} else if (var != null) {
				subscriptions.add(var);
			}
		} else {
			ProcessContext ctx = new ProcessContext(processInstance.getKnowledgeRuntime());
			ctx.setProcessInstance(processInstance);
			try {
				Object subscribeTo = caseParameter.getBindingRefinement().getEvaluator().evaluate(ctx);
				if ((subscribeTo instanceof Collection && ((Collection<?>) subscribeTo).isEmpty()) || subscribeTo == null) {
					// Nothing to subscribe to - subscribe to parent for CREATE and DELETE events
					Object parentToSubscribeTo = caseParameter.getBindingRefinement().getParentEvaluator().evaluate(ctx);
					if (parentToSubscribeTo != null) {
						Collection<Object> collection = parentSubscriptions.get(caseParameter.getBoundVariable());
						if (collection == null) {
							parentSubscriptions.put(caseParameter.getBoundVariable(), collection = new HashSet<Object>());
						}
						if (parentToSubscribeTo instanceof Collection) {
							collection.addAll((Collection<? extends Object>) parentToSubscribeTo);
						} else {
							collection.add(parentToSubscribeTo);
						}
					}
				} else if (subscribeTo instanceof Collection) {
					subscriptions.addAll((Collection<?>) subscribeTo);
				} else if (subscribeTo != null) {
					subscriptions.add(subscribeTo);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void populateSubscriptionsActivatedByParameters(SubscriptionContext sc, Collection<CaseParameter> subscribingParameters) {
		for (CaseParameter caseParameter : subscribingParameters) {
			populateSubscriptionsActivatedByParameter(sc, caseParameter);
		}
	}

	private static ProcessContext createContext(NodeInstance tpi) {
		return createContext(tpi.getProcessInstance(), tpi);
	}

	public static ProcessContext createContext(WorkflowProcessInstance processInstance, NodeInstance tpi) {
		ProcessContext pc = new ProcessContext(((ProcessInstanceImpl) processInstance).getKnowledgeRuntime());
		pc.setProcessInstance(processInstance);
		pc.setNodeInstance(tpi);
		return pc;
	}

	private static RuntimeException interpret(Exception e) {
		RuntimeException result;
		if (e instanceof RuntimeException) {
			result = (RuntimeException) e;
		} else {
			result = new RuntimeException(e);
		}
		return result;
	}
}
