package org.pavanecce.uml.uml2code.javascript;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeEnumeration;
import org.pavanecce.common.code.metamodel.CodeEnumerationLiteral;
import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeFieldValue;
import org.pavanecce.common.code.metamodel.CodeInterface;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.CodeParameter;
import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CodeVisibilityKind;
import org.pavanecce.common.code.metamodel.CollectionTypeReference;
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
import org.pavanecce.common.code.metamodel.statements.SetResultStatement;
import org.pavanecce.uml.uml2code.AbstractCodeGenerator;

public class JavaScriptGenerator extends AbstractCodeGenerator {
	Deque<Object> isInFor = new ArrayDeque<Object>();
	private Map<CodeTypeReference, String> mappedJavaScriptTypes = new HashMap<CodeTypeReference, String>();
	{

	}

	public String getModuleDefinition(CodePackage pkg) {
		SortedSet<CodeTypeReference> imports = new TreeSet<CodeTypeReference>();
		pushNewStringBuilder();
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
			appendClassifierDefinition(entry.getValue());
			sb.append("\n");
		}
		// for (CodeTypeReference r : imports) {
		// if (r.isPeer() && !mappedJavaScriptTypes.containsKey(r)) {
		// appendImport(sb, r);
		// appendLineEnd(sb);
		// }
		// }
		return popStringBuilder().toString();

	}

	@Override
	protected void appendAssignmentStatement(AssignmentStatement statement2) {
		if (statement2.getVariableName().startsWith("${self}")) {
			sb.append("this.set(\"");
			sb.append(statement2.getVariableName().substring("${self}.".length()));
			sb.append("\",");
			interpretExpression(statement2.getValue());
			sb.append(")");
		} else {
			sb.append(statement2.getVariableName());
			sb.append(" = ");
			interpretExpression(statement2.getValue());

			if (statement2 instanceof SetResultStatement && this.isInFor.size() > 0) {
				sb.append(";return false");
			}
		}
	}

	public String typeLastName(CodeTypeReference type) {
		String mappedName = getMappedName(type);
		if (mappedName != null) {
			return mappedName.substring(mappedName.lastIndexOf(".") + 1);
		}
		return type.getLastName();
	}

	@Override
	public JavaScriptGenerator appendVariableDeclaration(CodeField cf) {
		sb.append(cf.getName());
		sb.append(" = ");
		appendInitialization(cf);
		return this;

	}

	@Override
	protected String getMappedName(CodeTypeReference type) {
		return mappedJavaScriptTypes.get(type);
	}

	@Override
	public JavaScriptGenerator appendClassDefinition(CodeClass cc) {
		appendClassifierDefinitionImpl(cc);
		return this;

	}

	protected void appendClassifierDefinitionImpl(CodeClassifier cc) {
		sb.append("var ");
		sb.append(cc.getName());
		sb.append(" = Backbone.Model.extend({\n");
		sb.append("  defaults: {\n");
		Iterator<Entry<String, CodeField>> fieldIter = cc.getFields().entrySet().iterator();
		while (fieldIter.hasNext()) {
			Map.Entry<String, CodeField> entry = fieldIter.next();
			appendFieldDefinition(entry);
			if (fieldIter.hasNext()) {
				sb.append(",\n");
			}
		}
		sb.append("  },\n");
		sb.append("  initialize : function(){\n");
		fieldIter = cc.getFields().entrySet().iterator();
		while (fieldIter.hasNext()) {
			Map.Entry<String, CodeField> entry = fieldIter.next();
			CodeField cf = entry.getValue();
			if (cf.getType() instanceof CollectionTypeReference) {
				sb.append("      var CollectionOf");
				sb.append(cf.getName());
				sb.append(" = Backbone.Collection.extend({model:");
				sb.append(typeLastName(cf.getType().getElementTypes().get(0).getType()));
				sb.append("});\n");
				sb.append("      this.set({");
				sb.append(cf.getName());
				sb.append("Wrapper : new CollectionOf");
				sb.append(cf.getName());
				sb.append("});\n");
			}
		}
		sb.append("  }");
		Set<Entry<String, CodeMethod>> methods = cc.getMethods().entrySet();
		if (methods.isEmpty()) {
			sb.append(",\n");
		} else {
			for (Entry<String, CodeMethod> methodEntry : methods) {
				sb.append(",\n");
				appendMethodDeclaration(methodEntry.getValue());
			}
			sb.append("\n");
		}
		sb.append("})\n");
	}

	protected void appendFieldDefinition(Entry<String, CodeField> fieldEntry) {
		CodeField cf = fieldEntry.getValue();
		if (cf.getType() instanceof CollectionTypeReference) {
			sb.append("    ");
			sb.append(cf.getName());
			sb.append("Wrapper : null,\n");
		}
		sb.append("    ");
		sb.append(cf.getName());
		sb.append(" : ");
		appendInitialization(cf);
	}

	@Override
	public JavaScriptGenerator appendMethodDeclaration(CodeMethod method) {
		sb.append("  ");
		sb.append(method.getName());
		sb.append(" : function(");
		Iterator<CodeParameter> iterator = method.getParameters().iterator();
		while (iterator.hasNext()) {
			CodeParameter codeParameter = iterator.next();
			sb.append(codeParameter.getName());
			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("){\n");
		appendMethodBody(method);
		sb.append("  }");
		return this;

	}

	@Override
	protected JavaScriptGenerator appendMethodBody(CodeMethod method) {
		CodeExpression result2 = method.getResult();
		sb.append("    var self = this");
		this.appendLineEnd();
		if (result2 != null && method.returnsResult()) {
			sb.append("    var result = ");
			interpretExpression(result2);
			appendLineEnd();
		}
		appendCodeBlock("    ", method.getBody());
		if (result2 != null && method.returnsResult()) {
			sb.append("    return result;\n");
		}
		return this;

	}

	@Override
	public JavaScriptGenerator interpretExpression(CodeExpression exp) {
		if (exp instanceof PortableExpression) {
			sb.append(super.applyCommonReplacements(((PortableExpression) exp)));
		} else if (exp instanceof PrimitiveDefaultExpression) {
			sb.append(defaultValue(((PrimitiveDefaultExpression) exp).getPrimitiveTypeKind()));
		} else if (exp instanceof NullExpression) {
			sb.append("null");
		} else if (exp instanceof NewInstanceExpression) {
			CodeTypeReference type = ((NewInstanceExpression) exp).getType();
			if (type instanceof CollectionTypeReference) {
				sb.append("new Backbone.Collection()");
			} else if (type instanceof PrimitiveTypeReference) {
				CodePrimitiveTypeKind kind = ((PrimitiveTypeReference) type).getKind();
				sb.append(defaultValue(kind));
			} else {
				sb.append("new ");
				sb.append(this.toSimpleName(type));
				sb.append("()");
			}
		} else if (exp instanceof TypeExpression) {
			TypeExpression te = (TypeExpression) exp;
			switch (te.getKind()) {
			case AS_TYPE:
				sb.append("(");
				sb.append(te.getType().getLastName());
				sb.append(")");
				interpretExpression(te.getArg());
			case IS_TYPE:
				// TODO make this more intelligent
				interpretExpression(te.getArg());
				sb.append(" instanceof ");
				sb.append(te.getType().getLastName());
			case IS_KIND:
				interpretExpression(te.getArg());
				sb.append(" instanceof ");
				sb.append(te.getType().getLastName());
			}
		} else if (exp instanceof LiteralPrimitiveExpression) {
			LiteralPrimitiveExpression ne = (LiteralPrimitiveExpression) exp;
			if (ne.getPrimitiveTypeKind() == CodePrimitiveTypeKind.STRING) {
				append("\"").append(ne.getValue()).append("\"");
			} else {
				append(ne.getValue());
			}
		} else if (exp instanceof IsNullExpression) {
			IsNullExpression ne = (IsNullExpression) exp;
			sb.append("!");
			interpretExpression(ne.getSource());
		} else if (exp instanceof NotExpression) {
			NotExpression ne = (NotExpression) exp;
			sb.append("!(");
			interpretExpression(ne.getSource());
			sb.append(")");
		} else if (exp instanceof PrimitiveDefaultExpression) {
			sb.append(defaultValue(((PrimitiveDefaultExpression) exp).getPrimitiveTypeKind()));
		} else if (exp instanceof BinaryOperatorExpression) {
			BinaryOperatorExpression boe = (BinaryOperatorExpression) exp;
			if (boe.getOperator().equals("${equals}")) {
				append("( ").interpretExpression(boe.getArg1()).append("===").interpretExpression(boe.getArg2()).append(")");
			} else {
				sb.append("( ");
				interpretExpression(boe.getArg1());
				sb.append(" ");
				sb.append(boe.getOperator());
				sb.append(" ");
				interpretExpression(boe.getArg2());
				sb.append(" )");
			}
		} else if (exp instanceof ReadFieldExpression) {
			sb.append("this.get(\"");
			sb.append(((ReadFieldExpression) exp).getFieldName());
			sb.append("\")");
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

		}
		return this;
	}

	@Override
	protected void appendLineEnd() {
		sb.append(";\n");
	}

	@Override
	protected String getVoidType() {
		return "";
	}

	@Override
	public String getSelf() {
		return isInFor.isEmpty() ? "this" : "self";
	}

	@Override
	protected String toVisibility(CodeVisibilityKind k) {
		return "";
	}

	@Override
	public JavaScriptGenerator append(String string) {
		return (JavaScriptGenerator) super.append(string);
	}

	@Override
	protected JavaScriptGenerator appendInterfaceDefinition(CodeInterface value) {
		appendClassifierDefinitionImpl(value);
		return this;
	}

	@Override
	protected JavaScriptGenerator appendEnumerationDefinition(CodeEnumeration value) {
		appendClassifierDefinitionImpl(value);
		appendLineEnd();
		for (CodeEnumerationLiteral cel : value.getLiterals()) {
			append(value.getName()).append(".").append(cel.getName()).append(" = new ").append(value.getName()).append("()").appendLineEnd();
			Collection<CodeFieldValue> values = cel.getFieldValues().values();
			for (CodeFieldValue cfv : values) {
				append(value.getName()).append(".").append(cel.getName()).append(".set(\"").append(cfv.getField().getName()).append("\",")
						.interpretExpression(cfv.getValue()).append(")").appendLineEnd();
			}
		}
		return this;
	}

	@Override
	protected String defaultValue(CollectionTypeReference kind) {
		return "new Backbone.Collection()";
	}

	@Override
	protected JavaScriptGenerator closeFor() {
		sb.append("return true;});");
		isInFor.pop();
		return this;
	}

	@Override
	protected JavaScriptGenerator openFor(CodeTypeReference elemType, String elemName, String collectionExpression) {
		isInFor.push(new Object());
		sb.append(collectionExpression);
		sb.append(".all(function(");
		sb.append(elemName);
		sb.append("){");
		return this;
	}
}
