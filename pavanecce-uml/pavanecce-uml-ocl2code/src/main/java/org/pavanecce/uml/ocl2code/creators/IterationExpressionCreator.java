package org.pavanecce.uml.ocl2code.creators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.ocl.uml.CollectionType;
import org.eclipse.ocl.uml.IterateExp;
import org.eclipse.ocl.uml.IteratorExp;
import org.eclipse.ocl.uml.LoopExp;
import org.eclipse.ocl.uml.OCLExpression;
import org.eclipse.ocl.uml.Variable;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Parameter;
import org.pavanecce.common.code.metamodel.CodeBehaviour;
import org.pavanecce.common.code.metamodel.CodeBlock;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CodeVisibilityKind;
import org.pavanecce.common.code.metamodel.OclStandardLibrary;
import org.pavanecce.common.code.metamodel.expressions.IsNullExpression;
import org.pavanecce.common.code.metamodel.expressions.MethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.NewInstanceExpression;
import org.pavanecce.common.code.metamodel.expressions.NotExpression;
import org.pavanecce.common.code.metamodel.expressions.NullExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.common.code.metamodel.statements.AssignmentStatement;
import org.pavanecce.common.code.metamodel.statements.CodeForStatement;
import org.pavanecce.common.code.metamodel.statements.CodeIfStatement;
import org.pavanecce.common.code.metamodel.statements.MethodCallStatement;
import org.pavanecce.common.code.metamodel.statements.PortableStatement;
import org.pavanecce.common.code.metamodel.statements.SetResultStatement;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.ocl.AbstractOclContext;
import org.pavanecce.uml.ocl2code.common.EmfPropertyCallHelper;
import org.pavanecce.uml.ocl2code.common.ExpGeneratorHelper;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;
import org.pavanecce.uml.ocl2code.maps.ClassifierMap;
import org.pavanecce.uml.uml2code.StdlibMap;

public class IterationExpressionCreator {
	private CodeClassifier myClass = null;
	private CodeVisibilityKind priv = CodeVisibilityKind.PRIVATE;
	private String[] iterVarNames = { "it" };
	private CodeTypeReference resultTypePath = null;
	private CodeExpression resultDefault = null;
	private CodeTypeReference myElementTypePath = null;
	private CodeExpression myListDefault;
	private CodeExpression expStr = null;
	private CodeTypeReference expType;
	private String operName = "";
	private Classifier elementType = null;
	private ExpGeneratorHelper expGeneratorHelper;
	private UmlToCodeMaps codeMaps;
	private AbstractOclContext context;

	public IterationExpressionCreator(ExpGeneratorHelper h, CodeClassifier myClass, AbstractOclContext context) {
		super();
		this.expGeneratorHelper = h;
		this.codeMaps = h.CodeUtil;
		this.myClass = myClass;
		this.context = context;
	}

	public CodeExpression makeIteratorExpression(IteratorExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		CodeExpression result = null;
		setVariables(exp, isStatic, params);
		String iteratorName = exp.getName();
		if (iteratorName.equals("forAll")) {
			result = createForAll(exp, source, isStatic, params);
		} else if (iteratorName.equals("exists")) {
			result = createExists(exp, source, isStatic, params);
		} else if (iteratorName.equals("isUnique")) {
			result = createIsUnique(exp, source, isStatic, params);
		} else if (iteratorName.equals("one")) {
			result = createOne(exp, source, isStatic, params);
		} else if (iteratorName.equals("any")) {
			result = createAny(exp, source, isStatic, params);
		} else if (iteratorName.equals("reject")) {
			result = createReject(exp, source, isStatic, params);
		} else if (iteratorName.equals("select")) {
			result = createSelect(exp, source, isStatic, params);
		} else if (iteratorName.equals("sortedBy")) {
			result = createSortedBy(exp, source, isStatic, params);
		} else if (iteratorName.equals("collectNested")) {
			result = createCollectNested(exp, source, isStatic, params);
		} else if (iteratorName.equals("collect")) {
			result = createCollect(exp, source, isStatic, params);
		}
		return result;
	}

