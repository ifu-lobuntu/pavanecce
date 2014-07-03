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
import org.pavanecce.common.code.metamodel.CollectionTypeReference;
import org.pavanecce.common.code.metamodel.PrimitiveTypeReference;
import org.pavanecce.common.code.metamodel.expressions.MethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.NullExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.common.code.metamodel.statements.AssignmentStatement;
import org.pavanecce.common.code.metamodel.statements.CodeForStatement;
import org.pavanecce.common.code.metamodel.statements.CodeIfStatement;
import org.pavanecce.common.code.metamodel.statements.CodeSimpleStatement;
import org.pavanecce.common.code.metamodel.statements.CodeStatement;
import org.pavanecce.common.code.metamodel.statements.MappedStatement;
import org.pavanecce.common.code.metamodel.statements.MethodCallStatement;
import org.pavanecce.common.code.metamodel.statements.PortableStatement;

/**
 * Contract: appendXXX methods never end with lineEnds
 * 
 */
public abstract class AbstractCodeGenerator extends AbstractTextGenerator {
	public AbstractCodeGenerator() {
		super();
	}

	public abstract AbstractCodeGenerator interpretExpression(CodeExpression exp);

	public abstract String getSelf();

	protected abstract String toVisibility(CodeVisibilityKind k);

	protected abstract void appendLineEnd();

	protected abstract AbstractCodeGenerator appendMethodBody(CodeMethod method);

	protected abstract AbstractCodeGenerator appendInterfaceDefinition(CodeInterface value);

	protected abstract AbstractCodeGenerator appendEnumerationDefinition(CodeEnumeration value);

	public abstract AbstractCodeGenerator appendMethodDeclaration(CodeMethod method);

	public abstract AbstractCodeGenerator appendClassDefinition(CodeClass cc);

	public abstract AbstractCodeGenerator appendVariableDeclaration(CodeField cf);

	protected abstract String getVoidType();

	protected abstract String defaultValue(CollectionTypeReference kind);

	protected abstract String getMappedName(CodeTypeReference type);

	public String toMethodBody(CodeMethod m) {
		pushNewStringBuilder();
		appendMethodBody(m);
		return popStringBuilder().toString();
	}

	public String toClassifierDeclaration(CodeClassifier cc) {
		pushNewStringBuilder();
		appendClassifierDefinition(cc);
		return popStringBuilder().toString();
	}

	protected AbstractCodeGenerator appendClassifierDefinition(CodeClassifier value) {
		if (value instanceof CodeClass) {
			appendClassDefinition((CodeClass) value);
		} else if (value instanceof CodeEnumeration) {
			appendEnumerationDefinition((CodeEnumeration) value);
		} else if (value instanceof CodeInterface) {
			appendInterfaceDefinition((CodeInterface) value);
		} else if (value instanceof CodeEnumeration) {
			appendEnumerationDefinition((CodeEnumeration) value);
		}
		return this;

	}

	public String toQualifiedName(CodeClass cc) {
		pushNewStringBuilder();
		appendQualifiedName(cc.getPackage());
		sb.append(".");
		sb.append(cc.getName());
		return popStringBuilder().toString();
	}

	public AbstractCodeGenerator appendQualifiedName(CodePackage cp) {
		List<String> qn = cp.getPackageReference().getQualifiedNameInLanguage(getLanguage());
		Iterator<String> iterator = qn.iterator();
		while (iterator.hasNext()) {
			String string = iterator.next();
			sb.append(string);
			if (iterator.hasNext()) {
				sb.append('.');
			}

		}
		return this;
	}

	protected String getLanguage() {
		return "nothing";
	}

	public String toQualifiedName(CodeTypeReference type) {
		String mappedName = getMappedName(type);
		if (mappedName == null) {
			pushNewStringBuilder();
			appendQualifiedName(type.getCodePackageReference());
			sb.append(".");
			sb.append(type.getLastName());
			return popStringBuilder().toString();
		} else {
			return mappedName;
		}
	}

	public AbstractCodeGenerator appendQualifiedName(CodeTypeReference type) {
		String mappedType = getMappedName(type);
		if (mappedType != null) {
			sb.append(mappedType);
		} else {
			if (type == null) {
				sb.append(getVoidType());
			} else {
				appendQualifiedName(type.getCodePackageReference());
				sb.append(".");
				sb.append(type.getLastName());
			}
		}
		return this;
	}

	protected AbstractCodeGenerator appendQualifiedName(CodePackageReference ref) {
		Iterator<String> iter = ref.getQualifiedNameInLanguage(getLanguage()).iterator();
		while (iter.hasNext()) {
			String string = iter.next();
			sb.append(string);
			if (iter.hasNext()) {
				sb.append(".");
			}

		}
		return this;
	}

	public String toCodeBlock(CodeBlock body) {
		pushNewStringBuilder();
		appendCodeBlock("", body);
		return popStringBuilder().toString();
	}

	public String toMethodDeclaration(CodeMethod value) {
		pushNewStringBuilder();
		appendMethodDeclaration(value);
		return popStringBuilder().toString();
	}

