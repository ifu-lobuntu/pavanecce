package org.pavanecce.uml.uml2code.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeCollectionKind;
import org.pavanecce.common.code.metamodel.CodeConstructor;
import org.pavanecce.common.code.metamodel.CodeElementType;
import org.pavanecce.common.code.metamodel.CodeEnumeration;
import org.pavanecce.common.code.metamodel.CodeEnumerationLiteral;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeFieldValue;
import org.pavanecce.common.code.metamodel.CodeInterface;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CodeVisibilityKind;
import org.pavanecce.common.code.metamodel.CollectionTypeReference;
import org.pavanecce.common.code.metamodel.LibraryTypeReference;
import org.pavanecce.common.code.metamodel.OclStandardLibrary;
import org.pavanecce.common.code.metamodel.PrimitiveTypeReference;
import org.pavanecce.common.code.metamodel.expressions.BinaryOperatorExpression;
import org.pavanecce.common.code.metamodel.expressions.IsNullExpression;
import org.pavanecce.common.code.metamodel.expressions.LiteralPrimitiveExpression;
import org.pavanecce.common.code.metamodel.expressions.MethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.NewInstanceExpression;
import org.pavanecce.common.code.metamodel.expressions.NotExpression;
import org.pavanecce.common.code.metamodel.expressions.NullExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.common.code.metamodel.expressions.PrimitiveDefaultExpression;
import org.pavanecce.common.code.metamodel.expressions.ReadFieldExpression;
import org.pavanecce.common.code.metamodel.expressions.StaticFieldExpression;
import org.pavanecce.common.code.metamodel.expressions.StaticMethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.TypeExpression;
import org.pavanecce.common.code.metamodel.statements.AssignmentStatement;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.common.util.OclCollections;
import org.pavanecce.common.util.OclMath;
import org.pavanecce.common.util.OclPrimitives;
import org.pavanecce.uml.uml2code.AbstractCodeGenerator;
import org.pavanecce.uml.uml2code.jpa.AbstractJavaCodeDecorator;

public class JavaCodeGenerator extends AbstractCodeGenerator {
	Map<CodeTypeReference, String> mappedJavaTypes = new HashMap<CodeTypeReference, String>();
	private List<AbstractJavaCodeDecorator> decorators = new ArrayList<AbstractJavaCodeDecorator>();

	public void addDecorator(AbstractJavaCodeDecorator decorator) {
		this.decorators.add(decorator);
	}

	public void map(CodeTypeReference ctr, String javaName) {
		mappedJavaTypes.put(ctr, javaName);
	}

	@Override
	public JavaCodeGenerator appendVariableDeclaration(CodeField cf) {
		CodeTypeReference varName = cf.getType();
		if (cf.isStatic()) {
			sb.append("static ");
		}
		if (cf.isConstant()) {
			sb.append("final ");
		}
		appendType(varName);
		sb.append(" ");
		sb.append(cf.getName());
		sb.append(" = ");
		appendInitialization(cf);
		return this;
	}

	@Override
	protected void appendAssignmentStatement(AssignmentStatement statement2) {
		if (statement2.getVariableName().startsWith("${self}")) {
			sb.append("this");
			sb.append(statement2.getVariableName().substring("${self}".length()));
			sb.append("=");
			interpretExpression(statement2.getValue());
		} else {
			sb.append(statement2.getVariableName());
			sb.append(" = ");
			interpretExpression(statement2.getValue());
		}
	}

	@Override
	protected String getMappedName(CodeTypeReference type) {
		if (type instanceof PrimitiveTypeReference) {
			PrimitiveTypeReference ptr = (PrimitiveTypeReference) type;
			String javaPrimitive = type.getMappedType("java");
			if (javaPrimitive == null) {
				switch (ptr.getKind()) {
				case STRING:
					return "java.lang.String";
				case INTEGER:
					return "java.lang.Integer";
				case REAL:
					return "java.lang.Double";
				default:
					return "java.lang.Boolean";
				}
			} else {
				return javaPrimitive;
			}
		} else if (type instanceof LibraryTypeReference) {
			Enum<?> kind = ((LibraryTypeReference) type).getKind();
			if (kind instanceof OclStandardLibrary) {
				switch ((OclStandardLibrary) kind) {
				case COLLECTIONS:
					return OclCollections.class.getName();
				case MATH:
					return OclMath.class.getName();
				case PRIMITIVES:
					return OclPrimitives.class.getName();
				case FORMATTER:
					return ">????";
				}
			}
		} else if (type instanceof CollectionTypeReference) {
			CollectionTypeReference ctr = (CollectionTypeReference) type;
			if (ctr.isImplementation()) {
				// /mmm using twowaysets
				switch (ctr.getKind()) {
				case BAG:
					return "java.util.ArrayList";
				case SEQUENCE:
					return "java.util.ArrayList";
				case ORDERED_SET:
					return "java.util.ArrayList";
				default:
					return "java.util.HashSet";
				}
			} else {
				switch (ctr.getKind()) {
				case BAG:
					return "java.util.Collection";
				case SEQUENCE:
					return "java.util.List";
				case ORDERED_SET:
					return "java.util.List";
				default:
					return "java.util.Set";
				}
			}
		}
		String string = mappedJavaTypes.get(type);
		if (string == null) {
			return type.getMappedType("java");
		}
		return string;
	}

