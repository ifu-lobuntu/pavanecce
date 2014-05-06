package org.pavanecce.uml.jbpm;

import java.util.Collections;

import org.drools.compiler.compiler.ReturnValueDescr;
import org.drools.compiler.rule.builder.PackageBuildContext;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Package;
import org.jbpm.process.builder.ProcessBuildContext;
import org.jbpm.process.builder.ReturnValueEvaluatorBuilder;
import org.jbpm.process.core.ContextResolver;
import org.jbpm.process.instance.impl.ReturnValueConstraintEvaluator;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.uml.common.ocl.FreeExpressionContext;
import org.pavanecce.uml.common.util.EmfPackageUtil;
import org.pavanecce.uml.common.util.emulated.OclContextFactory;
import org.pavanecce.uml.ocltocode.OclCodeBuilder;
import org.pavanecce.uml.ocltocode.common.UmlToCodeMaps;
import org.pavanecce.uml.ocltocode.creators.ExpressionCreator;

public class OclReturnValueEvaluatorBuilder implements ReturnValueEvaluatorBuilder {

	@Override
	public void build(PackageBuildContext context, ReturnValueConstraintEvaluator returnValueConstraintEvaluator, ReturnValueDescr returnValueDescr, ContextResolver contextResolver) {
		ProcessBuildContext ctx= (ProcessBuildContext) context;
		ResourceSet rst= (ResourceSet) ctx.getProcess().getMetaData().get("ResourceSet");
		Package next = EmfPackageUtil.getRootObjects(rst).iterator().next();
		OclContextFactory f = new OclContextFactory(rst);
		FreeExpressionContext oclContext = f.getFreeOclExpressionContext(next, returnValueDescr.getText());
		UmlToCodeMaps codeMaps = new UmlToCodeMaps(false);
		ExpressionCreator ec = new ExpressionCreator(codeMaps, new CodeClass("asdf", new CodePackage("asdf",null)));
		CodeExpression codeExpression = ec.makeExpression(oclContext, false, Collections.<CodeParameter> emptyList());
		//Get models/resourceset from context
		//Create environment where process variables are implicit
		//generate method where process variables are used - pass them to constructor of a local inner class
		//Generate the code for the local inner class
	}

}
