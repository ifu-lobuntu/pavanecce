package org.pavanecce.uml.ocl2code.creators;

import java.util.List;

import org.eclipse.ocl.uml.OperationCallExp;
import org.eclipse.ocl.uml.TypeExp;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Operation;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.OclStandardLibrary;
import org.pavanecce.common.code.metamodel.expressions.BinaryOperatorExpression;
import org.pavanecce.common.code.metamodel.expressions.MethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.NotExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.common.code.metamodel.expressions.TernaryExpression;
import org.pavanecce.uml.common.util.EmfClassifierUtil;
import org.pavanecce.uml.ocl2code.common.OclTypeNames;

public class PrimitiveTypeOperCallCreator extends AbstractOperationCallCreator {
	public PrimitiveTypeOperCallCreator(CodeClassifier myClass) {
		super(myClass);
	}

	public CodeExpression makeOperCall(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, Operation referedOp, List<CodeParameter> params) {
		Classifier sourceType = (exp.getSource() == null ? null : exp.getSource().getType());
		if (EmfClassifierUtil.comformsToLibraryType(sourceType, OclTypeNames.BooleanTypeName)) {
			return booleanOperCall(exp, source, args, referedOp, params);
		} else if (EmfClassifierUtil.comformsToLibraryType(sourceType, OclTypeNames.StringTypeName)) {
			return stringOperCall(exp, source, args, referedOp, params);
		} else if (EmfClassifierUtil.comformsToLibraryType(sourceType, OclTypeNames.IntegerTypeName)) {
			return integerOperCall(exp, source, args, referedOp, params);
		} else if (EmfClassifierUtil.comformsToLibraryType(sourceType, OclTypeNames.RealTypeName)) {
			return realOperCall(exp, source, args, referedOp, params);
		}
		return null;
	}

	private CodeExpression integerOperCall(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, Operation referedOp,
			List<CodeParameter> params) {
		if (referedOp != null) {
			if (referedOp.getName().equals("toString")) {
				return new MethodCallExpression(OclStandardLibrary.FORMATTER.getPhysicalName() + ".getInstance().formatInteger", source);
			} else {
				return numericOperCall(exp, source, args, referedOp, params);
			}
		}
		return null;
	}

	private CodeExpression realOperCall(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, Operation referedOp, List<CodeParameter> params) {
		if (referedOp != null) {
			if (referedOp.getName().equals("toString")) {
				return new MethodCallExpression(OclStandardLibrary.FORMATTER.getPhysicalName() + ".getInstance().formatReal", source);
			} else {
				return numericOperCall(exp, source, args, referedOp, params);
			}
		}
		return null;
	}

	private CodeExpression booleanOperCall(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, Operation referedOp,
			List<CodeParameter> params) {
		CodeExpression result = null;
		if (referedOp != null) {
			if (referedOp.getName().equals("not")) {
				return new NotExpression(source);
			} else if (referedOp.getName().equals("or")) {
				return new BinaryOperatorExpression(source, "${or}", args.get(0));
			} else if (referedOp.getName().equals("xor")) {
				return new BinaryOperatorExpression(source, "${xor}", args.get(0));
			} else if (referedOp.getName().equals("and")) {
				return new BinaryOperatorExpression(source, "${and}", args.get(0));
			} else if (referedOp.getName().equals("implies")) {
				return new TernaryExpression(source, args.get(0), new PortableExpression("true"));
			} else if (referedOp.getName().equals("=")) {
				return new BinaryOperatorExpression(source, "${equals}", args.get(0));
			} else if (referedOp.getName().equals("<>")) {
				return new BinaryOperatorExpression(source, "!=", args.get(0));
			} else if (referedOp.getName().equals("toString")) {
				myClass.addStdLibToImports(OclStandardLibrary.FORMATTER);
				return new MethodCallExpression(OclStandardLibrary.FORMATTER.getPhysicalName() + ".getInstance().formatBoolean", source);
			} else {
				return commonOperations(source, referedOp);
			}
		}
		return result;
	}