	@Override
	protected String getLanguage() {
		return "java";
	}

	@Override
	public JavaCodeGenerator appendClassDefinition(CodeClass cc) {
		appendPackageAndImports(cc);
		for (AbstractJavaCodeDecorator d : this.decorators) {
			d.appendAdditionalImports(this, cc);
		}
		for (AbstractJavaCodeDecorator d1 : this.decorators) {
			d1.decorateClassDeclaration(this, cc);
		}
		appendClassDeclaration(cc);
		sb.append("{\n");
		for (AbstractJavaCodeDecorator d1 : this.decorators) {
			d1.appendAdditionalInnerClasses(this, cc);
		}
		appendFieldsAndMethodDeclarations(cc);
		sb.append("}\n");
		return this;
	}

	public String toClassDefinitionOnly(CodeClass cc) {
		pushNewStringBuilder();
		for (AbstractJavaCodeDecorator d : this.decorators) {
			d.decorateClassDeclaration(this, cc);
		}
		appendClassDeclaration(cc);
		sb.append("{\n");
		appendFieldsAndMethodDeclarations(cc);
		sb.append("}\n");
		return popStringBuilder().toString();
	}

	public String toImportsOnly(CodeClass cc) {
		pushNewStringBuilder();
		appendPackageAndImports(cc);
		for (AbstractJavaCodeDecorator d : this.decorators) {
			d.appendAdditionalImports(this, cc);
		}
		return popStringBuilder().toString();
	}

	protected JavaCodeGenerator appendFieldsAndMethodDeclarations(CodeClassifier cc) {
		appendAdditionalFields(cc);
		appendFields(cc);
		appendAdditionalConstructors(cc);
		appendConstructors(cc);
		appendAdditionalMethods(cc);
		appendMethods(cc);
		return this;
	}

	protected void appendAdditionalConstructors(CodeClassifier cc) {
		for (AbstractJavaCodeDecorator d : this.decorators) {
			d.appendAdditionalConstructors(this, cc);
		}
	}

	protected void appendMethods(CodeClassifier cc) {
		for (Entry<String, CodeMethod> methodEntry : cc.getMethods().entrySet()) {
			for (AbstractJavaCodeDecorator d : this.decorators) {
				d.decorateMethodDeclaration(this, methodEntry.getValue());
			}
			appendMethodDeclaration(methodEntry.getValue());
		}
	}

	protected void appendConstructors(CodeClassifier cc) {
		if (cc instanceof CodeClass) {
			for (CodeConstructor c : ((CodeClass) cc).getConstructors().values()) {
				sb.append("  ");
				sb.append(toVisibility(c.getVisibility()));
				sb.append(cc.getName());
				sb.append("(");
				appendParameters(c.getParameters());
				sb.append("){\n");
				appendCodeBlock("  ", c.getBody());
				sb.append("  }\n");
			}
		}
	}

	protected void appendFields(CodeClassifier cc) {
		for (Entry<String, CodeField> fieldEntry : cc.getFields().entrySet()) {
			for (AbstractJavaCodeDecorator d : this.decorators) {
				d.decorateFieldDeclaration(this, fieldEntry.getValue());
			}
			appendFieldDeclaration(fieldEntry.getValue());
			appendLineEnd();
		}
	}

	protected JavaCodeGenerator appendAdditionalFields(CodeClassifier cc) {
		for (AbstractJavaCodeDecorator d : this.decorators) {
			d.appendAdditionalFields(this, cc);
		}
		return this;
	}

	protected JavaCodeGenerator appendAdditionalMethods(CodeClassifier cc) {
		for (AbstractJavaCodeDecorator d : this.decorators) {
			d.appendAdditionalMethods(this, cc);
		}
		return this;

	}

	protected JavaCodeGenerator appendPackageAndImports(CodeClassifier cc) {
		sb.append("package ");
		appendQualifiedName(cc.getPackage());
		appendLineEnd();
		appendImports(cc);
		return this;
	}