	public AbstractCodeGenerator appendStatement(String padding, CodeStatement st) {
		if (st instanceof CodeSimpleStatement) {
			sb.append(padding);
			appendSimpleStatement((CodeSimpleStatement) st);
		} else if (st instanceof CodeIfStatement) {
			sb.append(padding);
			CodeIfStatement ifStatement = (CodeIfStatement) st;
			pushNewStringBuilder();
			interpretExpression(ifStatement.getCondition());
			openIf(popStringBuilder().toString());
			sb.append("\n");
			appendCodeBlock(padding + "  ", ifStatement.getThenBlock());
			if (ifStatement.hasElse()) {
				sb.append(padding);
				openElse();
				sb.append("\n");
				appendCodeBlock(padding + "  ", ifStatement.getElseBlock());
			}
			sb.append(padding);
			closeIf();
			sb.append("\n");
		} else if (st instanceof CodeForStatement) {
			sb.append(padding);
			CodeForStatement forStatement = (CodeForStatement) st;
			pushNewStringBuilder();
			interpretExpression(forStatement.getCollectionExpression());
			openFor(forStatement.getElemType(), forStatement.getElemName(), popStringBuilder().toString());
			sb.append("\n");
			appendCodeBlock(padding + "  ", forStatement.getBody());
			sb.append(padding);
			closeFor();
			sb.append("\n");
		}
		return this;
	}

	protected AbstractCodeGenerator closeFor() {
		sb.append("}");
		return this;
	}

	protected AbstractCodeGenerator closeIf() {
		sb.append("}");
		return this;
	}

	protected AbstractCodeGenerator openIf(String condition) {
		sb.append("if(");
		sb.append(condition);
		sb.append("){");
		return this;
	}

	protected AbstractCodeGenerator openElse() {
		sb.append("} else {");
		return this;
	}

	protected AbstractCodeGenerator appendSimpleStatement(CodeSimpleStatement statement) {
		if (statement instanceof PortableStatement) {
			sb.append(applyCommonReplacements((PortableStatement) statement));
		} else if (statement instanceof MethodCallStatement) {
			MethodCallStatement ms = (MethodCallStatement) statement;
			invokeMethod(ms.getArguments(), ms.getMethodName());
		} else if (statement instanceof AssignmentStatement) {
			appendAssignmentStatement((AssignmentStatement) statement);
		} else {
			sb.append("Not Supported: " + statement.getClass());
		}
		return this;
	}

	protected void appendAssignmentStatement(AssignmentStatement statement2) {
		sb.append(statement2.getVariableName());
		sb.append(" = ");
		interpretExpression(statement2.getValue());
	}

	protected String applyCommonReplacements(PortableExpression textStatement) {
		String expression = textStatement.getExpression();
		return applyCommonReplacements(expression);
	}

	protected String applyCommonReplacements(PortableStatement textStatement) {
		String statement = textStatement.getStatement();
		return applyCommonReplacements(statement);
	}

	protected String applyCommonReplacements(String statement) {
		return statement.replaceAll("\\$\\{self\\}", getSelf());
	}

	public String toFieldDeclaration(CodeField cf) {
		pushNewStringBuilder();
		appendFieldDeclaration(cf);
		appendLineEnd();
		return popStringBuilder().toString();
	}

	protected AbstractCodeGenerator appendFieldDeclaration(CodeField cf) {
		sb.append("  ");
		sb.append(toVisibility(cf.getVisibility()));
		appendVariableDeclaration(cf);
		return this;
	}

	public AbstractCodeGenerator appendCodeBlock(String padding, CodeBlock body) {
		for (CodeField codeField : body.getLocals()) {
			sb.append(padding);
			appendVariableDeclaration(codeField);
			appendLineEnd();
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
					} finally {

					}
				}
			} else {
				appendStatement(padding, statement);
				if (statement instanceof CodeSimpleStatement) {
					appendLineEnd();
				}
			}
		}
		return this;
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

	protected AbstractCodeGenerator appendInitialization(CodeField cf) {
		if (cf.getInitialization() == null) {
			CodeTypeReference type = cf.getType();
			if (type instanceof PrimitiveTypeReference) {
				sb.append(defaultValue(((PrimitiveTypeReference) type).getKind()));
			} else if (type instanceof CollectionTypeReference) {
				sb.append(defaultValue((CollectionTypeReference) type));
			} else {
				interpretExpression(new NullExpression());
			}
		} else {
			interpretExpression(cf.getInitialization());
		}
		return this;
	}

	protected AbstractCodeGenerator invokeMethod(List<CodeExpression> arguments, String methodName) {
		sb.append(this.applyCommonReplacements(methodName));
		sb.append("(");
		Iterator<CodeExpression> iterator = arguments.iterator();
		while (iterator.hasNext()) {
			CodeExpression arg = iterator.next();
			interpretExpression(arg);
			if (iterator.hasNext()) {
				sb.append(",");
			}
		}
		sb.append(")");
		return this;
	}

	protected AbstractCodeGenerator interpretMethodCallExpression(MethodCallExpression mce) {
		if (mce.getTarget() != null) {
			interpretExpression(mce.getTarget());
			sb.append(".");
		}
		List<CodeExpression> arguments = mce.getArguments();
		String methodName = mce.getMethodName();
		invokeMethod(arguments, methodName);
		return this;
	}

	protected AbstractCodeGenerator openFor(CodeTypeReference elemType, String elemName, String collectionExpression) {
		throw new RuntimeException();
	}

}