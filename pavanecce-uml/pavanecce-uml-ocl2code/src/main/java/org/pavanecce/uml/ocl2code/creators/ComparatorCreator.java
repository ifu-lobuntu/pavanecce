package org.pavanecce.uml.ocl2code.creators;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Type;
import org.pavanecce.common.code.metamodel.CodeBlock;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CodeVisibilityKind;
import org.pavanecce.common.code.metamodel.PrimitiveTypeReference;
import org.pavanecce.common.code.metamodel.expressions.IsNullExpression;
import org.pavanecce.common.code.metamodel.expressions.MethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.common.code.metamodel.expressions.TypeExpression;
import org.pavanecce.common.code.metamodel.expressions.TypeExpressionKind;
import org.pavanecce.common.code.metamodel.statements.AssignmentStatement;
import org.pavanecce.common.code.metamodel.statements.CodeIfStatement;
import org.pavanecce.common.code.metamodel.statements.PortableStatement;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;
import org.pavanecce.uml.ocl2code.maps.ClassifierMap;
import org.pavanecce.uml.ocl2code.maps.OperationMap;
import org.pavanecce.uml.uml2code.StdlibMap;

public class ComparatorCreator {
	private UmlToCodeMaps codeMaps;

	public ComparatorCreator(UmlToCodeMaps CodeUtil) {
		super();
		this.codeMaps = CodeUtil;
	}

	private String buildComparatorName(Classifier elemType, String identifier) {
		String elemTypeName = codeMaps.buildClassifierMap(elemType).javaTypePath().getLastName();
		return "Compare" + elemTypeName + "On" + identifier;
	}

	public CodeTypeReference makeComparator(Classifier elementType, Classifier exprType, CodeExpression expStr, String iterVarName, String identifier) {
		CodeClass created = new CodeClass(buildComparatorName(elementType, identifier), UtilityCreator.getUtilPack());
		makeCompareOp(created, elementType, exprType, expStr, iterVarName);
		CodeTypeReference result = created.getPathName();
		return result;
	}