	protected JavaCodeGenerator appendClassDeclaration(CodeClass cc) {
		if (cc.getVisibility() == CodeVisibilityKind.PUBLIC) {
			sb.append("public ");
		}
		sb.append("class ");
		sb.append(cc.getName());
		if (cc.getSuperClass() != null) {
			sb.append(" extends ");
			sb.append(this.toSimpleName(cc.getSuperClass()));
		}
		appendImplementedInterfaces(cc.getImplementedInterfaces());
		return this;
	}

	private void appendImplementedInterfaces(SortedSet<CodeTypeReference> implementedInterfaces) {
		if (implementedInterfaces.size() > 0) {
			sb.append(" implements ");
			Iterator<CodeTypeReference> iterator = implementedInterfaces.iterator();
			while (iterator.hasNext()) {
				CodeTypeReference ii = iterator.next();
				sb.append(this.toSimpleName(ii));
				if (iterator.hasNext()) {
					sb.append(", ");
				}
			}

		}
	}

	protected JavaCodeGenerator appendImports(CodeClassifier cc) {
		for (CodeTypeReference r : cc.getImports()) {
			String qualifiedName = this.toQualifiedName(r);
			if (!isInJavaLang(qualifiedName)) {
				sb.append("import ");
				sb.append(qualifiedName);
				appendLineEnd();
			}
		}
		return this;
	}

	private boolean isInJavaLang(String r) {
		boolean result = r.startsWith("java.lang.") && Character.isUpperCase(r.charAt("java.lang.".length()));
		return result || r.indexOf(".") == -1;
	}

	@Override
	public JavaCodeGenerator appendMethodDeclaration(CodeMethod method) {
		sb.append("  ");
		sb.append(toVisibility(method.getVisibility()));
		CodeTypeReference returnType = method.getReturnType();
		if (returnType == null) {
			sb.append("void");
		} else {
			appendType(returnType);
		}
		sb.append(" ");
		sb.append(method.getName());
		sb.append("(");
		List<CodeParameter> parameters = method.getParameters();
		appendParameters(parameters);
		sb.append(")");
		if (method.getDeclaringClass() instanceof CodeInterface) {
			sb.append(";\n");
		} else {
			sb.append("{\n");
			appendMethodBody(method);
			sb.append("  }\n");
		}
		return this;
	}