	private CodeExpression numericOperCall(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, Operation referedOp,
			List<CodeParameter> params) {
		if (referedOp != null) {
			if (referedOp.getName().equals("div")) {
				return new BinaryOperatorExpression(source, "/", args.get(0));
			} else if (referedOp.getName().equals("mod")) {
				return new BinaryOperatorExpression(source, "%", args.get(0));
			} else if (referedOp.getName().equals("abs")) {
				return new MethodCallExpression(OclStandardLibrary.MATH.getPhysicalName() + ".abs", source, args.get(0));
			} else if (referedOp.getName().equals("max")) {
				return new MethodCallExpression(OclStandardLibrary.MATH.getPhysicalName() + ".max", source, args.get(0));
			} else if (referedOp.getName().equals("min")) {
				return new MethodCallExpression(OclStandardLibrary.MATH.getPhysicalName() + ".min", source, args.get(0));
			} else if (referedOp.getName().equals("floor")) {
				return new MethodCallExpression(OclStandardLibrary.MATH.getPhysicalName() + ".floor", source, args.get(0));
			} else if (referedOp.getName().equals("round")) {
				return new MethodCallExpression(OclStandardLibrary.MATH.getPhysicalName() + ".round", source, args.get(0));
			} else if (referedOp.getName().equals("oclAsType")) {
				Classifier argType = ((TypeExp) exp.getArgument().get(0)).getReferredType();
				return new MethodCallExpression(OclStandardLibrary.MATH.getPhysicalName() + ".as" + argType.getName(), source, args.get(0));
			} else if (referedOp.getName().equals("=")) {
				return new BinaryOperatorExpression(source, "=", args.get(0));
			} else if (referedOp.getName().equals(">=")) {
				return new BinaryOperatorExpression(source, ">=", args.get(0));
			} else if (referedOp.getName().equals("<=")) {
				return new BinaryOperatorExpression(source, "<=", args.get(0));
			} else if (referedOp.getName().equals("<>")) {
				return new BinaryOperatorExpression(source, "${notEquals}", args.get(0));
			} else if (referedOp.getName().equals("<")) {
				return new BinaryOperatorExpression(source, "<", args.get(0));
			} else if (referedOp.getName().equals(">")) {
				return new BinaryOperatorExpression(source, ">", args.get(0));
			} else if (referedOp.getName().equals("+")) {
				return new BinaryOperatorExpression(source, "+", args.get(0));
			} else if (referedOp.getName().equals("*")) {
				return new BinaryOperatorExpression(source, "*", args.get(0));
			} else if (referedOp.getName().equals("/")) {
				return new BinaryOperatorExpression(source, "/", args.get(0));
			} else if (referedOp.getName().equals("-")) {
				if (exp.getArgument().isEmpty()) {
					return new BinaryOperatorExpression(new PortableExpression("0"), "-", source);
				} else {
					return new BinaryOperatorExpression(source, "", args.get(0));
				}
			} else {
				return commonOperations(source, referedOp);
			}
		}
		return null;
	}

	private CodeExpression stringOperCall(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, Operation referedOp,
			List<CodeParameter> params) {
		if (referedOp != null) {
			if (referedOp.getName().equals("size")) {
				myClass.addStdLibToImports(OclStandardLibrary.PRIMITIVES);
				return new MethodCallExpression(OclStandardLibrary.PRIMITIVES.getPhysicalName() + ".length", source);
			} else if (referedOp.getName().equals("concat")) {
				myClass.addStdLibToImports(OclStandardLibrary.PRIMITIVES);
				return new MethodCallExpression(OclStandardLibrary.PRIMITIVES.getPhysicalName() + ".concat", source, args.get(0));
			} else if (referedOp.getName().equals("+")) {
				myClass.addStdLibToImports(OclStandardLibrary.PRIMITIVES);
				return new MethodCallExpression(OclStandardLibrary.PRIMITIVES.getPhysicalName() + ".concat", source, args.get(0));
			} else if (referedOp.getName().equals("toInteger")) {
				myClass.addStdLibToImports(OclStandardLibrary.FORMATTER);
				return new MethodCallExpression(OclStandardLibrary.FORMATTER.getPhysicalName() + ".getInstance().parseInteger", source);
			} else if (referedOp.getName().equals("toReal")) {
				myClass.addStdLibToImports(OclStandardLibrary.FORMATTER);
				return new MethodCallExpression(OclStandardLibrary.FORMATTER.getPhysicalName() + ".getInstance().parseReal", source);
			} else if (referedOp.getName().equals("toUpper")) {
				myClass.addStdLibToImports(OclStandardLibrary.PRIMITIVES);
				return new MethodCallExpression(OclStandardLibrary.PRIMITIVES.getPhysicalName() + ".toUpper", source);
			} else if (referedOp.getName().equals("toLower")) {
				myClass.addStdLibToImports(OclStandardLibrary.PRIMITIVES);
				return new MethodCallExpression(OclStandardLibrary.PRIMITIVES.getPhysicalName() + ".toLower", source);
			} else if (referedOp.getName().equals("substring")) {
				myClass.addStdLibToImports(OclStandardLibrary.PRIMITIVES);
				return new MethodCallExpression(OclStandardLibrary.PRIMITIVES.getPhysicalName() + ".substring", source, args.get(0), args.get(1));
			} else if (referedOp.getName().equals("replaceAll")) {
				myClass.addStdLibToImports(OclStandardLibrary.PRIMITIVES);
				return new MethodCallExpression(OclStandardLibrary.PRIMITIVES.getPhysicalName() + ".replaceAll", source, args.get(0), args.get(1));
			} else if (referedOp.getName().equals("=")) {
				return new BinaryOperatorExpression(source, "${equals}", args.get(0));
			} else if (referedOp.getName().equals("<>")) {
				return new BinaryOperatorExpression(source, "${notEquals}", args.get(0));
			} else {
				return commonOperations(source, referedOp);
			}
		}
		return null;
	}
}
