package org.pavanecce.uml.ocl2code.creators;

import java.util.List;

import org.eclipse.ocl.expressions.CollectionKind;
import org.eclipse.ocl.uml.CollectionType;
import org.eclipse.ocl.uml.OperationCallExp;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.PrimitiveType;
import org.pavanecce.common.code.metamodel.CodeBehaviour;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CodeVisibilityKind;
import org.pavanecce.common.code.metamodel.OclStandardLibrary;
import org.pavanecce.common.code.metamodel.expressions.BinaryOperatorExpression;
import org.pavanecce.common.code.metamodel.expressions.IsNullExpression;
import org.pavanecce.common.code.metamodel.expressions.MethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.NewInstanceExpression;
import org.pavanecce.common.code.metamodel.expressions.NotExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.common.code.metamodel.statements.CodeForStatement;
import org.pavanecce.common.code.metamodel.statements.CodeIfStatement;
import org.pavanecce.common.code.metamodel.statements.CodeWhileStatement;
import org.pavanecce.common.code.metamodel.statements.MethodCallStatement;
import org.pavanecce.common.code.metamodel.statements.PortableStatement;
import org.pavanecce.common.code.metamodel.statements.SetResultStatement;
import org.pavanecce.uml.ocl2code.common.Check;
import org.pavanecce.uml.ocl2code.common.ExpGeneratorHelper;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;
import org.pavanecce.uml.ocl2code.maps.ClassifierMap;
import org.pavanecce.uml.ocl2code.maps.OperationMap;
import org.pavanecce.uml.uml2code.StdlibMap;

public class CollectionOperCallCreator {
	private CodeClassifier myClass = null;
	private UmlToCodeMaps codeMaps;
	private ExpGeneratorHelper expGeneratorHelper;

	public CollectionOperCallCreator(ExpGeneratorHelper h, CodeClassifier myClass) {
		super();
		this.expGeneratorHelper = h;
		this.codeMaps = h.CodeUtil;
		this.myClass = myClass;
	}

