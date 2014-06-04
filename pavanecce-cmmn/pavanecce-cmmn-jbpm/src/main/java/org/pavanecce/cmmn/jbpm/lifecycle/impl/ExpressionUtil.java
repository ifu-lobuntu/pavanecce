package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.drools.core.process.core.Work;
import org.drools.core.spi.ProcessContext;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.Action;
import org.jbpm.process.instance.impl.ConstraintEvaluator;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.pavanecce.cmmn.jbpm.flow.ApplicabilityRule;
import org.pavanecce.cmmn.jbpm.flow.BindingRefinement;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.ItemWithDefinition;
import org.pavanecce.cmmn.jbpm.flow.ParameterMapping;
import org.pavanecce.cmmn.jbpm.flow.PlanItemControl;
import org.pavanecce.cmmn.jbpm.flow.TableItem;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;

public class ExpressionUtil {
	public static class CustomContext extends ProcessContext {
		Object source;

		public CustomContext(KieRuntime kruntime) {
			super(kruntime);
		}

		@Override
		public Object getVariable(String variableName) {
			Object variable = super.getVariable(variableName);
			if (variable == null && variableName.equals("source")) {
				return source;
			}
			return variable;
		}
	}

	/****** PlanItemControl *****/
	public static boolean isActivatedManually(NodeInstance ni, ItemWithDefinition<?> item) {
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
				VariableScopeInstance varContext = (VariableScopeInstance) ((org.jbpm.workflow.instance.NodeInstance) contextNodeInstance)
						.resolveContextInstance(VariableScope.VARIABLE_SCOPE, variable.getName());
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
				// With writing of collections, to be on the safe side, merge rather than replace
				((Collection<Object>) refinedTarget).addAll((Collection<Object>) val);
			} else {
				((Collection<Object>) refinedTarget).add(val);
			}
		} else {
			Action setterOnParent = cp.getBindingRefinement().getSetterOnParent();
			if (setterOnParent != null) {
				try {
					CustomContext pc = buildCustomContext(tpi);
					pc.source = val;
					setterOnParent.execute(pc);
				} catch (Exception e) {
					throw interpret(e);
				}

			}
		}
	}

	/* ApplicabilityRule */
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

	public static void populateSubscriptionsActivatedByParameter(SubscriptionContext sc, CaseParameter caseParameter) {
		CaseInstance processInstance = sc.getProcessInstance();
		if (caseParameter.getBindingRefinement() == null || !caseParameter.getBindingRefinement().isValid()) {
			Object var = processInstance.getVariable(caseParameter.getBoundVariable().getName());
			// TODO there is still no way to subscribe to creates and deletes here
			if (var != null) {
				sc.addSubscription(var);
			}
		} else {
			ProcessContext ctx = new ProcessContext(processInstance.getKnowledgeRuntime());
			ctx.setProcessInstance(processInstance);
			try {
				Object subscribeTo = caseParameter.getBindingRefinement().getEvaluator().evaluate(ctx);
				// Nothing to subscribe to - subscribe to parent for CREATE and DELETE events
				Object parentToSubscribeTo = caseParameter.getBindingRefinement().getParentEvaluator().evaluate(ctx);
				if (parentToSubscribeTo != null) {
					sc.addParentSubscription(caseParameter.getBoundVariable(), parentToSubscribeTo);
				}
				if (subscribeTo != null) {
					sc.addSubscription(subscribeTo);
				}
			} catch (Exception e) {
				throw interpret(e);
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

	private static ProcessContext createContext(WorkflowProcessInstance processInstance, NodeInstance tpi) {
		ProcessContext pc = new ProcessContext(((ProcessInstanceImpl) processInstance).getKnowledgeRuntime());
		pc.setProcessInstance(processInstance);
		pc.setNodeInstance(tpi);
		return pc;
	}

	private static RuntimeException interpret(Throwable e) {
		RuntimeException result;
		if (e instanceof InvocationTargetException) {
			result = interpret(((InvocationTargetException) e).getTargetException());
		} else if (e instanceof RuntimeException) {
			result = (RuntimeException) e;
		} else {
			result = new RuntimeException(e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> transformParameters(List<ParameterMapping> parameterMappings, Map<String, Object> fromParameters,
			CaseTaskInstance nodeInstance) {
		Map<String, Object> inputParameters = new HashMap<String, Object>(fromParameters);
		CustomContext ctx = buildCustomContext(nodeInstance);
		for (ParameterMapping pm : parameterMappings) {
			Object sourceValue = fromParameters.get(pm.getSourceParameterName());
			if (pm.getTransformer() != null) {
				try {
					if (sourceValue instanceof Collection) {
						Collection<Object> sourceValues = (Collection<Object>) sourceValue;
						Collection<Object> targetValues = newCollection(sourceValues);
						for (Object object : sourceValues) {
							ctx.source = object;
							targetValues.add(pm.getTransformer().evaluate(ctx));
						}
						sourceValue = targetValues;
					} else {
						ctx.source = sourceValue;
						sourceValue = pm.getTransformer().evaluate(ctx);
					}
				} catch (Exception e) {
					throw interpret(e);
				}
			}
			inputParameters.put(pm.getSourceParameterName(), sourceValue);
		}
		return inputParameters;
	}

	private static CustomContext buildCustomContext(TaskPlanItemInstance<?, ?> nodeInstance) {
		CustomContext ctx = new CustomContext(nodeInstance.getProcessInstance().getKnowledgeRuntime());
		ctx.setNodeInstance(nodeInstance);
		ctx.setProcessInstance(nodeInstance.getProcessInstance());
		return ctx;
	}

	@SuppressWarnings("unchecked")
	private static Collection<Object> newCollection(Collection<Object> sourceValues) throws InstantiationException, IllegalAccessException {
		if (Set.class.isInstance(sourceValues)) {
			return new HashSet<Object>();
		} else if (List.class.isInstance(sourceValues)) {
			return new ArrayList<Object>();
		} else if (Stack.class.isInstance(sourceValues)) {
			return new Stack<Object>();
		} else if (Deque.class.isInstance(sourceValues)) {
			return new ArrayDeque<Object>();
		} else if (List.class.isInstance(sourceValues)) {
			return new ArrayList<Object>();
		}
		return sourceValues.getClass().newInstance();
	}
}
