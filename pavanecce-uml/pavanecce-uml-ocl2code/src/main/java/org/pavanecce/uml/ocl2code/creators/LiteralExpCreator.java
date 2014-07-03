package org.pavanecce.uml.ocl2code.creators;

import java.util.Iterator;
import java.util.List;

import org.eclipse.ocl.expressions.TupleLiteralPart;
import org.eclipse.ocl.uml.BooleanLiteralExp;
import org.eclipse.ocl.uml.CollectionItem;
import org.eclipse.ocl.uml.CollectionLiteralExp;
import org.eclipse.ocl.uml.CollectionLiteralPart;
import org.eclipse.ocl.uml.CollectionRange;
import org.eclipse.ocl.uml.EnumLiteralExp;
import org.eclipse.ocl.uml.LiteralExp;
import org.eclipse.ocl.uml.NullLiteralExp;
import org.eclipse.ocl.uml.NumericLiteralExp;
import org.eclipse.ocl.uml.OCLExpression;
import org.eclipse.ocl.uml.StringLiteralExp;
import org.eclipse.ocl.uml.TupleLiteralExp;
import org.eclipse.ocl.uml.TupleType;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Property;
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
import org.pavanecce.common.code.metamodel.expressions.NotExpression;
import org.pavanecce.common.code.metamodel.expressions.NullExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.common.code.metamodel.expressions.StaticFieldExpression;
import org.pavanecce.common.code.metamodel.statements.CodeIfStatement;
import org.pavanecce.common.code.metamodel.statements.MethodCallStatement;
import org.pavanecce.common.code.metamodel.statements.PortableStatement;
import org.pavanecce.uml.common.ocl.AbstractOclContext;
import org.pavanecce.uml.ocl2code.common.EmfPropertyCallHelper;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;
import org.pavanecce.uml.ocl2code.maps.ClassifierMap;
import org.pavanecce.uml.ocl2code.maps.PropertyMap;
import org.pavanecce.uml.ocl2code.maps.TupleTypeMap;

public class LiteralExpCreator {
	private CodeClassifier myClass = null;
	private UmlToCodeMaps codeMaps;
	private AbstractOclContext context;

	public LiteralExpCreator(UmlToCodeMaps CodeUtil, CodeClassifier myClass, AbstractOclContext context) {
		this.myClass = myClass;
		this.codeMaps = CodeUtil;
		this.context = context;
	}

	public CodeExpression makeExpression(LiteralExp in, boolean isStatic, List<CodeParameter> params) {
		if (in instanceof CollectionLiteralExp) {
			return makeCollectionLiteralExp((CollectionLiteralExp) in, isStatic, params);
		} else if (in instanceof EnumLiteralExp) {
			EnumerationLiteral lit = ((EnumLiteralExp) in).getReferredEnumLiteral();
			ClassifierMap mapper = codeMaps.buildClassifierMap(lit.getEnumeration());
			return new StaticFieldExpression(mapper.javaTypePath(), codeMaps.toCodeLiteral(lit));
		} else if (in instanceof NullLiteralExp) {
			return new NullExpression();
		} else if (in instanceof BooleanLiteralExp || in instanceof NumericLiteralExp) {
			return new PortableExpression(in.toString());
		} else if (in instanceof StringLiteralExp) {
			String text = in.toString().replaceAll("\\'", "\\\"");
			if (text.isEmpty() || text.charAt(0) != '\"') {
				text = "\"" + text + "\"";
			}
			return new PortableExpression(text);
		} else if (in instanceof TupleLiteralExp) {
			return makeTupleLiteral((TupleLiteralExp) in, isStatic, params);
		}
		return null;
	}

	private CodeExpression makeTupleLiteral(TupleLiteralExp in, boolean isStatic, List<CodeParameter> params) {
		TupleTypeMap tupleMap = codeMaps.buildTupleTypeMap((TupleType) in.getType());
		CodeMethod oper = new CodeMethod(myClass, "newTuble" + myClass.getUniqueNumber(), tupleMap.javaTypePath());
		oper.cloneToParameters(params);
		for (TupleLiteralPart<Classifier, Property> part : in.getPart()) {
			ExpressionCreator myExpMaker = new ExpressionCreator(codeMaps, myClass, this.context);
			CodeExpression arg = myExpMaker.makeExpression((OCLExpression) part.getValue(), isStatic, params);
			PropertyMap map = codeMaps.buildStructuralFeatureMap(part.getAttribute());
			new MethodCallStatement(oper.getBody(), map.setter(), arg);
		}
		return new PortableExpression(oper.getName() + "(" + CodeBehaviour.paramsToActuals(oper) + ")");
	}

