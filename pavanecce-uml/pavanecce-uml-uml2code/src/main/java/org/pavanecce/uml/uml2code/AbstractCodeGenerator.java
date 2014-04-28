package org.pavanecce.uml.uml2code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.pavanecce.common.code.metamodel.CodeBlock;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeEnumeration;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeInterface;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.CodePackageReference;
import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CodeVisibilityKind;
import org.pavanecce.common.code.metamodel.PrimitiveTypeReference;
import org.pavanecce.common.code.metamodel.expressions.MethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.NullExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.common.code.metamodel.statement.AssignmentStatement;
import org.pavanecce.common.code.metamodel.statement.CodeForStatement;
import org.pavanecce.common.code.metamodel.statement.CodeIfStatement;
import org.pavanecce.common.code.metamodel.statement.CodeSimpleStatement;
import org.pavanecce.common.code.metamodel.statement.CodeStatement;
import org.pavanecce.common.code.metamodel.statement.MappedStatement;
import org.pavanecce.common.code.metamodel.statement.MethodCallStatement;
import org.pavanecce.common.code.metamodel.statement.PortableStatement;

/**
 * Contract: appendXXX methods never end with lineEnds
 * 
 */
public abstract class AbstractCodeGenerator {

	public AbstractCodeGenerator() {
		super();
	}

	public abstract String getSelf();

	protected abstract String toVisibility(CodeVisibilityKind k);

	protected abstract StringBuilder appendLineEnd(StringBuilder sb);

	protected abstract void appendMethodBody(StringBuilder sb, CodeMethod method);

	public String toMethodBody(CodeMethod m) {
		StringBuilder sb = new StringBuilder();
		appendMethodBody(sb, m);
		return sb.toString();
	}

	public abstract void appendMethodDeclaration(StringBuilder sb, CodeMethod method);

	public abstract void appendClassDefinition(StringBuilder sb, CodeClass cc);

	public abstract void appendVariableDeclaration(StringBuilder sb, CodeField cf);

	public String toClassifierDeclaration(CodeClassifier cc) {
		StringBuilder sb = new StringBuilder();
		appendClassifierDefinition(sb, cc);
		return sb.toString();
	}

	protected void appendClassifierDefinition(StringBuilder sb, CodeClassifier value) {
		if (value instanceof CodeClass) {
			appendClassDefinition(sb, (CodeClass) value);
		} else if (value instanceof CodeEnumeration) {
			appendEnumerationDefinition(sb, (CodeEnumeration) value);
		} else if (value instanceof CodeInterface) {
			appendInterfaceDefinition(sb, (CodeInterface) value);
		}

	}

	protected abstract void appendInterfaceDefinition(StringBuilder sb, CodeInterface value);

	protected abstract void appendEnumerationDefinition(StringBuilder sb, CodeEnumeration value);

	public String toQualifiedName(CodeClass cc) {
		StringBuilder sb = new StringBuilder();
		appendQualifiedName(sb, cc.getPackage());
		sb.append(".");
		sb.append(cc.getName());
		return sb.toString();
	}

	public void appendQualifiedName(StringBuilder sb, CodePackage cp) {
		List<String> qn = cp.getPackageReference().getQualifiedNameInLanguage(getLanguage());
		Iterator<String> iterator = qn.iterator();
		while (iterator.hasNext()) {
			String string = iterator.next();
			sb.append(string);
			if (iterator.hasNext()) {
				sb.append('.');
			}

		}
	}

	protected String getLanguage() {
		return "nothing";
	}

	public String toQualifiedName(CodeTypeReference type) {
		String mappedName = getMappedName(type);
		if (mappedName == null) {
			StringBuilder sb = new StringBuilder();
			appendQualifiedName(sb, type.getCodePackageReference());
			sb.append(".");
			sb.append(type.getLastName());
			return sb.toString();
		} else {
			return mappedName;
		}
	}

	public void appendQualifiedName(StringBuilder sb, CodeTypeReference type) {
		String mappedType = getMappedName(type);
		if (mappedType != null) {
			sb.append(mappedType);
		} else {
			if (type == null) {
				sb.append(getVoidType());
			} else {
				appendQualifiedName(sb, type.getCodePackageReference());
				sb.append(".");
				sb.append(type.getLastName());
			}
		}
	}

	protected void appendQualifiedName(StringBuilder sb, CodePackageReference ref) {
		Iterator<String> iter = ref.getQualifiedNameInLanguage(getLanguage()).iterator();
		while (iter.hasNext()) {
			String string = (String) iter.next();
			sb.append(string);
			if (iter.hasNext()) {
				sb.append(".");
			}

		}

	}

	protected abstract String getVoidType();

	protected abstract String getMappedName(CodeTypeReference type);

	public String toCodeBlock(CodeBlock body) {
		StringBuilder sb = new StringBuilder();
		appendCodeBlock("", sb, body);
		return sb.toString();
	}

	public String toMethodDeclaration(CodeMethod value) {
		StringBuilder sb = new StringBuilder();
		appendMethodDeclaration(sb, value);
		return sb.toString();
	}