	public CodeExpression iterateExp(IterateExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		// get the result variable of the expression
		ExpressionCreator maker = new ExpressionCreator(codeMaps, myClass, context);
		List<CodeParameter> bodyParams = expGeneratorHelper.addVarToParams((Variable) exp.getResult(), params);
		String resultName = "result";
		// get info
		setVariables(exp, isStatic, bodyParams);
		// generate a separate operation
		CodeMethod oper = new CodeMethod(operName);
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setReturnType(resultTypePath);
		oper.setResultInitialValue(new NullExpression());
		oper.setVisibility(priv);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString());
		maker.makeVarDecl(oper.getBody(), (Variable) exp.getResult(), isStatic, params);
		if (exp.getIterator().size() == 2) {
			CodeForStatement forEach1 = new CodeForStatement(oper.getBody(), iterVarNames[0], myElementTypePath, source);
			CodeForStatement forEach2 = new CodeForStatement(forEach1.getBody(), iterVarNames[1], myElementTypePath, source);
			new AssignmentStatement(forEach2.getBody(), resultName, expStr);
		} else if (exp.getIterator().size() == 1) {
			CodeForStatement forEach = new CodeForStatement(oper.getBody(), iterVarNames[0], myElementTypePath, source);
			new AssignmentStatement(forEach.getBody(), resultName, expStr);
		}
		return callMethod(oper);
	}

	private CodeExpression createCollect(IteratorExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		OCLExpression body = (OCLExpression) exp.getBody();
		expStr = expGeneratorHelper.makeListElem(myClass, body.getType(), expStr);
		Classifier argType = body.getType();
		CodeMethod oper = new CodeMethod(operName, resultTypePath);
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setVisibility(priv);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString());
		oper.setResultInitialValue(resultDefault);
		CodeForStatement forEach = new CodeForStatement(oper.getBody(), iterVarNames[0], myElementTypePath, source);
		new CodeField(forEach.getBody(), "bodyExpResult", expType).setInitialization(expStr);
		if (argType instanceof CollectionType || EmfPropertyCallHelper.resultsInMany(body)) {
			new PortableStatement(forEach.getBody(), "result.addAll( bodyExpResult )");
		} else {
			NotExpression notNull = new NotExpression(new IsNullExpression(new PortableExpression("bodyExpResult")));
			new CodeIfStatement(forEach.getBody(), notNull, new PortableStatement("result.add( bodyExpResult )"));
		}

		return callMethod(oper);
	}

	private CodeExpression createCollectNested(IteratorExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		// make sure expressions of primitive type can be added to a collection
		expStr = expGeneratorHelper.makeListElem(myClass, exp.getBody().getType(), expStr);
		CodeMethod oper = buildOperation(exp, isStatic, params);
		CodeForStatement forEach = new CodeForStatement(oper.getBody(), iterVarNames[0], myElementTypePath, source);
		new CodeField(forEach.getBody(), "bodyExpResult", expType).setInitialization(expStr);
		CodeIfStatement ifNotNull = new CodeIfStatement(forEach.getBody(), new NotExpression(new IsNullExpression(new PortableExpression("bodyExpResult"))));
		new PortableStatement(ifNotNull.getThenBlock(), "result.add( bodyExpResult )");
		return callMethod(oper);
	}

	private CodeExpression createSortedBy(IteratorExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		String iterVarName = iterVarNames[0];
		String identifier = NameConverter.toValidVariableName(exp.toString());
		CodeTypeReference comparatorClassName = new ComparatorCreator(codeMaps).makeComparator(elementType, exp.getBody().getType(), expStr, iterVarName,
				identifier);

		CodeMethod oper = buildOperation(exp, isStatic, params);
		new CodeField(oper.getBody(), "comp", comparatorClassName).setInitialization(new NewInstanceExpression(comparatorClassName));
		new MethodCallStatement(oper.getBody(), "result.addAll", source);
		myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
		new PortableStatement(oper.getBody(), OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".sort(result, comp)");
		return callMethod(oper);
	}

	private CodeExpression createSelect(IteratorExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		CodeMethod oper = buildOperation(exp, isStatic, params);
		CodeBlock body11 = oper.getBody();
		CodeForStatement forEach = new CodeForStatement(body11, iterVarNames[0], myElementTypePath, source);
		CodeIfStatement ifMatch = new CodeIfStatement(forEach.getBody(), expStr);
		new PortableStatement(ifMatch.getThenBlock(), "result.add( " + iterVarNames[0] + " )");
		return callMethod(oper);
	}

	private CodeExpression callMethod(CodeMethod oper) {
		return new PortableExpression("${self}." + oper.getName() + "(" + CodeBehaviour.paramsToActuals(oper) + ")");
	}

	private CodeExpression createReject(IteratorExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		// generate operation
		CodeMethod oper = buildOperation(exp, isStatic, params);
		CodeForStatement forEach = new CodeForStatement(oper.getBody(), iterVarNames[0], myElementTypePath, source);
		CodeIfStatement ifNotMatch = new CodeIfStatement(forEach.getBody(), new NotExpression(expStr));
		new PortableStatement(ifNotMatch.getThenBlock(), "result.add( " + iterVarNames[0] + " )");
		return callMethod(oper);
	}

	private CodeMethod buildOperation(IteratorExp exp, boolean isStatic, List<CodeParameter> params) {
		CodeMethod oper = null;
		oper = new CodeMethod(operName);
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setReturnType(resultTypePath);
		oper.setResultInitialValue(resultDefault);
		oper.setVisibility(priv);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString());
		return oper;
	}

	private CodeExpression createAny(IteratorExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		CodeMethod oper = new CodeMethod(operName);
		oper.cloneToParameters(params);
		oper.setDeclaringClass(myClass);
		oper.setReturnType(myElementTypePath);
		oper.setResultInitialValue(myListDefault);
		oper.setVisibility(priv);
		oper.setStatic(isStatic);
		oper.setComment("implements " + exp.toString());
		CodeForStatement forEach = new CodeForStatement(oper.getBody(), iterVarNames[0], myElementTypePath, source);
		CodeIfStatement ifMatch = new CodeIfStatement(forEach.getBody(), expStr);
		new SetResultStatement(ifMatch.getThenBlock(), new PortableExpression(iterVarNames[0]));
		return callMethod(oper);
	}

	private CodeExpression createOne(IteratorExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		// generate operation
		CodeMethod oper = buildOperation(exp, isStatic, params);
		oper.setResultInitialValue("false");
		CodeField genr = new CodeField(oper.getBody(), "count", StdlibMap.javaIntegerType);
		genr.setInitialization("0");
		CodeForStatement forEach = new CodeForStatement(oper.getBody(), iterVarNames[0], myElementTypePath, source);
		CodeIfStatement ifMatch = new CodeIfStatement(forEach.getBody(), expStr);
		new PortableStatement(ifMatch.getThenBlock(), "count = count + 1");
		CodeIfStatement ifNotOne = new CodeIfStatement(ifMatch.getThenBlock(), new PortableExpression("count > 1"));
		new SetResultStatement(ifNotOne.getThenBlock(), new PortableExpression("false"));
		new SetResultStatement(oper.getBody(), new PortableExpression("count == 1"));
		return callMethod(oper);
	}

	private CodeExpression createIsUnique(IteratorExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		ClassifierMap map = codeMaps.buildClassifierMap(exp.getBody().getType());
		CodeTypeReference valueType = StdlibMap.javaSequenceType.getCopy();
		valueType.addToElementTypes(map.javaDefaultTypePath());
		CodeTypeReference valueImplType = StdlibMap.javaSequenceImplType.getCopy();
		valueImplType.addToElementTypes(map.javaDefaultTypePath());
		// generate operation
		CodeMethod oper = buildOperation(exp, isStatic, params);
		CodeField valuesInSource = new CodeField(oper.getBody(), "_valuesInSource", valueType);
		valuesInSource.setInitialization(new NewInstanceExpression(valueImplType));
		CodeForStatement forEach = new CodeForStatement(oper.getBody(), iterVarNames[0], myElementTypePath, source);
		CodeIfStatement ifAlreadyFound = new CodeIfStatement(forEach.getBody(), new MethodCallExpression("_valuesInSource.contains", expStr));
		new PortableStatement(ifAlreadyFound.getThenBlock(), "return false");
		new MethodCallStatement(forEach.getBody(), "_valuesInSource.add", expStr);
		new PortableStatement(oper.getBody(), "return true");
		return callMethod(oper);
	}

	private CodeExpression createExists(IteratorExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		CodeMethod oper = buildOperation(exp, isStatic, params);
		oper.setResultInitialValue("false");
		CodeForStatement forEach = new CodeForStatement(oper.getBody(), iterVarNames[0], myElementTypePath, source);
		CodeIfStatement ifMatch = new CodeIfStatement(forEach.getBody(), expStr);
		new SetResultStatement(ifMatch.getThenBlock(), new PortableExpression("true"));
		return callMethod(oper);
	}

	private CodeExpression createForAll(IteratorExp exp, CodeExpression source, boolean isStatic, List<CodeParameter> params) {
		CodeMethod oper = null;
		if (iterVarNames.length == 1) {
			oper = buildOperation(exp, isStatic, params);
			CodeForStatement forEach = new CodeForStatement(oper.getBody(), iterVarNames[0], myElementTypePath, source);
			CodeIfStatement ifNotMatch = new CodeIfStatement(forEach.getBody(), new NotExpression(expStr));
			new PortableStatement(ifNotMatch.getThenBlock(), "return false");
			new PortableStatement(oper.getBody(), "return true");
		} else if (iterVarNames.length == 2) {
			oper = buildOperation(exp, isStatic, params);
			CodeForStatement forOuter = new CodeForStatement(oper.getBody(), iterVarNames[0], myElementTypePath, source);
			CodeForStatement forInner = new CodeForStatement(forOuter.getBody(), iterVarNames[1], myElementTypePath, source);
			CodeIfStatement ifNotMatch = new CodeIfStatement(forInner.getBody(), new NotExpression(expStr));
			new PortableStatement(ifNotMatch.getThenBlock(), "return false");
			new PortableStatement(oper.getBody(), "return true");
		}
		return callMethod(oper);
	}

	private void setVariables(LoopExp exp, boolean isStatic, List<CodeParameter> params) {
		ClassifierMap nodeMap = codeMaps.buildClassifierMap(exp.getType());
		// the return type of the loop operation
		resultTypePath = nodeMap.javaTypePath();
		// the default value that will be returned if all else fails
		resultDefault = nodeMap.defaultValue();
		// the type of the elements in source
		CollectionType sourceType = (CollectionType) exp.getSource().getType();
		elementType = sourceType.getElementType(); // in UML
		ClassifierMap elementMap = codeMaps.buildClassifierMap(elementType);
		myElementTypePath = expGeneratorHelper.makeListType(elementType);
		myListDefault = elementMap.defaultValue();
		iterVarNames = getIterVarNames(exp);
		ExpressionCreator myExpMaker = new ExpressionCreator(codeMaps, myClass, context);
		List<CodeParameter> loopParams = addItersToParams(exp.getIterator(), params);
		expStr = myExpMaker.makeExpression((OCLExpression) exp.getBody(), isStatic, loopParams);
		ClassifierMap map = codeMaps.buildClassifierMap(exp.getBody().getType());
		if (!(exp.getBody().getType() instanceof CollectionType) && EmfPropertyCallHelper.resultsInMany((OCLExpression) exp.getBody())) {
			expType = StdlibMap.javaBagType.getCopy();
			expType.addToElementTypes(map.javaTypePath());
		} else if (map.isJavaPrimitive()) {
			expType = map.javaObjectTypePath();
		} else {
			expType = map.javaTypePath().getCopy();
			if (exp.getBody().getType() instanceof CollectionType && expType.getElementTypes().size() == 1) {
				expType.getElementTypes().get(0).setExtends(true);
			}
		}
		// the name of the loop operation
		operName = exp.getName() + myClass.getUniqueNumber();
	}

	/**
	 * The iterator variable(s) needs to be added to any generated operation implementing the body expression. This
	 * CodeMethod add the iterator variable(s) and replaces any variables that will be eclipsed by another name in the
	 * scope of the body expression.
	 * 
	 */
	private List<CodeParameter> addItersToParams(EList<org.eclipse.ocl.expressions.Variable<Classifier, Parameter>> eList, List<CodeParameter> params) {
		List<CodeParameter> result = new ArrayList<CodeParameter>(params);
		Iterator<?> outerIt = eList.iterator();
		while (outerIt.hasNext()) {
			Variable elem = (Variable) outerIt.next();
			Iterator<?> innerIt = params.iterator();
			while (innerIt.hasNext()) {
				CodeParameter par = (CodeParameter) innerIt.next();
				if (ExpGeneratorHelper.javaFieldName(elem).equals(par.getName())) {
					result.remove(par);
				}
			}
			result.add(expGeneratorHelper.varDeclToCodePar(elem));
		}
		return result;
	}

	private String[] getIterVarNames(LoopExp exp) {
		String[] iterVarNames = new String[exp.getIterator().size()];
		Iterator<?> it = exp.getIterator().iterator();
		int i = 0;
		while (it.hasNext()) {
			Variable iterVar = (Variable) it.next();
			iterVarNames[i] = ExpGeneratorHelper.javaFieldName(iterVar);
			i++;
		}
		return iterVarNames;
	}
}
