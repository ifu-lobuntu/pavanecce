package org.pavanecce.uml.uml2code.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeCollectionKind;
import org.pavanecce.common.code.metamodel.CodeElementType;
import org.pavanecce.common.code.metamodel.CodeEnumeration;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeInterface;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CodeVisibilityKind;
import org.pavanecce.common.code.metamodel.CollectionTypeReference;
import org.pavanecce.common.code.metamodel.PrimitiveTypeReference;
import org.pavanecce.common.code.metamodel.expressions.BinaryOperatorExpression;
import org.pavanecce.common.code.metamodel.expressions.IsNullExpression;
import org.pavanecce.common.code.metamodel.expressions.MethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.NewInstanceExpression;
import org.pavanecce.common.code.metamodel.expressions.NotExpression;
import org.pavanecce.common.code.metamodel.expressions.NullExpression;
import org.pavanecce.common.code.metamodel.expressions.PortableExpression;
import org.pavanecce.common.code.metamodel.expressions.PrimitiveDefaultExpression;
import org.pavanecce.common.code.metamodel.expressions.StaticFieldExpression;
import org.pavanecce.common.code.metamodel.expressions.StaticMethodCallExpression;
import org.pavanecce.common.code.metamodel.expressions.TypeExpression;
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
	public void appendVariableDeclaration(StringBuilder sb, CodeField cf) {
		CodeTypeReference varName = cf.getType();
		if (cf.isStatic()) {
			sb.append("static ");
		}
		if (cf.isConstant()) {
			sb.append("final ");
		}
		appendType(sb, varName);
		sb.append(" ");
		sb.append(cf.getName());
		sb.append(" = ");
		appendInitialization(sb, cf);
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
	public void appendClassDefinition(StringBuilder sb, CodeClass cc) {
		appendPackageAndImports(sb, cc);
		for (AbstractJavaCodeDecorator d : this.decorators) {
			d.appendAdditionalImports(sb, cc);
		}
		for (AbstractJavaCodeDecorator d : this.decorators) {
			d.decorateClassDeclaration(sb, cc);
		}
		appendClassDeclaration(sb, cc);
		sb.append("{\n");
		appendFieldsAndMethodDeclarations(sb, cc);
		sb.append("}\n");
	}

	protected void appendFieldsAndMethodDeclarations(StringBuilder sb, CodeClassifier cc) {
		for (Entry<String, CodeField> fieldEntry : cc.getFields().entrySet()) {
			for (AbstractJavaCodeDecorator d : this.decorators) {
				d.decorateFieldDeclaration(sb, fieldEntry.getValue());
			}
			appendFieldDeclaration(sb, fieldEntry.getValue());
			appendLineEnd(sb);
		}
		appendAdditionalFields(sb, cc);
		for (Entry<String, CodeMethod> methodEntry : cc.getMethods().entrySet()) {
			for (AbstractJavaCodeDecorator d : this.decorators) {
				d.decorateMethodDeclaration(sb, methodEntry.getValue());
			}
			appendMethodDeclaration(sb, methodEntry.getValue());
		}
		appendAdditionalMethods(sb, cc);
	}

	protected void appendAdditionalFields(StringBuilder sb, CodeClassifier cc) {
		for (AbstractJavaCodeDecorator d : this.decorators) {
			d.appendAdditionalFields(sb, cc);
		}
	}

	protected void appendAdditionalMethods(StringBuilder sb, CodeClassifier cc) {
		for (AbstractJavaCodeDecorator d : this.decorators) {
			d.appendAdditionalMethods(sb, cc);
		}

	}

	protected void appendPackageAndImports(StringBuilder sb, CodeClassifier cc) {
		sb.append("package ");
		appendQualifiedName(sb, cc.getPackage());
		appendLineEnd(sb);
		appendImports(sb, cc);
	}

	protected void appendClassDeclaration(StringBuilder sb, CodeClass cc) {
		sb.append("public class ");
		sb.append(cc.getName());
		if (cc.getSuperClass() != null) {
			sb.append(" extends ");
			sb.append(this.toSimpleName(cc.getSuperClass()));
		}
		if (cc.getImplementedInterfaces().size() > 0) {
			sb.append(" implements ");
			Iterator<CodeTypeReference> iterator = cc.getImplementedInterfaces().iterator();
			while (iterator.hasNext()) {
				CodeTypeReference ii = iterator.next();
				sb.append(this.toSimpleName(ii));
				if (iterator.hasNext()) {
					sb.append(", ");
				}
			}

		}
	}

	protected void appendImports(StringBuilder sb, CodeClassifier cc) {
		for (CodeTypeReference r : cc.getImports()) {
			String qualifiedName = this.toQualifiedName(r);
			if (!isInJavaLang(qualifiedName)) {
				sb.append("import ");
				sb.append(qualifiedName);
				appendLineEnd(sb);
			}
		}
	}

	private boolean isInJavaLang(String r) {
		boolean result = r.startsWith("java.lang.") && Character.isUpperCase(r.charAt("java.lang.".length()));
		return result || r.indexOf(".") == -1;
	}

	@Override
	public void appendMethodDeclaration(StringBuilder sb, CodeMethod method) {
		sb.append("  ");
		sb.append(toVisibility(method.getVisibility()));
		CodeTypeReference returnType = method.getReturnType();
		if (returnType == null) {
			sb.append("void");
		} else {
			appendType(sb, returnType);
		}
		sb.append(" ");
		sb.append(method.getName());
		sb.append("(");
		Iterator<CodeParameter> iterator = method.getParameters().iterator();
		while (iterator.hasNext()) {
			CodeParameter codeParameter = (CodeParameter) iterator.next();
			CodeTypeReference paramType = codeParameter.getType();
			appendType(sb, paramType);
			sb.append(" ");
			sb.append(codeParameter.getName());
			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(")");
		if (method.getDeclaringClass() instanceof CodeInterface) {
			sb.append(";\n");
		} else {
			sb.append("{\n");
			appendMethodBody(sb, method);
			sb.append("  }\n");
		}
	}

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
	protected void appendMethodBody(StringBuilder sb, CodeMethod method) {
		if (method.getResult() != null && method.returnsResult()) {
			sb.append("    ");
			CodeTypeReference returnType = method.getReturnType();
			appendType(sb, returnType);
			sb.append(" result = ");
			interpretExpression(sb, method.getResult());
			appendLineEnd(sb);
		}
		appendCodeBlock("    ", sb, method.getBody());
		if (method.getResult() != null && method.returnsResult()) {
			sb.append("    return result;\n");
		}
	}

	@Override
	protected void openFor(StringBuilder sb, CodeTypeReference elemType, String elemName, String collectionExpression) {
		sb.append("for(");
		appendType(sb, elemType);
		sb.append(" ");
		sb.append(elemName);
		sb.append(" : ");
		sb.append(collectionExpression);
		sb.append("){");
	}

	private void appendType(StringBuilder sb, CodeTypeReference returnType) {
		if (returnType.isMapped()) {
			String mappedName = getMappedName(returnType);
			if (mappedName != null) {
				mappedName = mappedName.substring(mappedName.lastIndexOf(".") + 1, mappedName.length());
			}
			sb.append(mappedName);
		} else {
			sb.append(returnType.getLastName());
		}
		if (returnType.getElementTypes().size() > 0) {
			appendElementTypes(sb, returnType);
		}
	}

	protected void appendElementTypes(StringBuilder sb, CodeTypeReference returnType) {
		sb.append("<");
		Iterator<CodeElementType> iterator = returnType.getElementTypes().iterator();
		while (iterator.hasNext()) {
			CodeElementType element = iterator.next();
			if (element.isExtends()) {
				sb.append("? extends ");
			}
			appendType(sb, element.getType());
			if (iterator.hasNext()) {
				sb.append(",");
			}
		}
		sb.append(">");
	}

	@Override
	protected StringBuilder appendLineEnd(StringBuilder sb) {
		return sb.append(";\n");
	}

	@Override
	protected String getVoidType() {
		return "void";
	}

	@Override
	public String getSelf() {
		return "this";
	}

	public void interpretExpression(StringBuilder sb, CodeExpression exp) {
		if (exp instanceof PortableExpression) {
			sb.append(super.applyCommonReplacements((PortableExpression) exp));
		} else if (exp instanceof TypeExpression) {
			TypeExpression te = (TypeExpression) exp;
			switch (te.getKind()) {
			case AS_TYPE:
				sb.append("(");
				sb.append(te.getType().getLastName());
				sb.append(")");
				interpretExpression(sb, te.getArg());
			case IS_TYPE:
				// TODO make this more intelligent
				interpretExpression(sb, te.getArg());
				sb.append(" instanceof ");
				sb.append(te.getType().getLastName());
			case IS_KIND:
				interpretExpression(sb, te.getArg());
				sb.append(" instanceof ");
				sb.append(te.getType().getLastName());
			}
		} else if (exp instanceof IsNullExpression) {
			IsNullExpression ne = (IsNullExpression) exp;
			interpretExpression(sb, ne.getSource());
			sb.append(" == null");
		} else if (exp instanceof NotExpression) {
			NotExpression ne = (NotExpression) exp;
			sb.append("!(");
			interpretExpression(sb, ne.getSource());
			sb.append(")");
		} else if (exp instanceof PrimitiveDefaultExpression) {
			sb.append(defaultValue(((PrimitiveDefaultExpression) exp).getPrimitiveTypeKind()));
		} else if (exp instanceof BinaryOperatorExpression) {
			BinaryOperatorExpression boe = (BinaryOperatorExpression) exp;
			sb.append("( ");
			interpretExpression(sb, boe.getArg1());
			sb.append(" ");
			sb.append(boe.getOperator());
			sb.append(" ");
			interpretExpression(sb, boe.getArg2());
			sb.append(" )");
		} else if (exp instanceof StaticFieldExpression) {
			StaticFieldExpression sfe = (StaticFieldExpression) exp;
			sb.append(sfe.getType().getLastName());
			sb.append(".");
			sb.append(sfe.getFieldName());
		} else if (exp instanceof StaticMethodCallExpression) {
			StaticMethodCallExpression smce = (StaticMethodCallExpression) exp;
			sb.append(smce.getType().getLastName());
			sb.append(".");
			invokeMethod(sb, smce.getArguments(), smce.getMethodName());
		} else if (exp instanceof MethodCallExpression) {
			interpretMethodCallExpression(sb, (MethodCallExpression) exp);
		} else if (exp instanceof NullExpression) {
			sb.append("null");
		} else if (exp instanceof NewInstanceExpression) {
			CodeTypeReference type = ((NewInstanceExpression) exp).getType();
			if (type instanceof CollectionTypeReference) {
				CollectionTypeReference impl = ((CollectionTypeReference) type).getImplementation();
				sb.append("new ");
				sb.append(this.toSimpleName(impl));
				appendElementTypes(sb, type);
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

	}

	@Override
	protected void appendInterfaceDefinition(StringBuilder sb, CodeInterface cc) {
		appendPackageAndImports(sb, cc);
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
		appendFieldsAndMethodDeclarations(sb, cc);
		sb.append("}\n");

	}

	@Override
	protected void appendEnumerationDefinition(StringBuilder sb, CodeEnumeration cc) {
		appendPackageAndImports(sb, cc);
		sb.append("public enum ");
		if (cc.getImplementedInterfaces().size() > 0) {
			sb.append(" implements ");
			Iterator<CodeTypeReference> iterator = cc.getImplementedInterfaces().iterator();
			while (iterator.hasNext()) {
				CodeTypeReference ii = iterator.next();
				sb.append(this.toSimpleName(ii));
				if (iterator.hasNext()) {
					sb.append(", ");
				}
			}

		}
		sb.append("{\n");
		appendFieldsAndMethodDeclarations(sb, cc);
		sb.append("}\n");
		sb.append(cc.getName());
	}

	@Override
	protected String defaultValue(CollectionTypeReference kind) {
		if(kind.getKind()==CodeCollectionKind.SET){
			return "new HashSet<" + elementTypeLastName(kind) +">()";
		}else{
			return "new ArrayList<" + elementTypeLastName(kind) +">()";
		}
	}

	public String elementTypeLastName(CollectionTypeReference kind) {
		CodeTypeReference type = kind.getElementTypes().get(0).getType();
		String mappedName = getMappedName(type);
		if(mappedName!=null){
			return mappedName.substring(mappedName.lastIndexOf(".")+1);
		}
		return type.getLastName();
	}

}