	public void appendStatement(String padding, StringBuilder sb, CodeStatement st) {
		if (st instanceof CodeSimpleStatement) {
			sb.append(padding);
			appendSimpleStatement(sb, (CodeSimpleStatement) st);
		} else if (st instanceof CodeIfStatement) {
			sb.append(padding);
			CodeIfStatement ifStatement = (CodeIfStatement) st;
			StringBuilder condition = new StringBuilder();
			interpretExpression(condition, ifStatement.getCondition());
			openIf(sb, condition.toString());
			sb.append("\n");
			appendCodeBlock(padding + "  ", sb, ifStatement.getThenBlock());
			if (ifStatement.hasElse()) {
				sb.append(padding);
				openElse(sb);
				sb.append("\n");
				appendCodeBlock(padding + "  ", sb, ifStatement.getElseBlock());
			}
			sb.append(padding);
			closeIf(sb);
			sb.append("\n");
		} else if (st instanceof CodeForStatement) {
			sb.append(padding);
			CodeForStatement forStatement = (CodeForStatement) st;
			StringBuilder collectionExpression = new StringBuilder();
			interpretExpression(collectionExpression, forStatement.getCollectionExpression());
			openFor(sb, forStatement.getElemType(), forStatement.getElemName(), collectionExpression.toString());
			sb.append("\n");
			appendCodeBlock(padding + "  ", sb, forStatement.getBody());
			sb.append(padding);
			closeFor(sb);
			sb.append("\n");
		}
	}

	protected void closeFor(StringBuilder sb) {
		sb.append("}");
	}

	protected void closeIf(StringBuilder sb) {
		sb.append("}");
	}

	protected void openIf(StringBuilder sb, String condition) {
		sb.append("if(");
		sb.append(condition);
		sb.append("){");
	}

	protected void openElse(StringBuilder sb) {
		sb.append("} else {");
	}

	protected void appendSimpleStatement(StringBuilder sb, CodeSimpleStatement statement) {
		if (statement instanceof PortableStatement) {
			sb.append(applyCommonReplacements((PortableStatement) statement));
		} else if (statement instanceof MethodCallStatement) {
			MethodCallStatement ms = (MethodCallStatement) statement;
			invokeMethod(sb, ms.getArguments(), ms.getMethodName());
		} else if (statement instanceof AssignmentStatement) {
			sb.append(((AssignmentStatement) statement).getVariableName());
			sb.append(" = ");
			interpretExpression(sb, ((AssignmentStatement) statement).getValue());
		} else {
			sb.append("Not Supported: " + statement.getClass());
		}
	}

	protected String applyCommonReplacements(PortableExpression textStatement) {
		return textStatement.getExpression().replaceAll("\\$\\{self\\}", getSelf());
	}

	protected String applyCommonReplacements(PortableStatement textStatement) {
		return textStatement.getStatement().replaceAll("\\$\\{self\\}", getSelf());
	}

	public String toFieldDeclaration(CodeField cf) {
		StringBuilder sb = new StringBuilder();
		appendFieldDeclaration(sb, cf);
		appendLineEnd(sb);
		return sb.toString();
	}

	protected void appendFieldDeclaration(StringBuilder sb, CodeField cf) {
		sb.append("  ");
		sb.append(toVisibility(cf.getVisibility()));
		appendVariableDeclaration(sb, cf);
	}

	public void appendCodeBlock(String padding, StringBuilder sb, CodeBlock body) {
		for (CodeField codeField : body.getLocals()) {
			sb.append(padding);
			appendVariableDeclaration(sb, codeField);
			appendLineEnd(sb);
		}
		for (CodeStatement statement : body.getStatements()) {
			if (statement instanceof MappedStatement) {
				String l = ((MappedStatement) statement).getStatementInLanguage(getLanguage());
				if (l != null) {
					BufferedReader bufferedReader = new BufferedReader(new StringReader(l));
					String line = null;
					try {
						while ((line = bufferedReader.readLine()) != null) {
							sb.append(padding);
							sb.append(line);
							sb.append("\n");
						}
					} catch (IOException e) {
					}finally{
						
					}
				}
			} else {
				appendStatement(padding, sb, statement);
				if (statement instanceof CodeSimpleStatement) {
					appendLineEnd(sb);
				}
			}
		}
	}

	protected String defaultValue(CodePrimitiveTypeKind kind) {
		switch (kind) {
		case BOOLEAN:
			return "false";
		case INTEGER:
			return "0";
		case STRING:
			return "\"\"";
		default:
			return "0.0";
		}
	}

	protected String toSimpleName(CodeTypeReference type) {
		if (type == null) {
			return null;
		} else if (type.isMapped()) {
			String qn = toQualifiedName(type);
			return qn.substring(qn.lastIndexOf('.') + 1, qn.length());
		} else {
			return type.getLastName();
		}
	}

	public abstract void interpretExpression(StringBuilder sb, CodeExpression exp);

	protected void appendInitialization(StringBuilder sb, CodeField cf) {
		if (cf.getInitialization() == null) {
			CodeTypeReference type = cf.getType();
			if (type instanceof PrimitiveTypeReference) {
				sb.append(defaultValue(((PrimitiveTypeReference) type).getKind()));
			} else {
				interpretExpression(sb, new NullExpression());
			}
		} else {
			interpretExpression(sb, cf.getInitialization());
		}
	}

	protected void invokeMethod(StringBuilder sb, List<CodeExpression> arguments, String methodName) {
		sb.append(methodName);
		sb.append("(");
		Iterator<CodeExpression> iterator = arguments.iterator();
		while (iterator.hasNext()) {
			CodeExpression arg = iterator.next();
			interpretExpression(sb, arg);
			if (iterator.hasNext()) {
				sb.append(",");
			}
		}
		sb.append(")");
	}

	protected void interpretMethodCallExpression(StringBuilder sb, MethodCallExpression mce) {
		if (mce.getTarget() != null) {
			interpretExpression(sb, mce.getTarget());
			sb.append(".");
		}
		List<CodeExpression> arguments = mce.getArguments();
		String methodName = mce.getMethodName();
		invokeMethod(sb, arguments, methodName);
	}

	protected void openFor(StringBuilder sb, CodeTypeReference elemType, String elemName, String collectionExpression) {
		throw new RuntimeException();
	}

}