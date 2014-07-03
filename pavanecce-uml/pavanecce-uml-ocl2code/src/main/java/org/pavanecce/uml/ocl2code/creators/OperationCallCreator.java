package org.pavanecce.uml.ocl2code.creators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ocl.uml.CollectionType;
import org.eclipse.ocl.uml.OCLExpression;
import org.eclipse.ocl.uml.OperationCallExp;
import org.eclipse.ocl.uml.PrimitiveType;
import org.eclipse.ocl.uml.TypeExp;
import org.eclipse.ocl.uml.TypeType;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Operation;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.expressions.BinaryOperatorExpression;
import org.pavanecce.common.code.metamodel.expressions.IsNullExpression;
import org.pavanecce.common.code.metamodel.expressions.MethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.NotExpression;
import org.pavanecce.common.code.metamodel.expressions.StaticMethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.TypeExpression;
import org.pavanecce.common.code.metamodel.expressions.TypeExpressionKind;
import org.pavanecce.uml.common.ocl.AbstractOclContext;
import org.pavanecce.uml.ocl2code.common.ExpGeneratorHelper;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;
import org.pavanecce.uml.ocl2code.maps.ClassifierMap;
import org.pavanecce.uml.ocl2code.maps.OperationMap;

public class OperationCallCreator extends AbstractOperationCallCreator {
	private UmlToCodeMaps codeMaps;
	private ExpGeneratorHelper expGeneratorHelper;
	private AbstractOclContext context;

	public OperationCallCreator(ExpGeneratorHelper h, CodeClassifier myClass, AbstractOclContext context) {
		super(myClass);
		expGeneratorHelper = h;
		this.codeMaps = h.CodeUtil;
		this.context = context;
	}

	public CodeExpression operationCallExp(OperationCallExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		CodeExpression result = null;
		List<CodeExpression> args = makeArgs(exp, isStatic, params);
		Operation referedOp = exp.getReferredOperation();
		Classifier sourceType = (exp.getSource() == null ? null : exp.getSource().getType());
		if (sourceType instanceof TypeType) {
			if (referedOp.getName().equals("allInstances")) {
				result = buildAllInstances(exp, source, args, referedOp);
			} else {
				result = buildClassOp(args, referedOp);
			}
		} else if (sourceType instanceof CollectionType) {
			CollectionOperCallCreator maker1 = new CollectionOperCallCreator(expGeneratorHelper, myClass);
			result = maker1.collectionOperCall(exp, source, args, referedOp, isStatic, params);
		} else if (sourceType instanceof PrimitiveType) {
			PrimitiveTypeOperCallCreator maker2 = new PrimitiveTypeOperCallCreator(myClass);
			result = maker2.makeOperCall(exp, source, args, referedOp, params);
		} else if (sourceType instanceof Enumeration) {
			EnumerationOperationCallCreator eocc = new EnumerationOperationCallCreator(myClass);
			result = eocc.makeOperCall(exp, source, args, referedOp, params);
			if (result == null) {
				result = makeOperCall(exp, source, args, referedOp, params);
			}
		} else {
			result = makeOperCall(exp, source, args, referedOp, params);
		}
		return result;
	}

	private CodeExpression buildClassOp(List<CodeExpression> args, Operation referedOp) {
		ClassifierMap owner = codeMaps.buildClassifierMap((Classifier) referedOp.getOwner());
		return new StaticMethodCallExpression(owner.javaTypePath(), referedOp.getName(), args);
	}

	private List<CodeExpression> makeArgs(OperationCallExp exp, boolean isStatic, List<CodeParameter> params) {
		List<CodeExpression> result = new ArrayList<CodeExpression>();
		if (exp.getArgument().size() > 0) {
			ExpressionCreator myExpMaker = new ExpressionCreator(codeMaps, myClass, context);
			exp.getArgument();
			Iterator<?> it = exp.getArgument().iterator();
			while (it.hasNext()) {
				OCLExpression arg = (OCLExpression) it.next();
				CodeExpression expStr = myExpMaker.makeExpression(arg, isStatic, params);
				result.add(expStr);
			}
		}
		return result;
	}

