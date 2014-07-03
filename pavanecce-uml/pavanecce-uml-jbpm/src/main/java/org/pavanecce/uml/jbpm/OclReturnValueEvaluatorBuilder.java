package org.pavanecce.uml.jbpm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.drools.compiler.compiler.ReturnValueDescr;
import org.drools.compiler.rule.builder.PackageBuildContext;
import org.drools.core.rule.ImportDeclaration;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.ocl.expressions.CollectionKind;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;
import org.jbpm.process.builder.ProcessBuildContext;
import org.jbpm.process.builder.ReturnValueEvaluatorBuilder;
import org.jbpm.process.builder.dialect.ProcessDialectRegistry;
import org.jbpm.process.core.ContextResolver;
import org.jbpm.process.core.Process;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.impl.ReturnValueConstraintEvaluator;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemDefinitionType;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.CodePackageReference;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CodeVisibilityKind;
import org.pavanecce.uml.common.ocl.FreeExpressionContext;
import org.pavanecce.uml.common.util.EmfPackageUtil;
import org.pavanecce.uml.common.util.emulated.OclContextFactory;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;
import org.pavanecce.uml.ocl2code.creators.ExpressionCreator;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

public class OclReturnValueEvaluatorBuilder implements ReturnValueEvaluatorBuilder {
	private static final CodeTypeReference OBJECT = new CodeTypeReference(false, new CodePackageReference(null, "java", null), "Object",
			Collections.singletonMap("java", "java.lang.Object"));

	@Override
	public void build(PackageBuildContext context, ReturnValueConstraintEvaluator returnValueConstraintEvaluator, ReturnValueDescr returnValueDescr,
			ContextResolver contextResolver) {
		ProcessBuildContext ctx = (ProcessBuildContext) context;
		ResourceSet rst = (ResourceSet) ctx.getProcess().getMetaData().get(UmlBuilder.UML_RESOURCE_SET);
		Collection<Package> rootObjects = EmfPackageUtil.getRootObjects(rst);
		Package next = rootObjects.iterator().next();
		OclContextFactory factory = new OclContextFactory(rst);
		Map<String, Classifier> vars = findVariabes(returnValueDescr, ctx, rootObjects, factory);
		FreeExpressionContext oclContext = factory.getFreeOclExpressionContext(next, vars, returnValueDescr.getText());
		UmlToCodeMaps codeMaps = new UmlToCodeMaps(factory.getLibrary(), factory.getTypeResolver());
		CodeModel codeModel = new CodeModel();
		CodePackage codePackage = new CodePackage("tmp", codeModel);
		CodeClass cc = new CodeClass("tmp", codePackage);
		cc.setVisibility(CodeVisibilityKind.PACKAGE);
		CodeMethod methodToInvoke = new CodeMethod("go", OBJECT);
		for (Entry<String, Classifier> entry : vars.entrySet()) {
			methodToInvoke.addParam(entry.getKey(), codeMaps.classifierPathname(entry.getValue()));
		}
		methodToInvoke.setDeclaringClass(cc);
		ExpressionCreator ec = new ExpressionCreator(codeMaps, cc);
		CodeExpression codeExpression = ec.makeExpression(oclContext, false, methodToInvoke.getParameters());
		methodToInvoke.setResultInitialValue(codeExpression);
		JavaCodeGenerator jcg = new JavaCodeGenerator();
		for (CodeTypeReference imp : cc.getImports()) {
			ctx.getPkg().getImports().put(jcg.toQualifiedName(imp), new ImportDeclaration(jcg.toQualifiedName(imp)));
		}
		StringBuilder sb = new StringBuilder();
		sb.append(jcg.toClassDefinitionOnly(cc));
		sb.append(";\nreturn new ");
		sb.append(cc.getName());
		sb.append("().go(");
		Iterator<Entry<String, Classifier>> iterator = vars.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Classifier> entry = iterator.next();
			sb.append(entry.getKey());
			if (iterator.hasNext()) {
				sb.append(",");
			}
		}
		sb.append(");");
		returnValueDescr.setText(sb.toString());
		ProcessDialectRegistry.getDialect("java").getReturnValueEvaluatorBuilder()
				.build(context, returnValueConstraintEvaluator, returnValueDescr, contextResolver);
	}

	protected Map<String, Classifier> findVariabes(ReturnValueDescr returnValueDescr, ProcessBuildContext ctx, Collection<Package> rootObjects,
			OclContextFactory f) {
		Map<String, Classifier> vars = new HashMap<String, Classifier>();
		VariableScope variables = (VariableScope) ((Process) ctx.getProcess()).getDefaultContext(VariableScope.VARIABLE_SCOPE);
		for (Variable variable : variables.getVariables()) {
			if (variable instanceof CaseFileItem) {
				CaseFileItem cfi = (CaseFileItem) variable;
				if (cfi.getDefinition() != null && cfi.getDefinition().getDefinitionType() == CaseFileItemDefinitionType.UML_CLASS) {
					if (isVariableUsed(returnValueDescr, cfi)) {
						String structureRef = cfi.getDefinition().getStructureRef();
						Classifier type = findClassifier(rootObjects, structureRef);
						if (cfi.isCollection()) {
							type = (Classifier) f.getTypeResolver().resolveCollectionType(CollectionKind.BAG_LITERAL, type);
						}
						if (type != null) {
							vars.put(cfi.getName(), type);
						}
					}
				}
			}
		}
		return vars;
	}

	protected boolean isVariableUsed(ReturnValueDescr returnValueDescr, CaseFileItem cfi) {
		int fromIndex = 0;

		int i = -1;
		while (0 <= (i = returnValueDescr.getText().indexOf(cfi.getName(), fromIndex))) {
			if (i == 0 || (returnValueDescr.getText().charAt(i - 1) != '.' && !Character.isAlphabetic(returnValueDescr.getText().charAt(i - 1)))) {
				char charAt = returnValueDescr.getText().charAt(i + cfi.getName().length());
				boolean isPartOfOtherName = Character.isAlphabetic(charAt);
				if (!isPartOfOtherName) {
					return true;
				}
			}
			fromIndex = i + 1;
		}
		return false;
	}

	private Classifier findClassifier(Collection<Package> rootObjects, String structureRef) {
		String[] qn = structureRef.split("\\:\\:");
		for (Namespace ns : rootObjects) {
			Classifier result = tryToMatch(qn, ns);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private Classifier tryToMatch(String[] qn, Namespace ns) {
		Classifier result = null;
		if (qn[0].equals(ns.getName())) {
			for (int i = 1; i < qn.length; i++) {
				if (ns == null) {
					result = null;
				} else {
					ns = (Namespace) ns.getMember(qn[i]);
				}
			}
			result = (Classifier) ns;
		}
		return result;
	}
}