	public CodeExpression collectionOperCall(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, Operation referedOp, boolean isStatic,
			List<CodeParameter> params) {
		Check.pre("source is not null", exp.getSource() != null);
		//
		if (referedOp != null) {
			if (referedOp.getName().equals("count")) {
				return buildCount(exp, source, args, isStatic, params);
			} else if (referedOp.getName().equals("excludes")) {
				return buildLibMethodCall(source, ".excludes", args);
			} else if (referedOp.getName().equals("excludesAll")) {
				return buildLibMethodCall(source, ".excludesAll", args);
			} else if (referedOp.getName().equals("includes")) {
				return buildLibMethodCall(source, ".includes", args);
			} else if (referedOp.getName().equals("includesAll")) {
				return buildLibMethodCall(source, ".includesAll", args);
			} else if (referedOp.getName().equals("isEmpty")) {
				return new MethodCallExpression(source, "isEmpty");
			} else if (referedOp.getName().equals("notEmpty")) {
				return new NotExpression(new MethodCallExpression(source, "isEmpty"));
			} else if (referedOp.getName().equals("size")) {
				return new MethodCallExpression(source, "size");
			} else if (referedOp.getName().equals("sum")) {
				return buildSum(exp, source, isStatic, params);
				// from here with variant meaning
			} else if (referedOp.getName().equals("=")) {
				return buildEquals(exp, source, args, isStatic, params);
			} else if (referedOp.getName().equals("<>")) {
				return new NotExpression(buildEquals(exp, source, args, isStatic, params));
			} else if (referedOp.getName().equals("-")) {
				return buildMinus(exp, source, args, isStatic, params);
			} else if (referedOp.getName().equals("append")) {
				return buildAppend(exp, source, args, isStatic, params);
			} else if (referedOp.getName().equals("at")) {
				BinaryOperatorExpression minusOne = new BinaryOperatorExpression(args.get(0), "-", new PortableExpression("1"));
				return makeGet(exp, source, minusOne, minusOne);
			} else if (referedOp.getName().equals("excluding")) {
				return buildExcluding(exp, source, args, isStatic, params);
			} else if (referedOp.getName().equals("first")) {
				return makeGet(exp, source, new PortableExpression("0"), new PortableExpression("0"));
			} else if (referedOp.getName().equals("flatten")) {
				return buildFlatten(exp, source, args, isStatic, params);
			} else if (referedOp.getName().equals("including")) {
				return buildIncluding(exp, source, args, isStatic, params);
			} else if (referedOp.getName().equals("indexOf")) {
				MethodCallExpression indexOf = new MethodCallExpression(source, "indexOf", args.get(0));
				return new BinaryOperatorExpression(indexOf, "+", new PortableExpression("1"));
			} else if (referedOp.getName().equals("insertAt")) {
				return buildInsertAt(exp, source, args);
			} else if (referedOp.getName().equals("intersection")) {
				return buildIntersection(exp, source, args, isStatic, params);
			} else if (referedOp.getName().equals("last")) {
				return makeGet(exp, source, new PortableExpression("0"), new PortableExpression("source.size()-1"));
			} else if (referedOp.getName().equals("prepend")) {
				return buildPrepend(exp, source, args, isStatic, params);
			} else if (referedOp.getName().equals("subOrderedSet")) {
				return buildSubList(exp, source, args);
			} else if (referedOp.getName().equals("subSequence")) {
				return buildSubList(exp, source, args);
			} else if (referedOp.getName().equals("symmetricDifference")) {
				return buildSymmetricDifference(exp, source, args, isStatic, params);
			} else if (referedOp.getName().equals("union")) {
				return buildUnion(exp, source, args, isStatic, params);
			} else if (referedOp.getName().equals("asBag")) {
				myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
				return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".collectionAsBag", source);
			} else if (referedOp.getName().equals("asSequence")) {
				myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
				return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".collectionAsSequence", source);
			} else if (referedOp.getName().equals("asOrderedSet")) {
				myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
				return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".collectionAsOrderedSet", source);
			} else if (referedOp.getName().equals("asSet")) {
				myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
				return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".collectionAsSet", source);
			} else if (referedOp.getName().equals("oclIsUndefined")) {
				return new IsNullExpression(source);
			}
		}
		return null;
	}

	CollectionKind getCollectionKind(OperationCallExp exp) {
		return ((CollectionType) exp.getType()).getKind();
	}

	private CodeExpression buildEquals(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, boolean isStatic, List<CodeParameter> params) {
		CollectionKind collectionKind = getCollectionKind(exp);
		String operName = "";
		if (collectionKind == CollectionKind.SET_LITERAL) {
			operName = "setEquals";
		} else if (collectionKind == CollectionKind.BAG_LITERAL) {
			operName = "bagEquals";
		} else if (collectionKind == CollectionKind.SEQUENCE_LITERAL) {
			operName = "sequenceEquals";
		} else if (collectionKind == CollectionKind.ORDERED_SET_LITERAL) {
			operName = "orderedsetEquals";
		}
		myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
		return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + "." + operName, source, args.get(0));
	}

	private CodeExpression makeGet(OperationCallExp exp, CodeExpression source, CodeExpression minSize, CodeExpression index) {
		CollectionType elementType = (CollectionType) exp.getSource().getType();
		ClassifierMap elementMap = codeMaps.buildClassifierMap(elementType.getElementType());
		ClassifierMap collectionMap = codeMaps.buildClassifierMap(elementType);
		String operName = "getAtIndex" + myClass.getUniqueNumber();
		CodeMethod getAtIndex = new CodeMethod(operName, collectionMap.javaElementTypePath());
		getAtIndex.addParam("source", collectionMap.javaTypePath());
		getAtIndex.setDeclaringClass(myClass);
		getAtIndex.setVisibility(CodeVisibilityKind.PRIVATE);
		getAtIndex.setResultInitialValue(elementMap.defaultValue());
		BinaryOperatorExpression greaterThanMinSize = new BinaryOperatorExpression(new PortableExpression("source.size()"), ">", minSize);
		SetResultStatement setResult = new SetResultStatement(new MethodCallExpression("source.get", index));
		new CodeIfStatement(getAtIndex.getBody(), greaterThanMinSize, setResult);
		return new MethodCallExpression(getAtIndex.getName(), source);

	}

	private CodeExpression makeCopyOfSource(OperationCallExp exp, CodeExpression source) {
		myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
		return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".makeCopy", source);
	}

	private CodeExpression buildIncluding(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, boolean isStatic, List<CodeParameter> params) {
		Classifier elementType = (Classifier) exp.getReferredOperation().getType();
		CodeExpression argument = expGeneratorHelper.makeListElem(myClass, elementType, args.get(0));
		CollectionKind collectionKind = getCollectionKind(exp);
		ClassifierMap sourceMap = codeMaps.buildClassifierMap(exp.getType());
		CodeTypeReference returnTypePath = sourceMap.javaTypePath();
		String operName = exp.getReferredOperation().getName() + myClass.getUniqueNumber();
		CodeMethod oper = new CodeMethod(operName);
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setReturnType(returnTypePath);
		oper.setResultInitialValue(makeCopyOfSource(exp, source));

		oper.setVisibility(CodeVisibilityKind.PRIVATE);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString() + " on " + exp.getSource().toString());
		if (collectionKind == CollectionKind.BAG_LITERAL || collectionKind == CollectionKind.SEQUENCE_LITERAL) {
			new MethodCallStatement(oper.getBody(), "result.add", argument);
		} else if (collectionKind == CollectionKind.SET_LITERAL || collectionKind == CollectionKind.ORDERED_SET_LITERAL) {
			CodeIfStatement ifNotContains = new CodeIfStatement(oper.getBody(), new NotExpression(new MethodCallExpression("result.contains", argument)));
			new MethodCallStatement(ifNotContains.getThenBlock(), "result.add", argument);
		}
		return callMethod(oper);
	}

	private CodeExpression buildMinus(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, boolean isStatic, List<CodeParameter> params) {
		ClassifierMap sourceMap = codeMaps.buildClassifierMap(exp.getType());
		CodeTypeReference returnTypePath = sourceMap.javaTypePath();
		String operName = "minus" + myClass.getUniqueNumber();
		CodeMethod oper = new CodeMethod(operName, returnTypePath);
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setResultInitialValue(makeCopyOfSource(exp, source));
		oper.setVisibility(CodeVisibilityKind.PRIVATE);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString() + " on " + exp.getSource().toString());
		new MethodCallStatement(oper.getBody(), "result.removeAll", args.get(0));
		return callMethod(oper);
	}

	private CodeExpression buildExcluding(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, boolean isStatic, List<CodeParameter> params) {
		CollectionKind collectionKind = getCollectionKind(exp);
		ClassifierMap sourceMap = codeMaps.buildClassifierMap(exp.getType());
		CodeTypeReference returnTypePath = sourceMap.javaTypePath();
		String operName = exp.getReferredOperation().getName() + myClass.getUniqueNumber();
		CodeMethod oper = new CodeMethod(operName, returnTypePath);
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setResultInitialValue(makeCopyOfSource(exp, source));
		oper.setVisibility(CodeVisibilityKind.PRIVATE);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString() + " on " + exp.getSource().toString());
		if (collectionKind == CollectionKind.BAG_LITERAL || collectionKind == CollectionKind.SEQUENCE_LITERAL) {
			CodeWhileStatement whileContains = new CodeWhileStatement(oper.getBody(), new MethodCallExpression("result.contains", args.get(0)));
			new MethodCallStatement(whileContains.getBody(), "result.remove", args.get(0));
		} else if (collectionKind == CollectionKind.SET_LITERAL || collectionKind == CollectionKind.ORDERED_SET_LITERAL) {

			new MethodCallStatement(oper.getBody(), "result.remove", args.get(0));
		}
		return callMethod(oper);
	}

	private CodeExpression buildAppend(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, boolean isStatic, List<CodeParameter> params) {
		ClassifierMap sourceMap = codeMaps.buildClassifierMap(exp.getType());
		CodeTypeReference returnTypePath = sourceMap.javaTypePath();
		String operName = exp.getReferredOperation().getName() + myClass.getUniqueNumber();
		CodeMethod oper = new CodeMethod(operName);
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setReturnType(returnTypePath);
		oper.setResultInitialValue(makeCopyOfSource(exp, source));
		oper.setVisibility(CodeVisibilityKind.PRIVATE);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString() + " on " + exp.getSource().toString());
		new MethodCallStatement(oper.getBody(), "result.add", args.get(0));
		return callMethod(oper);
	}

	private CodeExpression buildPrepend(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, boolean isStatic, List<CodeParameter> params) {
		ClassifierMap sourceMap = codeMaps.buildClassifierMap(exp.getType());
		CodeTypeReference returnTypePath = sourceMap.javaTypePath();
		String operName = exp.getReferredOperation().getName() + myClass.getUniqueNumber();
		CodeMethod oper = new CodeMethod(operName, returnTypePath);
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setResultInitialValue(makeCopyOfSource(exp, source));
		oper.setVisibility(CodeVisibilityKind.PRIVATE);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString() + " on " + exp.getSource().toString());
		new MethodCallStatement(oper.getBody(), "result.add", new PortableExpression("0"), args.get(0));
		return callMethod(oper);
	}

	private CodeExpression buildUnion(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, boolean isStatic, List<CodeParameter> params) {
		ClassifierMap sourceMap = codeMaps.buildClassifierMap(exp.getType());
		String operName = exp.getReferredOperation().getName() + myClass.getUniqueNumber();
		CodeMethod oper = new CodeMethod(operName, sourceMap.javaTypePath());
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setResultInitialValue(makeCopyOfSource(exp, source));
		oper.setVisibility(CodeVisibilityKind.PRIVATE);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString() + " on " + exp.getSource().toString());
		if (!exp.getReferredOperation().isUnique()) {
			new MethodCallStatement(oper.getBody(), "result.addAll", args.get(0));
		}
		if (exp.getReferredOperation().isUnique()) {
			CodeForStatement forEach = new CodeForStatement(oper.getBody(), "elem", sourceMap.javaElementTypePath(), args.get(0));
			CodeIfStatement ifNotContains = new CodeIfStatement(forEach.getBody(), new NotExpression(new PortableExpression("result.contains(elem")));
			new PortableStatement(ifNotContains.getThenBlock(), "result.add(elem)");
		}
		return callMethod(oper);
	}

	private CodeExpression buildInsertAt(OperationCallExp exp, CodeExpression source, List<CodeExpression> args) {
		CodeExpression index = new BinaryOperatorExpression(args.get(0), "-", new PortableExpression("1"));
		CodeExpression insertedElem = expGeneratorHelper.makeListElem(myClass, (Classifier) exp.getReferredOperation().getType(), args.get(1));
		myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
		return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".insertAt", source, index, insertedElem);
	}

	private CodeExpression buildIntersection(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, boolean isStatic,
			List<CodeParameter> params) {
		Classifier elementType = (Classifier) exp.getReferredOperation().getType();
		ClassifierMap sourceMap = codeMaps.buildClassifierMap(exp.getType());
		CodeTypeReference returnTypePath = sourceMap.javaTypePath();
		CodeTypeReference myListType = expGeneratorHelper.makeListType(elementType);
		String operName = exp.getReferredOperation().getName() + myClass.getUniqueNumber();
		CodeMethod oper = new CodeMethod(operName, returnTypePath);
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setResultInitialValue(new NewInstanceExpression(sourceMap.javaDefaultTypePath()));
		oper.setVisibility(CodeVisibilityKind.PRIVATE);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString() + " on " + exp.getSource().toString());
		CodeForStatement forEach = new CodeForStatement(oper.getBody(), "elem", myListType, source);
		new CodeIfStatement(forEach.getBody(), new MethodCallExpression(args.get(0), "contains", new PortableExpression("elem")));
		return callMethod(oper);
	}

	private CodeExpression buildFlatten(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, boolean isStatic, List<CodeParameter> params) {
		CollectionKind collectionKind = getCollectionKind(exp);
		String operName = "";
		if (collectionKind == CollectionKind.SET_LITERAL) {
			operName = ".setFlatten";
		} else if (collectionKind == CollectionKind.BAG_LITERAL) {
			operName = ".bagFlatten";
		} else if (collectionKind == CollectionKind.SEQUENCE_LITERAL) {
			operName = ".sequenceFlatten";
		} else if (collectionKind == CollectionKind.ORDERED_SET_LITERAL) {
			operName = ".orderedsetFlatten";
		}
		myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
		return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + operName, source);
	}

	private CodeExpression buildSubList(OperationCallExp exp, CodeExpression source, List<CodeExpression> args) {
		MethodCallExpression result = new MethodCallExpression(source, "subList");
		for (CodeExpression arg : args) {
			result.getArguments().add(new BinaryOperatorExpression(arg, "-", new PortableExpression("1")));
		}
		return result;
	}

	private CodeExpression buildSymmetricDifference(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, boolean isStatic,
			List<CodeParameter> params) {
		ClassifierMap sourceMap = codeMaps.buildClassifierMap(exp.getType());
		Classifier elementType = (Classifier) exp.getReferredOperation().getType();
		CodeTypeReference returnTypePath = sourceMap.javaTypePath();
		CodeTypeReference myListType = expGeneratorHelper.makeListType(elementType);
		String operName = exp.getReferredOperation().getName() + myClass.getUniqueNumber();
		CodeMethod oper = new CodeMethod(operName, returnTypePath);
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setResultInitialValue(makeCopyOfSource(exp, source));
		oper.setVisibility(CodeVisibilityKind.PRIVATE);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString() + " on " + exp.getSource().toString());
		new MethodCallStatement(oper.getBody(), "result.removeAll", args.get(0));
		CodeForStatement forEach = new CodeForStatement(oper.getBody(), "elem", myListType, args.get(0));
		CodeIfStatement ifNotContains = new CodeIfStatement(forEach.getBody(), new NotExpression(new MethodCallExpression(source, "contains",
				new PortableExpression("elem"))));
		new PortableStatement(ifNotContains.getThenBlock(), "result.add(elem)");
		return callMethod(oper);
	}

	private CodeExpression buildSum(OperationCallExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		Classifier elementType = (Classifier) exp.getReferredOperation().getType();
		ClassifierMap elementMap = codeMaps.buildClassifierMap(exp.getType());
		String operName = exp.getReferredOperation().getName() + myClass.getUniqueNumber();
		String sumExp = "result + elem";
		if (!(elementType instanceof PrimitiveType)) { // the model class
			// defines the
			// '+' operation
			sumExp = "result." + OperationMap.javaPlusOperName() + "(elem)";
		}
		CodeMethod oper = new CodeMethod(operName, elementMap.javaTypePath());
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setResultInitialValue(elementMap.defaultValue());
		oper.setVisibility(CodeVisibilityKind.PRIVATE);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString() + " on " + exp.getSource().toString());
		CodeForStatement forEach = new CodeForStatement(oper.getBody(), "elem", elementMap.javaTypePath(), source);
		new PortableStatement(forEach.getBody(), "result = " + sumExp);
		return callMethod(oper);
	}

	private PortableExpression callMethod(CodeMethod oper) {
		return new PortableExpression(oper.getName() + "(" + CodeBehaviour.paramsToActuals(oper) + ")");
	}

	protected CodeExpression buildLibMethodCall(CodeExpression source, String string, List<CodeExpression> args) {
		myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
		return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + string, source, args.get(0));
	}

	protected CodeExpression buildLibMethodCall(CodeExpression source, String string) {
		myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
		return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + string, source);
	}

	private CodeExpression buildCount(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, boolean isStatic, List<CodeParameter> params) {
		Classifier elementType = (Classifier) exp.getReferredOperation().getType();
		CodeTypeReference myType = expGeneratorHelper.makeListType(elementType);
		String operName = "count" + myClass.getUniqueNumber();
		CodeExpression argStr = expGeneratorHelper.makeListElem(myClass, elementType, args.get(0));
		CodeMethod oper = new CodeMethod(operName, StdlibMap.javaIntegerObjectType);
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setResultInitialValue("0");

		oper.setVisibility(CodeVisibilityKind.PRIVATE);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString() + " on " + exp.getSource().toString());
		CodeForStatement forEach = new CodeForStatement(oper.getBody(), "elem", myType, source);
		CodeIfStatement ifEquals = new CodeIfStatement(forEach.getBody(), new BinaryOperatorExpression(new PortableExpression("elem"), "${equals}", argStr));
		new PortableStatement(ifEquals.getThenBlock(), "result = result + 1");
		return callMethod(oper);
	}
}