	private void makeCompareOp(CodeClass owner, Classifier elemType, Classifier exprType, CodeExpression expString, String iterVarName) {
		ClassifierMap elemTypeMap = codeMaps.buildClassifierMap(elemType);
		ClassifierMap exprTypeMap = codeMaps.buildClassifierMap(exprType);
		CodeTypeReference elemTypeName = elemTypeMap.javaTypePath();
		CodeTypeReference exprTypeName = exprTypeMap.javaTypePath();
		CodeExpression exprDefault = exprTypeMap.defaultValue();
		EList<Type> paramTypes = new BasicEList<Type>();
		paramTypes.add(exprType);
		Operation lessOper = exprType.getOperation("<", null, paramTypes);
		Operation moreOper = exprType.getOperation(">", null, paramTypes);
		String moreOperName = ">";
		if (lessOper != null && moreOper != null) {
			if (lessOper.getType().getName().equals("Boolean") && moreOper.getType().getName().equals("Boolean")) {
				OperationMap mapper = codeMaps.buildOperationMap(lessOper);
				mapper = codeMaps.buildOperationMap(moreOper);
				moreOperName = mapper.javaOperName();
			}
		}
		CodeMethod compare = new CodeMethod("compare");
		compare.addParam("arg0", elemTypeMap.javaTypePath());
		compare.addParam("arg1", elemTypeMap.javaTypePath());
		compare.setDeclaringClass(owner);
		compare.setReturnType(StdlibMap.javaIntegerObjectType);
		compare.setResultInitialValue("0");
		compare.setVisibility(CodeVisibilityKind.PUBLIC);
		IsNullExpression arg0Null = new IsNullExpression(new PortableExpression("arg0"));
		IsNullExpression arg1Null = new IsNullExpression(new PortableExpression("arg1"));
		CodeIfStatement ifArg0Null = new CodeIfStatement(compare.getBody(), arg0Null);
		CodeIfStatement bothNull = new CodeIfStatement(ifArg0Null.getThenBlock(), arg1Null, new PortableStatement("return 0"));
		new PortableStatement(bothNull.getElseBlock(), "return -1");
		CodeIfStatement ifArg1Null = new CodeIfStatement(ifArg0Null.getElseBlock(), arg1Null, new PortableStatement("return 1"));
		CodeBlock noNullsBlock = ifArg1Null.getElseBlock();
		new CodeField(noNullsBlock, "value0", exprTypeName).setInitialization(exprDefault);
		new CodeField(noNullsBlock, "value1", exprTypeName).setInitialization(exprDefault);

		readArg0Value(expString, iterVarName, elemTypeName, noNullsBlock);
		readArg1Value(expString, iterVarName, elemTypeName, noNullsBlock);
		new CodeIfStatement(noNullsBlock, new PortableExpression("value0 == value1"), new PortableStatement("return 0"));

		if (exprTypeName instanceof PrimitiveTypeReference) {
			CodePrimitiveTypeKind primitiveTypeKind = ((PrimitiveTypeReference) exprTypeName).getKind();
			if (primitiveTypeKind == CodePrimitiveTypeKind.BOOLEAN) {
				new CodeIfStatement(noNullsBlock, new PortableExpression("value0"), new PortableStatement("return 1"));
				new CodeIfStatement(noNullsBlock, new PortableExpression("value1"), new PortableStatement("return -1"));
			} else if (primitiveTypeKind == CodePrimitiveTypeKind.STRING) {
				new PortableStatement(noNullsBlock, "return value0.compareTo(value1)");
			} else {
				new CodeIfStatement(noNullsBlock, new PortableExpression("value0 > value1"), new PortableStatement("return 1"));
				new CodeIfStatement(noNullsBlock, new PortableExpression("value1 < value1"), new PortableStatement("return -1"));
			}
		} else {
			MethodCallExpression value0GreaterThanValue1 = new MethodCallExpression("value0." + moreOperName, new PortableExpression("value1"));
			new CodeIfStatement(noNullsBlock, value0GreaterThanValue1, new PortableStatement("return 0"));
			MethodCallExpression value1GreaterThanValue0 = new MethodCallExpression("value1." + moreOperName, new PortableExpression("value0"));
			new CodeIfStatement(noNullsBlock, value1GreaterThanValue0, new PortableStatement("return 0"));
			new PortableStatement(noNullsBlock, "return 0");
		}
	}

	private void readArg0Value(CodeExpression expString, String iterVarName, CodeTypeReference elemTypeName, CodeBlock noNullsBlock) {
		TypeExpression arg0IsInstance = new TypeExpression(elemTypeName, TypeExpressionKind.IS_KIND, new PortableExpression("arg0"));
		CodeIfStatement ifArg0IsInstance = new CodeIfStatement(noNullsBlock, arg0IsInstance);
		TypeExpression arg0AsCorrectType = new TypeExpression(elemTypeName, TypeExpressionKind.AS_TYPE, new PortableExpression("arg0"));
		new CodeField(ifArg0IsInstance.getThenBlock(), iterVarName, elemTypeName).setInitialization(arg0AsCorrectType);
		new AssignmentStatement(ifArg0IsInstance.getThenBlock(), "value0", expString);
	}

	private void readArg1Value(CodeExpression expString, String iterVarName, CodeTypeReference elemTypeName, CodeBlock noNullsBlock) {
		TypeExpression arg1IsInstance = new TypeExpression(elemTypeName, TypeExpressionKind.IS_KIND, new PortableExpression("arg1"));
		CodeIfStatement ifArg1IsInstance = new CodeIfStatement(noNullsBlock, arg1IsInstance);
		TypeExpression arg1AsCorrectType = new TypeExpression(elemTypeName, TypeExpressionKind.AS_TYPE, new PortableExpression("arg1"));
		new CodeField(ifArg1IsInstance.getThenBlock(), iterVarName, elemTypeName).setInitialization(arg1AsCorrectType);
		new AssignmentStatement(ifArg1IsInstance.getThenBlock(), "value1", expString);
	}
}
