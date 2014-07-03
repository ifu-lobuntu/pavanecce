package org.pavanecce.uml.ocl2code.creators;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.uml.CallExp;
import org.eclipse.ocl.uml.IfExp;
import org.eclipse.ocl.uml.LetExp;
import org.eclipse.ocl.uml.LiteralExp;
import org.eclipse.ocl.uml.MessageExp;
import org.eclipse.ocl.uml.OCLExpression;
import org.eclipse.ocl.uml.TypeExp;
import org.eclipse.ocl.uml.Variable;
import org.eclipse.ocl.uml.VariableExp;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.TimeExpression;
import org.eclipse.uml2.uml.Transition;
import org.pavanecce.common.code.metamodel.CodeBehaviour;
import org.pavanecce.common.code.metamodel.CodeBlock;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CodeVisibilityKind;
import org.pavanecce.common.code.metamodel.expressions.NullExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.common.code.metamodel.expressions.TernaryExpression;
import org.pavanecce.common.code.metamodel.statements.SetResultStatement;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.ocl.AbstractOclContext;
import org.pavanecce.uml.common.ocl.PropertyOfImplicitObject;
import org.pavanecce.uml.common.util.EmfClassifierUtil;
import org.pavanecce.uml.common.util.EmfElementFinder;
import org.pavanecce.uml.ocl2code.common.ExpGeneratorHelper;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;
import org.pavanecce.uml.ocl2code.maps.ClassifierMap;
import org.pavanecce.uml.ocl2code.maps.PropertyMap;

public class ExpressionCreator {
	private CodeClassifier myClass = null;
	UmlToCodeMaps codeMaps;
	ExpGeneratorHelper expGeneratorHelper;
	private AbstractOclContext context;

	ExpressionCreator(UmlToCodeMaps codeMaps, CodeClassifier myOwner, AbstractOclContext context) {
		super();
		this.context = context;
		this.codeMaps = codeMaps;
		this.expGeneratorHelper = new ExpGeneratorHelper(codeMaps);
		this.myClass = myOwner;
	}

	public ExpressionCreator(UmlToCodeMaps codeMaps, CodeClassifier myClass2) {
		this.codeMaps = codeMaps;
		this.expGeneratorHelper = new ExpGeneratorHelper(codeMaps);
		this.myClass = myClass2;
	}

	public CodeExpression makeExpression(AbstractOclContext in, boolean isStatic, List<CodeParameter> params) {
		context = in;
		return makeExpression(in.getExpression(), isStatic, params);
	}

	CodeExpression makeExpression(OCLExpression in, boolean isStatic, List<CodeParameter> params) {
		if (in instanceof IfExp) {
			return makeIfExpression((IfExp) in, isStatic, params);
		} else if (in instanceof LetExp) {
			return makeLetExpression((LetExp) in, isStatic, params);
		} else if (in instanceof LiteralExp) {
			LiteralExpCreator maker = new LiteralExpCreator(codeMaps, myClass, context);
			return maker.makeExpression((LiteralExp) in, isStatic, params);
		} else if (in instanceof MessageExp) {
			// TODO OclMessageExp
		} else if (in instanceof CallExp) {
			// only if the first node is a call to a class property
			PropCallCreator maker = new PropCallCreator(expGeneratorHelper, myClass, context);
			CallExp ce = (CallExp) in;
			if (ce.getSource() == null || ce.getSource() instanceof TypeExp) {
				return maker.makeExpressionNode((CallExp) in, isStatic, params);
			} else {
				CodeExpression source = makeExpression((OCLExpression) ce.getSource(), isStatic, params);
				return maker.makeExpression(ce, source, isStatic, params);
			}
		} else if (in instanceof VariableExp) {
			return makeVariableExp((VariableExp) in);
		}
		return null;
	}

