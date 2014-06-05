package org.pavanecce.uml.uml2code.python;

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
import org.pavanecce.common.code.metamodel.expressions.ReadFieldExpression;
import org.pavanecce.common.code.metamodel.statements.AssignmentStatement;
import org.pavanecce.uml.uml2code.AbstractCodeGenerator;

public class PythonCodeGenerator extends AbstractCodeGenerator {
	private Map<CodeTypeReference, String> mappedPythonTypes = new HashMap<CodeTypeReference, String>();
	{

	}

	public String getModuleDefinition(CodePackage pkg) {
		SortedSet<CodeTypeReference> imports = new TreeSet<CodeTypeReference>();
		pushNewStringBuilder();
		for (Entry<String, CodeClassifier> entry : pkg.getClassifiers().entrySet()) {
			imports.addAll(entry.getValue().getImports());
		}
		for (CodeTypeReference r : imports) {
			if (!(r instanceof CollectionTypeReference) && !r.isPeer() && !mappedPythonTypes.containsKey(r)) {
				appendImport(sb, r);
				appendLineEnd();
			}
		}
		for (Entry<String, CodeClassifier> entry : pkg.getClassifiers().entrySet()) {
			appendClassifierDefinition(entry.getValue());
			sb.append("\n");
		}
		for (CodeTypeReference r : imports) {
			if (!(r instanceof CollectionTypeReference) && r.isPeer() && !mappedPythonTypes.containsKey(r)) {
				if (!r.getCodePackageReference().equals(pkg.getPackageReference())) {
					// TODO figure out why this causes the test to fail
					// Maybe find a mechanism to import it on a need to know basis
					// appendImport(sb, r);
					// appendLineEnd();
				}
			}
		}
		return popStringBuilder().toString();

	}

	protected void appendImport(StringBuilder sb, CodeTypeReference r) {
		sb.append("from ");
		appendQualifiedName(r.getCodePackageReference());
		sb.append(" import ");
		sb.append(r.getLastName());
	}

	@Override
	public PythonCodeGenerator appendVariableDeclaration(CodeField cf) {
		sb.append(cf.getName());
		sb.append(" = ");
		appendInitialization(cf);
		return this;

	}

	@Override
	protected void appendAssignmentStatement(AssignmentStatement statement2) {
		sb.append(applyCommonReplacements(statement2.getVariableName()));
		sb.append(" = ");
		interpretExpression(statement2.getValue());
	}

	@Override
	protected String getMappedName(CodeTypeReference type) {
		return mappedPythonTypes.get(type);
	}

	@Override
	public PythonCodeGenerator appendClassDefinition(CodeClass cc) {
		appendClassifierDefinitionImpl(sb, cc);
		return this;

	}

	protected void appendClassifierDefinitionImpl(StringBuilder sb, CodeClassifier cc) {
		sb.append("class ");
		sb.append(cc.getName());
		sb.append("():\n");
		sb.append("  def __init__(self):\n");
		for (Entry<String, CodeField> fieldEntry : cc.getFields().entrySet()) {
			sb.append("    self.");
			appendVariableDeclaration(fieldEntry.getValue());
			appendLineEnd();
		}
		if (cc.getFields().isEmpty()) {
			sb.append("    pass");
		}
		sb.append("\n");
		for (Entry<String, CodeMethod> methodEntry : cc.getMethods().entrySet()) {
			appendMethodDeclaration(methodEntry.getValue());
		}
		sb.append("\n");
	}

	@Override
	public PythonCodeGenerator appendMethodDeclaration(CodeMethod method) {
		sb.append("  def ");
		sb.append(method.getName());
		sb.append("(self");
		Iterator<CodeParameter> iterator = method.getParameters().iterator();
		while (iterator.hasNext()) {
			sb.append(", ");
			CodeParameter codeParameter = iterator.next();
			sb.append(codeParameter.getName());
		}
		sb.append("):\n");
		appendMethodBody(method);
		sb.append("\n");
		return this;

	}

	@Override
	protected PythonCodeGenerator appendMethodBody(CodeMethod method) {
		if (method.getResult() != null && method.returnsResult()) {
			sb.append("    result = ");
			interpretExpression(method.getResult());
			appendLineEnd();
		}
		appendCodeBlock("    ", method.getBody());
		if (method.getResult() != null && method.returnsResult()) {
			sb.append("    return result\n");
		}
		return this;

	}

	@Override
	public PythonCodeGenerator interpretExpression(CodeExpression exp) {
		if (exp instanceof PortableExpression) {
			sb.append(super.applyCommonReplacements(((PortableExpression) exp)));
		} else if (exp instanceof PrimitiveDefaultExpression) {
			sb.append(defaultValue(((PrimitiveDefaultExpression) exp).getPrimitiveTypeKind()));
		} else if (exp instanceof NullExpression) {
			sb.append("None");
		} else if (exp instanceof ReadFieldExpression) {
			sb.append("self.");
			sb.append(((ReadFieldExpression) exp).getFieldName());
		} else if (exp instanceof NewInstanceExpression) {
			CodeTypeReference type = ((NewInstanceExpression) exp).getType();
			if (type instanceof CollectionTypeReference) {
				sb.append("[]");
			} else if (type instanceof PrimitiveTypeReference) {
				CodePrimitiveTypeKind kind = ((PrimitiveTypeReference) type).getKind();
				sb.append(defaultValue(kind));
			} else {
				sb.append(this.toSimpleName(type));
				sb.append("()");
			}
		}
		return this;
	}

	@Override
	protected void appendLineEnd() {
		sb.append("\n");
	}

	@Override
	protected String getVoidType() {
		return "";
	}

	@Override
	public String getSelf() {
		return "self";
	}

	@Override
	protected String toVisibility(CodeVisibilityKind k) {
		return "";
	}

	@Override
	protected PythonCodeGenerator appendInterfaceDefinition(CodeInterface value) {
		appendClassifierDefinitionImpl(sb, value);
		return this;
	}

	@Override
	protected PythonCodeGenerator appendEnumerationDefinition(CodeEnumeration value) {
		appendClassifierDefinitionImpl(sb, value);
		return this;

	}

	@Override
	protected String defaultValue(CollectionTypeReference kind) {
		return "[]";
	}

}
