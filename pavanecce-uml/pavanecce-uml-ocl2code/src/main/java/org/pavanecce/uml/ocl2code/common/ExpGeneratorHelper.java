package org.pavanecce.uml.ocl2code.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ocl.uml.Variable;
import org.eclipse.uml2.uml.Classifier;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.ocl2code.maps.ClassifierMap;

public class ExpGeneratorHelper {
	public UmlToCodeMaps CodeUtil;

	public ExpGeneratorHelper(UmlToCodeMaps CodeUtil) {
		super();
		this.CodeUtil = CodeUtil;
	}

	/**
	 * The let variable needs to be added to any generated operation implementing the body expression. This CodeMethod
	 * adds the let variable and replaces any variable that will be eclipsed by it in the scope of the body expression.
	 * 
	 * @param params
	 * @return
	 */
	public List<CodeParameter> addVarToParams(Variable elem, List<CodeParameter> params) {
		List<CodeParameter> result = new ArrayList<CodeParameter>(params);
		Iterator<?> it = params.iterator();
		while (it.hasNext()) {
			CodeParameter par = (CodeParameter) it.next();
			if (javaFieldName(elem).equals(par.getName())) {
				result.remove(par);
			}
		}
		result.add(varDeclToCodePar(elem));
		return result;
	}

	public CodeParameter varDeclToCodePar(Variable elem) {
		CodeParameter result = new CodeParameter(javaFieldName(elem));
		result.setType(CodeUtil.buildClassifierMap(elem.getType()).javaTypePath());
		return result;
	}

	public static String javaFieldName(Variable elem) {
		return NameConverter.decapitalize(elem.getName());
	}

	public CodeTypeReference makeListType(Classifier elementType) {
		CodeTypeReference myType;
		ClassifierMap elementMap = CodeUtil.buildClassifierMap(elementType);
		if (elementMap.isJavaPrimitive()) {
			myType = elementMap.javaObjectTypePath();
		} else {
			myType = elementMap.javaTypePath();
		}
		return myType;
	}

	public CodeExpression makeListElem(CodeClassifier myClass, Classifier type, CodeExpression argStr) {
		return argStr;
		// ClassifierMap typeMap = codeMaps.buildClassifierMap(type);
		// if(typeMap.isJavaPrimitive()){
		// String myType = typeMap.javaObjectType();
		// argStr = "new " + myType + "(" + argStr + ")";
		// }else{
		// argStr = StringHelpers.addBrackets(argStr);
		// }
		// return argStr;
	}
}