	private CodeExpression makeCollectionLiteralExp(CollectionLiteralExp exp, boolean isStatic, List<CodeParameter> params) {
		if (exp.getPart().size() == 1 && exp.getPart().get(0) instanceof CollectionItem
				&& EmfPropertyCallHelper.resultsInMany((OCLExpression) ((CollectionItem) exp.getPart().get(0)).getItem())) {
			ExpressionCreator myExpMaker = new ExpressionCreator(codeMaps, myClass, context);
			OCLExpression item = (OCLExpression) ((CollectionItem) exp.getPart().get(0)).getItem();
			return myExpMaker.makeExpression(item, isStatic, params);
		} else if (exp.getPart().size() == 0) {
			myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
			switch (exp.getKind()) {
			case BAG_LITERAL:
				return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".emptyBag");
			case SEQUENCE_LITERAL:
				return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".emptySequence");
			case ORDERED_SET_LITERAL:
				return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".emptyOrderedSet");
			case SET_LITERAL:
				return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".emptySet");
			default:
				return new MethodCallExpression(OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".emptyBag");
			}
		} else {
			// generate a separate operation
			ClassifierMap mapper = codeMaps.buildClassifierMap(exp.getType());
			String operName = "collectionLiteral" + myClass.getUniqueNumber();
			CodeMethod oper = new CodeMethod(operName);
			oper.cloneToParameters(params);
			oper.setDeclaringClass(myClass);
			oper.setReturnType(mapper.javaTypePath());

			oper.setStatic(isStatic);
			oper.setVisibility(CodeVisibilityKind.PRIVATE);
			oper.setComment("implements " + exp.toString());
			createCollectionBody(oper, exp, mapper.javaTypePath(), isStatic, params);
			return new PortableExpression(operName + "(" + CodeBehaviour.paramsToActuals(oper) + ")");
		}
	}

	private void createCollectionBody(CodeMethod oper, CollectionLiteralExp exp, CodeTypeReference myType, boolean isStatic, List<CodeParameter> params) {
		CodeBlock body = oper.getBody();
		ClassifierMap mapper = codeMaps.buildClassifierMap(exp.getType());
		CodeExpression myDefault = mapper.defaultValue();
		oper.setResultInitialValue(myDefault);
		String collectionVarName = "result";
		// NB!! Eclipse qualified associations still see multiplicity[0..1] as a
		// single object, and not a set, therefore
		// an expression invoking the qualified property as a collection will be
		// interpreted as a collection item
		ExpressionCreator myExpMaker = new ExpressionCreator(codeMaps, myClass, context);
		Iterator<?> partsIter = exp.getPart().iterator();
		int i = 0;
		while (partsIter.hasNext()) {
			CollectionLiteralPart part = (CollectionLiteralPart) partsIter.next();
			if (part instanceof CollectionItem) {
				OCLExpression item = (OCLExpression) ((CollectionItem) part).getItem();
				CodeExpression str = myExpMaker.makeExpression(item, isStatic, params);
				new CodeField(body, "value" + i, myType.getElementTypes().get(0).getType()).setInitialization(str);
				NotExpression isNotNull = new NotExpression(new IsNullExpression(new PortableExpression("value" + i)));
				new CodeIfStatement(body, isNotNull, new PortableStatement(collectionVarName + ".add( value" + i + " )"));
			}
			if (part instanceof CollectionRange) {
				myClass.addStdLibToImports(OclStandardLibrary.COLLECTIONS);
				CodeExpression first = myExpMaker.makeExpression((OCLExpression) ((CollectionRange) part).getFirst(), isStatic, params);
				CodeExpression last = myExpMaker.makeExpression((OCLExpression) ((CollectionRange) part).getLast(), isStatic, params);
				new MethodCallStatement(body, OclStandardLibrary.COLLECTIONS.getPhysicalName() + ".rangeOf", new PortableExpression(collectionVarName), first,
						last);
			}
			i++;
		}
	}
}