	private CodeExpression makeOperCall(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, Operation referedOp, List<CodeParameter> params) {
		CodeExpression result = null;
		if (referedOp != null) {
			ClassifierMap typeMap = codeMaps.buildClassifierMap(exp.getSource().getType());
			if (referedOp.getName().equals("oclIsNew")) {
				if (exp.getSource().getType() instanceof PrimitiveType) {
					return new BinaryOperatorExpression(source, "${equals}", typeMap.defaultValue());
				} else if (exp.getSource().getType() instanceof Enumeration) {
					// ???
					return new BinaryOperatorExpression(source, "${equals}", typeMap.defaultValue());
				} else {
					return new MethodCallExpression(source, "isNew");
				}
			} else if (referedOp.getName().equals("oclIsUndefined")) {
				result = buildIsUndefined(exp, source, args);
			} else if (referedOp.getName().equals("oclInState") || referedOp.getName().equals("oclIsInState")) { // on
				result = buildOclInState(exp, source, args);
			} else if (referedOp.getName().equals("oclIsKindOf")) {
				result = buildIsKindOf(exp, source, args);
			} else if (referedOp.getName().equals("oclIsTypeOf")) {
				result = buildIsTypeOf(exp, source, args);
			} else if (referedOp.getName().equals("oclAsType")) {
				result = buildAsType(exp, source, args);
			} else if (referedOp.getName().equals("=")) {
				result = new BinaryOperatorExpression(source, "${equals}", args.get(0));
			} else if (referedOp.getName().equals("<>")) {
				result = new NotExpression(new BinaryOperatorExpression(source, "${equals}", args.get(0)));
			} else {
				result = super.commonOperations(source, referedOp);
				if (result == null) {
					result = buildModelOp(exp, source, args, referedOp);
				}
			}
		}
		return result;
	}

	private CodeExpression buildAllInstances(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, Operation referedOp) {
		TypeType tt = (TypeType) exp.getSource().getType();
		ClassifierMap owner = codeMaps.buildClassifierMap(tt.getReferredType());
		return new StaticMethodCallExpression(owner.javaTypePath(), "allInstances");
	}

	private CodeExpression buildIsUndefined(OperationCallExp exp, CodeExpression source, List<CodeExpression> args) {
		return new IsNullExpression(source);
	}

	private CodeExpression buildAsType(OperationCallExp exp, CodeExpression source, List<CodeExpression> args) {
		Classifier argType = ((TypeExp) exp.getArgument().get(0)).getReferredType();
		return new TypeExpression(codeMaps.buildClassifierMap(argType).javaTypePath(), TypeExpressionKind.AS_TYPE, args.get(0));
	}

	private CodeExpression buildIsKindOf(OperationCallExp exp, CodeExpression source, List<CodeExpression> args) {
		Classifier argType = ((TypeExp) exp.getArgument().get(0)).getReferredType();
		return new TypeExpression(codeMaps.buildClassifierMap(argType).javaTypePath(), TypeExpressionKind.IS_KIND, args.get(0));
	}

	private CodeExpression buildIsTypeOf(OperationCallExp exp, CodeExpression source, List<CodeExpression> args) {
		Classifier argType = ((TypeExp) exp.getArgument().get(0)).getReferredType();
		return new TypeExpression(codeMaps.buildClassifierMap(argType).javaTypePath(), TypeExpressionKind.IS_TYPE, args.get(0));
	}

	private CodeExpression buildOclInState(OperationCallExp exp, CodeExpression source, List<CodeExpression> args) {
		return new MethodCallExpression(source, "isInState" + args.get(0));
	}

	private CodeExpression buildModelOp(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, Operation referedOp) {
		OperationMap operation = codeMaps.buildOperationMap(referedOp);
		String operationName = operation.javaOperName();
		MethodCallExpression result = null;
		result = new MethodCallExpression(source, operationName, args);
		return result;
	}
}
