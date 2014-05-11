package org.pavanecce.cmmn.jbpm.flow.builder;

import java.util.List;

import org.drools.compiler.compiler.ReturnValueDescr;
import org.drools.compiler.lang.descr.ProcessDescr;
import org.jbpm.process.builder.ProcessBuildContext;
import org.jbpm.process.builder.ProcessNodeBuilder;
import org.jbpm.process.builder.ReturnValueEvaluatorBuilder;
import org.jbpm.process.builder.dialect.ProcessDialectRegistry;
import org.jbpm.process.instance.impl.ReturnValueConstraintEvaluator;
import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.impl.ConstraintImpl;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.CaseTask;
import org.pavanecce.cmmn.jbpm.flow.ParameterMapping;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;

public class PlanItemBuilder implements ProcessNodeBuilder {

	@Override
	public void build(Process process, ProcessDescr processDescr, ProcessBuildContext context, Node node) {
		processCaseInputParameters(process, context); // TODO find a better place to do this?
		PlanItem item = (PlanItem) node;
		PlanItemDefinition def = item.getPlanInfo().getDefinition();
		if (def instanceof TaskDefinition) {
			processParameters(context, node, ((TaskDefinition) def).getInputs());
			processParameters(context, node, ((TaskDefinition) def).getOutputs());
			if(def instanceof CaseTask){
				for (ParameterMapping pm : ((CaseTask) def).getParameterMappings()) {
					pm.setTransformation(build(context, node, pm.getTransformation()));
				}
			}
		}
	}

	protected void processCaseInputParameters(Process process, ProcessBuildContext context) {
		Case case1 = (Case) process;
		processParameters(context, case1.getDefaultStart(), case1.getInputParameters());
		processParameters(context, case1.getDefaultStart(), case1.getOutputParameters());
	}

	private void processParameters(ProcessBuildContext context, Node node, List<CaseParameter> inputs2) {
		for (CaseParameter cp : inputs2) {
			Constraint constraint = cp.getBindingRefinement();
			if (constraint != null && !(constraint instanceof ReturnValueConstraintEvaluator) && cp.getBindingRefinementParent() == null) {
				String parentExpression = getParentExpression(cp, constraint);
				if (parentExpression != null) {
					Constraint parentConstraint = new ConstraintImpl();
					parentConstraint.setDialect(constraint.getDialect());
					parentConstraint.setConstraint(parentExpression);
					parentConstraint.setDefault(constraint.isDefault());
					parentConstraint.setPriority(constraint.getPriority());
					parentConstraint.setType(constraint.getType());
					parentConstraint.setDialect(constraint.getDialect());
					cp.setBindingRefinementParent(build(context, node, parentConstraint));
				}
			}
			cp.setBindingRefinement(build(context, node, constraint));
		}
	}

	public static String getParentExpression(CaseParameter cp, Constraint constraint) {
		// TODO make this strategy configurable by dialect
		return getParentExpression(constraint.getConstraint(), cp.getBoundVariable().getName());
	}

	protected static String getParentExpression(String constraintText, String varName) {
		boolean isExpressedOnParentInMvelOrOcl = constraintText.endsWith(varName);
		if (isExpressedOnParentInMvelOrOcl) {
			return constraintText.substring(0, constraintText.length() - varName.length() - 1);
		} else {
			String getter = "get" + Character.toUpperCase(varName.charAt(0)) + varName.substring(1) + "();";
			boolean isExpressedOnParentInJava = constraintText.endsWith(getter);
			if (isExpressedOnParentInJava) {
				return constraintText.substring(0, constraintText.length() - getter.length() - 1) + ";";
			}
		}
		return null;
	}

	public static ReturnValueConstraintEvaluator build(ProcessBuildContext context, Node node, Constraint constraint) {
		if (constraint != null && !(constraint instanceof ReturnValueConstraintEvaluator)) {
			ReturnValueConstraintEvaluator returnValueConstraint = new ReturnValueConstraintEvaluator();
			returnValueConstraint.setDialect(constraint.getDialect());
			returnValueConstraint.setName(constraint.getName());
			returnValueConstraint.setPriority(constraint.getPriority());
			returnValueConstraint.setDefault(constraint.isDefault());
			ReturnValueDescr returnValueDescr = new ReturnValueDescr();
			returnValueDescr.setText(constraint.getConstraint());
			ReturnValueEvaluatorBuilder builder = ProcessDialectRegistry.getDialect(constraint.getDialect()).getReturnValueEvaluatorBuilder();
			builder.build(context, returnValueConstraint, returnValueDescr, (NodeImpl) node);
			constraint = returnValueConstraint;
		}
		return (ReturnValueConstraintEvaluator) constraint;
	}

}
