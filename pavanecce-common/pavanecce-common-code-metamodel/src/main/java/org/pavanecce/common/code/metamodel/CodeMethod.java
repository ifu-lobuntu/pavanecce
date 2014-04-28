package org.pavanecce.common.code.metamodel;

import java.util.List;

import org.pavanecce.common.code.metamodel.expressions.PortableExpression;

public class CodeMethod extends CodeBehaviour {

	private boolean isStatic;
	private CodeTypeReference type;
	private CodeExpression result;
	/**
	 * For no-parameter methods
	 */
	public CodeMethod(CodeClassifier clss, String name, CodeTypeReference type2) {
		super(clss, name);
		this.type = type2;
	}

	/**
	 * For no-parameter methods
	 */
	public CodeMethod(String operName) {
		super(operName);

	}

	public CodeMethod(CodeClassifier clss, String name) {
		this(name);
	}

	public CodeMethod(String operName, CodeTypeReference type) {
		this(operName);
		this.type = type;
	}

	public void setResultInitialValue(String result) {
		this.result = new PortableExpression(result);
	}

	public void setResultInitialValue(CodeExpression result) {
		this.result = result;
	}

	public CodeExpression getResult() {
		return result;
	}

	public CodeTypeReference getReturnType() {
		return type;
	}

	public void setReturnType(CodeTypeReference type) {
		this.type = type;
	}

	@Deprecated
	public CodeClassifier getOwner() {
		return getDeclaringClass();
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	public void cloneToParameters(List<CodeParameter> params) {
		for (CodeParameter codeParameter : params) {
			addParam(codeParameter.getName(), codeParameter.getType());
		}

	}

	public boolean returnsResult() {
		return getReturnType() != null;
	}
	public void setDeclaringClass(CodeClassifier declaringClass) {
		super.setDeclaringClass(declaringClass);
		declaringClass.getMethods().put(generateIdentifier(), this);
	}

	public String toString() {
		if (getDeclaringClass() != null) {
			return getDeclaringClass().getName() +  "." + generateIdentifier(getName(), getParameters());
		} else {
			return generateIdentifier(getName(), getParameters());
		}
	}
}
