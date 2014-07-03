package org.pavanecce.uml.ocl2code.creators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ocl.uml.AssociationClassCallExp;
import org.eclipse.ocl.uml.CallExp;
import org.eclipse.ocl.uml.CollectionType;
import org.eclipse.ocl.uml.FeatureCallExp;
import org.eclipse.ocl.uml.IterateExp;
import org.eclipse.ocl.uml.IteratorExp;
import org.eclipse.ocl.uml.LoopExp;
import org.eclipse.ocl.uml.OCLExpression;
import org.eclipse.ocl.uml.OperationCallExp;
import org.eclipse.ocl.uml.PropertyCallExp;
import org.eclipse.uml2.uml.Property;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CodeVisibilityKind;
import org.pavanecce.common.code.metamodel.CollectionTypeReference;
import org.pavanecce.common.code.metamodel.expressions.IsNullExpression;
import org.pavanecce.common.code.metamodel.expressions.MethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.NewInstanceExpression;
import org.pavanecce.common.code.metamodel.expressions.NotExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.common.code.metamodel.expressions.StaticMethodCallExpression;
import org.pavanecce.common.code.metamodel.statements.CodeForStatement;
import org.pavanecce.common.code.metamodel.statements.CodeIfStatement;
import org.pavanecce.common.code.metamodel.statements.MethodCallStatement;
import org.pavanecce.uml.common.ocl.AbstractOclContext;
import org.pavanecce.uml.ocl2code.common.EmfPropertyCallHelper;
import org.pavanecce.uml.ocl2code.common.ExpGeneratorHelper;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;
import org.pavanecce.uml.ocl2code.maps.NavToAssocClassMap;
import org.pavanecce.uml.ocl2code.maps.PropertyMap;
import org.pavanecce.uml.uml2code.StdlibMap;

public class PropCallCreator {
	private CodeClassifier myClass = null;
	private ExpGeneratorHelper expGeneratorHelper;
	private UmlToCodeMaps codeMaps;
	private AbstractOclContext context;

	public PropCallCreator(ExpGeneratorHelper e, CodeClassifier myClass, AbstractOclContext context) {
		super();
		expGeneratorHelper = e;
		this.myClass = myClass;
		this.codeMaps = e.CodeUtil;
		this.context = context;
	}

	public CodeExpression makeExpression(CallExp in, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		return privMakeExpNode(in, source, isStatic, params);
	}

	public CodeExpression makeExpressionNode(CallExp in, boolean isStatic, List<CodeParameter> params) {
		return privMakeExpNode(in, null, isStatic, params);
	}

	private CodeExpression privMakeExpNode(CallExp in, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		if (in instanceof LoopExp) {
			if (in instanceof IterateExp) {
				IterationExpressionCreator maker = new IterationExpressionCreator(expGeneratorHelper, myClass, context);
				return maker.iterateExp((IterateExp) in, source, isStatic, params);
			} else if (in instanceof IteratorExp) {
				IterationExpressionCreator maker = new IterationExpressionCreator(expGeneratorHelper, myClass, context);
				return maker.makeIteratorExpression((IteratorExp) in, source, isStatic, params);
			}
		} else if (in instanceof FeatureCallExp) {
			if (in instanceof PropertyCallExp) {
				return attributeCallExp((PropertyCallExp) in, source, params);
			} else if (in instanceof AssociationClassCallExp) {
				return associationClassCallExp((AssociationClassCallExp) in, source);
			} else if (in instanceof OperationCallExp) {
				OperationCallCreator maker = new OperationCallCreator(expGeneratorHelper, myClass, context);
				return maker.operationCallExp((OperationCallExp) in, source, isStatic, params);
			}
		}
		return source;
	}

	private CodeExpression attributeCallExp(PropertyCallExp exp, CodeExpression source, List<CodeParameter> params) {
		Property prop = exp.getReferredProperty();
		PropertyMap mapper = codeMaps.buildStructuralFeatureMap(prop);
		String getterName = mapper.getter();
		if (prop.isStatic() || source == null) {
			return new StaticMethodCallExpression(mapper.javaBaseDefaultTypePath(), getterName);
		} else {
			List<CodeExpression> qArgs = new ArrayList<CodeExpression>();
			if (exp.getQualifier().size() > 0) {
				ExpressionCreator myExpMaker = new ExpressionCreator(codeMaps, myClass, context);
				Iterator<?> it = exp.getQualifier().iterator();
				while (it.hasNext()) {
					OCLExpression arg = (OCLExpression) it.next();
					qArgs.add(myExpMaker.makeExpression(arg, false, params));
				}
			}
			if (!(exp.getSource().getType() instanceof CollectionType) && EmfPropertyCallHelper.resultsInMany((OCLExpression) exp.getSource())) {
				CodeTypeReference bag = StdlibMap.javaBagType.getCopy();
				bag.addToElementTypes(mapper.javaBaseTypePath());
				CodeMethod oper = new CodeMethod("collect" + myClass.getUniqueNumber(), bag);
				CodeTypeReference paramType = StdlibMap.javaBagType.getCopy();
				oper.addParam("source", paramType);
				oper.setDeclaringClass(myClass);
				oper.setVisibility(CodeVisibilityKind.PRIVATE);
				CodeTypeReference sourceType = codeMaps.classifierPathname(exp.getSource().getType());
				paramType.addToElementTypes(sourceType);
				CollectionTypeReference bagImpl = StdlibMap.javaBagImplType.getCopy();
				bagImpl.addToElementTypes(mapper.javaBaseTypePath());
				oper.setResultInitialValue(new NewInstanceExpression(bagImpl));
				CodeForStatement foreach = new CodeForStatement(oper.getBody(), "el", sourceType, new PortableExpression("source"));
				MethodCallExpression getterCall = new MethodCallExpression("el." + mapper.getter(), qArgs);
				if (prop.getQualifiers().size() > 0 && exp.getQualifier().isEmpty() || prop.isMultivalued()) {
					new MethodCallStatement(foreach.getBody(), "result.addAll", getterCall);
				} else {
					CodeIfStatement ifNotNull = new CodeIfStatement(foreach.getBody(), new NotExpression(new IsNullExpression(getterCall)));
					new MethodCallStatement(ifNotNull.getThenBlock(), "result.add", getterCall);
				}
				return new MethodCallExpression(oper.getName(), source);
			} else {
				return new MethodCallExpression(source, mapper.getter(), qArgs);
			}
		}
	}

	private CodeExpression associationClassCallExp(AssociationClassCallExp exp, CodeExpression source) {
		Property navSource = exp.getNavigationSource();
		NavToAssocClassMap mapper = new NavToAssocClassMap(navSource);
		return new MethodCallExpression(source, mapper.getter());
	}
}
