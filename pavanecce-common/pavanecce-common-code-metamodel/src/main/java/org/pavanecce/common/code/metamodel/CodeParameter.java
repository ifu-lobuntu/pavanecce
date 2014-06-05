package org.pavanecce.common.code.metamodel;

public class CodeParameter extends CodeElement {

	private CodeMethod method;
	private CodeTypeReference type;

	public CodeParameter(String name, CodeMethod method) {
		super(name);
		this.method = method;
		method.getParameters().add(this);
	}

	public CodeParameter(String name, CodeMethod method, CodeTypeReference type2) {
		this(name, method);
		this.type = type2;
	}

	public CodeParameter(String name, CodeTypeReference paramType) {
		super(name);
		type = paramType;
	}

	/**
	 * For dummy parameters representing local variables
	 * 
	 * @param javaFieldName
	 */
	public CodeParameter(String javaFieldName) {
		super(javaFieldName);
	}

	public CodeMethod getMethod() {
		return method;
	}

	public CodeTypeReference getType() {
		return type;
	}

	public void setType(CodeTypeReference type) {
		this.type = type;
	}

}