	protected void appendParameters(List<CodeParameter> parameters) {
		Iterator<CodeParameter> iterator = parameters.iterator();
		while (iterator.hasNext()) {
			CodeParameter codeParameter = iterator.next();
			CodeTypeReference paramType = codeParameter.getType();
			appendType(paramType);
			sb.append(" ");
			sb.append(codeParameter.getName());
			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
	}

	@Override
	public String toVisibility(CodeVisibilityKind k) {
		switch (k) {
		case PRIVATE:
			return "private ";
		case PUBLIC:
			return "public ";
		case PROTECTED:
			return "protected ";
		default:
			return "";
		}
	}

	@Override
	protected JavaCodeGenerator appendMethodBody(CodeMethod method) {
		if (method.getResult() != null && method.returnsResult()) {
			sb.append("    ");
			CodeTypeReference returnType = method.getReturnType();
			appendType(returnType);
			sb.append(" result = ");
			interpretExpression(method.getResult());
			appendLineEnd();
		}
		appendCodeBlock("    ", method.getBody());
		if (method.getResult() != null && method.returnsResult()) {
			sb.append("    return result;\n");
		}
		return this;
	}

	@Override
	protected JavaCodeGenerator openFor(CodeTypeReference elemType, String elemName, String collectionExpression) {
		sb.append("for(");
		appendType(elemType);
		sb.append(" ");
		sb.append(elemName);
		sb.append(" : ");
		sb.append(collectionExpression);
		sb.append("){");
		return this;
	}

	public JavaCodeGenerator appendType(CodeTypeReference returnType) {
		if (returnType.isMapped()) {
			String mappedName = getMappedName(returnType);
			if (mappedName == null) {
				// ???
			} else {
				mappedName = mappedName.substring(mappedName.lastIndexOf(".") + 1, mappedName.length());
			}
			sb.append(mappedName);
		} else {
			sb.append(returnType.getLastName());
		}
		if (returnType.getElementTypes().size() > 0) {
			appendElementTypes(returnType);
		}
		return this;
	}

	protected JavaCodeGenerator appendElementTypes(CodeTypeReference returnType) {
		sb.append("<");
		Iterator<CodeElementType> iterator = returnType.getElementTypes().iterator();
		while (iterator.hasNext()) {
			CodeElementType element = iterator.next();
			if (element.isExtends()) {
				sb.append("? extends ");
			}
			appendType(element.getType());
			if (iterator.hasNext()) {
				sb.append(",");
			}
		}
		sb.append(">");
		return this;
	}

	@Override
	public void appendLineEnd() {
		sb.append(";\n");
	}

	@Override
	protected String getVoidType() {
		return "void";
	}

	@Override
	public String getSelf() {
		return "this";
	}

	@Override
	public JavaCodeGenerator interpretExpression(CodeExpression exp) {
		if (exp instanceof PortableExpression) {
			append(super.applyCommonReplacements((PortableExpression) exp));
		} else if (exp instanceof TypeExpression) {
			TypeExpression te = (TypeExpression) exp;
			switch (te.getKind()) {
			case AS_TYPE:
				append("(").append(te.getType().getLastName()).append(")").interpretExpression(te.getArg());
			case IS_TYPE:
				// TODO make this more intelligent
				interpretExpression(te.getArg());
				sb.append(" instanceof ");
				sb.append(te.getType().getLastName());
			case IS_KIND:
				interpretExpression(te.getArg()).append(" instanceof ").append(te.getType().getLastName());
			}
		} else if (exp instanceof IsNullExpression) {
			IsNullExpression ne = (IsNullExpression) exp;
			interpretExpression(ne.getSource()).append(" == null");
		} else if (exp instanceof LiteralPrimitiveExpression) {
			LiteralPrimitiveExpression ne = (LiteralPrimitiveExpression) exp;
			if (ne.getPrimitiveTypeKind() == CodePrimitiveTypeKind.STRING) {
				append("\"").append(ne.getValue()).append("\"");
			} else {
				append(ne.getValue());
			}
		} else if (exp instanceof NotExpression) {
			NotExpression ne = (NotExpression) exp;
			sb.append("!(");
			interpretExpression(ne.getSource());
			sb.append(")");
		} else if (exp instanceof PrimitiveDefaultExpression) {
			sb.append(defaultValue(((PrimitiveDefaultExpression) exp).getPrimitiveTypeKind()));
		} else if (exp instanceof BinaryOperatorExpression) {
			BinaryOperatorExpression boe = (BinaryOperatorExpression) exp;
			String operator = boe.getOperator();
			if (operator.equals("${equals}")) {
				interpretExpression(boe.getArg1());
				sb.append(".equals(");
				interpretExpression(boe.getArg2());
				sb.append(")");
			} else if (operator.equals("${notEquals}")) {
				sb.append("!");
				interpretExpression(boe.getArg1());
				sb.append(".equals(");
				interpretExpression(boe.getArg2());
				sb.append(")");
			} else {
				if (operator.equals("=")) {
					operator = "==";
				} else if (operator.startsWith("${")) {
					operator = operator.replace("${or}", "||");
					operator = operator.replace("${and}", "&&");
					operator = operator.replace("${or}", "||");
					operator = operator.replace("${or}", "||");
				}
				sb.append("( ");
				interpretExpression(boe.getArg1());
				sb.append(" ");
				sb.append(operator);
				sb.append(" ");
				interpretExpression(boe.getArg2());
				sb.append(" )");
			}
		} else if (exp instanceof StaticFieldExpression) {
			StaticFieldExpression sfe = (StaticFieldExpression) exp;
			sb.append(sfe.getType().getLastName());
			sb.append(".");
			sb.append(sfe.getFieldName());
		} else if (exp instanceof StaticMethodCallExpression) {
			StaticMethodCallExpression smce = (StaticMethodCallExpression) exp;
			sb.append(smce.getType().getLastName());
			sb.append(".");
			invokeMethod(smce.getArguments(), smce.getMethodName());
		} else if (exp instanceof MethodCallExpression) {
			interpretMethodCallExpression((MethodCallExpression) exp);
		} else if (exp instanceof ReadFieldExpression) {
			sb.append("this.");
			sb.append(((ReadFieldExpression) exp).getFieldName());
		} else if (exp instanceof NullExpression) {
			sb.append("null");
		} else if (exp instanceof NewInstanceExpression) {
			CodeTypeReference type = ((NewInstanceExpression) exp).getType();
			if (type instanceof CollectionTypeReference) {
				CollectionTypeReference impl = ((CollectionTypeReference) type).getImplementation();
				sb.append("new ");
				sb.append(this.toSimpleName(impl));
				appendElementTypes(type);
				sb.append("()");
			} else if (type instanceof PrimitiveTypeReference) {
				CodePrimitiveTypeKind kind = ((PrimitiveTypeReference) type).getKind();
				sb.append(defaultValue(kind));
			} else {
				sb.append("new ");
				sb.append(this.toSimpleName(type));
				sb.append("()");
			}
		}
		return this;
	}

	@Override
	protected JavaCodeGenerator appendInterfaceDefinition(CodeInterface cc) {
		appendPackageAndImports(cc);
		sb.append("public interface ");
		sb.append(cc.getName());
		if (cc.getSuperInterfaces().size() > 0) {
			sb.append(" extends ");
			Iterator<CodeTypeReference> iterator = cc.getSuperInterfaces().iterator();
			while (iterator.hasNext()) {
				CodeTypeReference ii = iterator.next();
				sb.append(this.toSimpleName(ii));
				if (iterator.hasNext()) {
					sb.append(", ");
				}
			}
		}
		sb.append("{\n");
		appendFieldsAndMethodDeclarations(cc);
		sb.append("}\n");
		return this;

	}

	@Override
	protected JavaCodeGenerator appendEnumerationDefinition(CodeEnumeration cc) {
		appendPackageAndImports(cc);
		append("public enum ").append(cc.getName());
		appendImplementedInterfaces(cc.getImplementedInterfaces());
		append("{\n");
		int numberOfFieldValues = appendEnumerationLiterals(cc);
		appendEnumerationConstructor(cc, numberOfFieldValues);
		appendFieldsAndMethodDeclarations(cc);
		append("}\n");
		return this;
	}

	private void appendEnumerationConstructor(CodeEnumeration cc, int numberOfFieldValues) {
		if (numberOfFieldValues > 0) {
			append("  private ").append(cc.getName()).append("(");
			Collection<CodeFieldValue> fieldValues = cc.getLiterals().get(0).getFieldValues().values();
			Iterator<CodeFieldValue> fieldValueIter = fieldValues.iterator();
			while (fieldValueIter.hasNext()) {
				CodeFieldValue cfv = fieldValueIter.next();
				append(typeLastName(cfv.getField().getType())).append(" ").append(cfv.getField().getName());
				if (fieldValueIter.hasNext()) {
					append(", ");
				}
			}
			append("){\n");
			for (CodeFieldValue cfv : fieldValues) {
				String name = cfv.getField().getName();
				String capitalized = NameConverter.capitalize(name);
				append("    this.").append("set").append(capitalized).append("(").append(name).append(")").appendLineEnd();
			}
			append("  }\n");
		}
	}

	private int appendEnumerationLiterals(CodeEnumeration cc) {
		Iterator<CodeEnumerationLiteral> litIter = cc.getLiterals().iterator();
		int numberOfFieldValues = -1;
		while (litIter.hasNext()) {
			CodeEnumerationLiteral cel = (CodeEnumerationLiteral) litIter.next();
			append("  ").append(cel.getName());
			if (numberOfFieldValues == -1) {
				numberOfFieldValues = cel.getFieldValues().size();
			}
			if (numberOfFieldValues != cel.getFieldValues().size()) {
				throw new IllegalStateException("All enumeration literals for " + cc.getName() + "must have " + numberOfFieldValues + " field values");
			}
			if (cel.getFieldValues().size() > 0) {
				append("(");
				Iterator<CodeFieldValue> iterator = cel.getFieldValues().values().iterator();
				while (iterator.hasNext()) {
					CodeFieldValue codeFieldValue = iterator.next();
					this.interpretExpression(codeFieldValue.getValue());
					if (iterator.hasNext()) {
						append(", ");
					}
				}
				append(")");
			}
			if (litIter.hasNext()) {
				append(",\n");
			}
		}
		append(";\n");
		return numberOfFieldValues;
	}

	@Override
	protected String defaultValue(CollectionTypeReference kind) {
		if (kind.getKind() == CodeCollectionKind.SET) {
			return "new HashSet<" + elementTypeLastName(kind) + ">()";
		} else {
			return "new ArrayList<" + elementTypeLastName(kind) + ">()";
		}
	}

	public String elementTypeLastName(CollectionTypeReference kind) {
		CodeTypeReference type = kind.getElementTypes().get(0).getType();
		return typeLastName(type);
	}

	public String typeLastName(CodeTypeReference type) {
		String mappedName = getMappedName(type);
		if (mappedName != null) {
			return mappedName.substring(mappedName.lastIndexOf(".") + 1);
		}
		return type.getLastName();
	}

	@Override
	public JavaCodeGenerator append(String string) {
		super.append(string);
		return this;
	}

}
