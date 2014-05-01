package org.pavanecce.uml.uml2code.javascript;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeEnumeration;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeInterface;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CodeVisibilityKind;
import org.pavanecce.common.code.metamodel.CollectionTypeReference;
import org.pavanecce.common.code.metamodel.PrimitiveTypeReference;
import org.pavanecce.common.code.metamodel.expressions.NewInstanceExpression;
import org.pavanecce.common.code.metamodel.expressions.NullExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.common.code.metamodel.expressions.PrimitiveDefaultExpression;
import org.pavanecce.uml.uml2code.AbstractCodeGenerator;

public class JavaScriptGenerator extends AbstractCodeGenerator {
	private Map<CodeTypeReference, String> mappedJavaScriptTypes = new HashMap<CodeTypeReference, String>();
	{

	}

	public String getModuleDefinition(CodePackage pkg) {
		SortedSet<CodeTypeReference> imports = new TreeSet<CodeTypeReference>();
		StringBuilder sb = new StringBuilder();
		for (Entry<String, CodeClassifier> entry : pkg.getClassifiers().entrySet()) {
			imports.addAll(entry.getValue().getImports());
		}
		// for (CodeTypeReference r : imports) {
		// if (!r.isPeer() && !mappedJavaScriptTypes.containsKey(r)) {
		// appendImport(sb, r);
		// appendLineEnd(sb);
		// }
		// }
		for (Entry<String, CodeClassifier> entry : pkg.getClassifiers().entrySet()) {
			appendClassifierDefinition(sb, entry.getValue());
			sb.append("\n");
		}
		// for (CodeTypeReference r : imports) {
		// if (r.isPeer() && !mappedJavaScriptTypes.containsKey(r)) {
		// appendImport(sb, r);
		// appendLineEnd(sb);
		// }
		// }
		return sb.toString();

	}

	@Override
	public void appendVariableDeclaration(StringBuilder sb, CodeField cf) {
		sb.append(cf.getName());
		sb.append(" = ");
		appendInitialization(sb, cf);
	}

	@Override
	protected String getMappedName(CodeTypeReference type) {
		return mappedJavaScriptTypes.get(type);
	}

	@Override
	public void appendClassDefinition(StringBuilder sb, CodeClass cc) {
		appendClassifierDefinitionImpl(sb, cc);
	}

	protected void appendClassifierDefinitionImpl(StringBuilder sb, CodeClassifier cc) {
		sb.append("function ");
		sb.append(cc.getName());
		sb.append("(){\n");
		for (Entry<String, CodeField> fieldEntry : cc.getFields().entrySet()) {
			sb.append("  this.");
			appendVariableDeclaration(sb, fieldEntry.getValue());
			appendLineEnd(sb);
		}
		for (Entry<String, CodeMethod> methodEntry : cc.getMethods().entrySet()) {
			appendMethodDeclaration(sb, methodEntry.getValue());
		}
		sb.append("}\n");
	}

	@Override
	public void appendMethodDeclaration(StringBuilder sb, CodeMethod method) {
		sb.append("  ");
		sb.append(method.getDeclaringClass().getName());
		sb.append(".prototype.");
		sb.append(method.getName());
		sb.append(" = function(");
		Iterator<CodeParameter> iterator = method.getParameters().iterator();
		while (iterator.hasNext()) {
			CodeParameter codeParameter = (CodeParameter) iterator.next();
			sb.append(codeParameter.getName());
			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("){\n");
		appendMethodBody(sb, method);
		sb.append("  }\n");
	}

	@Override
	protected void appendMethodBody(StringBuilder sb, CodeMethod method) {
		CodeExpression result2 = method.getResult();
		if (result2 != null && method.returnsResult()) {
			sb.append("    var result = ");
			interpretExpression(sb, result2);
			appendLineEnd(sb);
		}
		appendCodeBlock("    ", sb, method.getBody());
		if (result2 != null && method.returnsResult()) {
			sb.append("    return result;\n");
		}
	}

	@Override
	public void interpretExpression(StringBuilder sb, CodeExpression exp) {
		if (exp instanceof PortableExpression) {
			sb.append(super.applyCommonReplacements(((PortableExpression) exp)) );
		} else if (exp instanceof PrimitiveDefaultExpression) {
			sb.append(defaultValue(((PrimitiveDefaultExpression) exp).getPrimitiveTypeKind()));
		} else if (exp instanceof NullExpression) {
			sb.append("null");
		} else if (exp instanceof NewInstanceExpression) {
			CodeTypeReference type = ((NewInstanceExpression) exp).getType();
			if (type instanceof CollectionTypeReference) {
				sb.append( "[]");
			} else if (type instanceof PrimitiveTypeReference) {
				CodePrimitiveTypeKind kind = ((PrimitiveTypeReference) type).getKind();
				sb.append(defaultValue(kind));
			} else {
				sb.append("new ");
				sb.append(this.toSimpleName(type));
				sb.append("()");
			}
		}
	}

	@Override
	protected StringBuilder appendLineEnd(StringBuilder sb) {
		return sb.append(";\n");
	}

	@Override
	protected String getVoidType() {
		return "";
	}

	@Override
	public String getSelf() {
		return "this";
	}

	@Override
	protected String toVisibility(CodeVisibilityKind k) {
		return "";
	}

	@Override
	protected void appendInterfaceDefinition(StringBuilder sb, CodeInterface value) {
		appendClassifierDefinitionImpl(sb, value);
	}

	@Override
	protected void appendEnumerationDefinition(StringBuilder sb, CodeEnumeration value) {
		appendClassifierDefinitionImpl(sb, value);
		
	}

	@Override
	protected String defaultValue(CollectionTypeReference kind) {
		return "[]";
	}

}