	private CodeExpression makeVariableExp(VariableExp in) {
		String result = null;
		org.eclipse.ocl.expressions.Variable<Classifier, Parameter> referredVariable = in.getReferredVariable();
		if (referredVariable instanceof PropertyOfImplicitObject) {
			PropertyOfImplicitObject poio = (PropertyOfImplicitObject) referredVariable;
			PropertyMap map = codeMaps.buildStructuralFeatureMap((Property) poio.getOriginalElement());
			if (poio.getImplicitVar().getName().equals("responsibility") || poio.getImplicitVar().getName().equals("contextObject")) {
				result = "get" + NameConverter.capitalize(poio.getImplicitVar().getName()) + "()." + map.getter() + "()";
			} else if (poio.getImplicitVar().getName().equals("self")) {
				Classifier type = poio.getImplicitVar().getType();
				result = calcSelfExpression(result, type) + "." + map.getter() + "()";
			} else {
				result = poio.getImplicitVar().getName() + "." + map.getter() + "()";
			}
		} else if (in.getName().equals("self")) {
			Classifier type = in.getType();
			result = calcSelfExpression(result, type);
		} else {
			result = in.getName();
		}
		return new PortableExpression(result);
	}

	private String calcSelfExpression(String result, Classifier type) {
		if (type instanceof StateMachine) {
			if (context.getBodyContainer() instanceof OpaqueExpression) {
				if (context.getBodyContainer().getOwner().getOwner() instanceof Transition) {
					result = "getStateMachineExecution()";
				} else if (context.getBodyContainer().getOwner() instanceof TimeExpression) {
					result = "getStateMachineExecution()";
				}
			}
		}
		EObject element = context.getBodyContainer();
		while (!(element == null || element instanceof Operation)) {
			element = EmfElementFinder.getContainer(element);
			// OCL Expressions on stereotype attributes
		}
		if (result == null) {
			result = "${self}";
		}
		return result;
	}

	public CodeField makeVarDecl(CodeBlock block, Variable exp, boolean isStatic, List<CodeParameter> params) {
		OCLExpression initExpression = (OCLExpression) exp.getInitExpression();
		ClassifierMap mapper = codeMaps.buildClassifierMap(exp.getType());
		CodeTypeReference myType = mapper.javaTypePath();
		CodeExpression myInitExp = mapper.defaultValue();
		// create init expression
		if (initExpression != null) {
			myInitExp = makeExpression(initExpression, isStatic, params);
		}
		CodeField result = new CodeField(block, ExpGeneratorHelper.javaFieldName(exp));
		result.setInitialization(myInitExp);
		result.setType(myType);
		return result;
	}

	private CodeExpression makeLetExpression(LetExp in, boolean isStatic, List<CodeParameter> params) {
		// generate a separate operation
		ClassifierMap inMap = codeMaps.buildClassifierMap(in.getType());
		CodeExpression myDefault = inMap.defaultValue();
		String operName = "letExpression" + myClass.getUniqueNumber();
		CodeMethod oper = null;
		List<CodeParameter> bodyParams = expGeneratorHelper.addVarToParams((Variable) in.getVariable(), params);
		oper = new CodeMethod(operName);
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setReturnType(inMap.javaTypePath());
		oper.setStatic(isStatic);
		oper.setVisibility(CodeVisibilityKind.PRIVATE);
		oper.setResultInitialValue(myDefault);
		oper.setComment("implements " + in.toString());
		makeVarDecl(oper.getBody(), (Variable) in.getVariable(), isStatic, params);
		oper.setResultInitialValue(new NullExpression());
		new SetResultStatement(oper.getBody(), makeExpression((OCLExpression) in.getIn(), isStatic, bodyParams));
		// generate the call to the created operation
		return new PortableExpression(operName + "(" + CodeBehaviour.paramsToActuals(oper) + ")");
	}

	private TernaryExpression makeIfExpression(IfExp in, boolean isStatic, List<CodeParameter> params) {
		ExpressionCreator myExpMaker = new ExpressionCreator(codeMaps, myClass, context);
		CodeExpression condition = myExpMaker.makeExpression((OCLExpression) in.getCondition(), isStatic, params);
		CodeExpression thenExpression = myExpMaker.makeExpression((OCLExpression) in.getThenExpression(), isStatic, params);
		CodeExpression elseExpression = myExpMaker.makeExpression((OCLExpression) in.getElseExpression(), isStatic, params);
		TernaryExpression te = new TernaryExpression(condition, thenExpression, elseExpression);
		Classifier thenType = in.getThenExpression().getType();
		Classifier elseType = in.getElseExpression().getType();
		if (thenType != elseType) {
			Classifier commonSuperType = EmfClassifierUtil.findCommonSuperType(thenType, elseType);
			ClassifierMap mapper = codeMaps.buildClassifierMap(commonSuperType);
			te.setConvertTo(mapper.javaTypePath());
		}
		return te;
	}
}
