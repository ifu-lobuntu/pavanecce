package org.pavanecce.cmmn.jbpm.flow.builder;

import org.drools.compiler.compiler.ReturnValueDescr;
import org.drools.compiler.lang.descr.ProcessDescr;
import org.jbpm.process.builder.ProcessBuildContext;
import org.jbpm.process.builder.ProcessNodeBuilder;
import org.jbpm.process.builder.ReturnValueEvaluatorBuilder;
import org.jbpm.process.builder.dialect.ProcessDialectRegistry;
import org.jbpm.process.instance.impl.ReturnValueConstraintEvaluator;
import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;

public class PlanItemBuilder implements ProcessNodeBuilder {

	@Override
	public void build(Process process, ProcessDescr processDescr, ProcessBuildContext context, Node node) {
		PlanItem item = (PlanItem) node;
		PlanItemDefinition def = item.getPlanInfo().getDefinition();
		for (CaseParameter cp : def.getInputs()) {
			Constraint constraint = cp.getBindingRefinement();
			cp.setBindingRefinement(build(context, node, constraint));            
		}

	}

	public static ReturnValueConstraintEvaluator build(ProcessBuildContext context, Node node, Constraint constraint) {
		if (constraint != null && !(constraint instanceof ReturnValueConstraintEvaluator)) {
			ReturnValueConstraintEvaluator returnValueConstraint = new ReturnValueConstraintEvaluator();
		    returnValueConstraint.setDialect( constraint.getDialect() );
		    returnValueConstraint.setName( constraint.getName() );
		    returnValueConstraint.setPriority( constraint.getPriority() );
		    returnValueConstraint.setDefault( constraint.isDefault() );
		    ReturnValueDescr returnValueDescr = new ReturnValueDescr();
		    returnValueDescr.setText( constraint.getConstraint() );
			ReturnValueEvaluatorBuilder builder = ProcessDialectRegistry.getDialect(constraint.getDialect()).getReturnValueEvaluatorBuilder();
			builder.build(context, returnValueConstraint, returnValueDescr, (NodeImpl)node);
			constraint= returnValueConstraint;
		}
		return (ReturnValueConstraintEvaluator) constraint;
	}

}
