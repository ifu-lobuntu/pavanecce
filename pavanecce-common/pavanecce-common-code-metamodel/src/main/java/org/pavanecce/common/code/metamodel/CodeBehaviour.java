package org.pavanecce.common.code.metamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CodeBehaviour extends CodeElement {

	protected CodeClassifier declaringClass;
	private List<CodeParameter> parameters = new ArrayList<CodeParameter>();
	private CodeVisibilityKind visibility = CodeVisibilityKind.PUBLIC;
	private String identifier;
	private CodeBlock body;

	public static String generateIdentifier(String name, List<?> parameters) {
		StringBuilder sb = new StringBuilder(name);
		Iterator<?> iterator = parameters.iterator();
		sb.append("(");
		while (iterator.hasNext()) {
			Object p = iterator.next();
			CodeTypeReference type = null;
			if (p instanceof CodeParameter) {
				type = ((CodeParameter) p).getType();
			} else if (p instanceof CodeTypeReference) {
				type = (CodeTypeReference) p;
			}
			sb.append(type.getLastName());
			if (iterator.hasNext()) {
				sb.append(",");
			}

		}
		sb.append(")");
		String string = sb.toString();
		return string;
	}

	public CodeVisibilityKind getVisibility() {
		return visibility;
	}

	public void setDeclaringClass(CodeClassifier declaringClass) {
		this.declaringClass = declaringClass;
	}

	protected String generateIdentifier() {
		if (this.identifier == null) {
			identifier = generateIdentifier(this.getName(), this.getParameters());
			parameters = Collections.unmodifiableList(getParameters());
		} else {
			throw new IllegalStateException("Method identifier can only be generated once");
		}
		return this.identifier;
	}

	public static StringBuilder paramsToActuals(CodeMethod op) {
		StringBuilder result = new StringBuilder();
		Iterator<CodeParameter> it = op.getParameters().iterator();
		boolean first = true;
		while (it.hasNext()) {
			CodeParameter elem = it.next();
			if (first) {
				first = false;
			} else {
				result.append(", ");
			}
			result.append(elem.getName());
		}
		return result;
	}

	public CodeBlock getBody() {
		return body;
	}

	public List<CodeParameter> getParameters() {
		return parameters;
	}

	public CodeClassifier getDeclaringClass() {
		return declaringClass;
	}

	public void addParam(String name, CodeTypeReference paramType) {
		this.parameters.add(new CodeParameter(name, paramType));
	}

	public void setVisibility(CodeVisibilityKind v) {
		this.visibility = v;

	}

	public CodeBehaviour(String name2) {
		super(name2);
		body = new CodeBlock(getName() + "Body");

	}

	public CodeBehaviour(CodeClassifier clss, String name) {
		this(name);
		setDeclaringClass(clss);
	}

	@Override
	public String toString() {
		if (this.declaringClass != null) {
			return declaringClass.getName() + identifier + "." + generateIdentifier(getName(), getParameters());
		} else {
			return generateIdentifier(getName(), getParameters());
		}
	}

}