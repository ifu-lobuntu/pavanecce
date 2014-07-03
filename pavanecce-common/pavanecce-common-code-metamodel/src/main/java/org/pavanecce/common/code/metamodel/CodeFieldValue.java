package org.pavanecce.common.code.metamodel;

public class CodeFieldValue extends CodeElement {

	private CodeExpression value;
	private CodeField field;

	public CodeFieldValue(CodeField field, CodeExpression value) {
		super(field.getName());
		this.value = value;
		this.field = field;
	}

	public CodeExpression getValue() {
		return value;
	}

	public void setValue(CodeExpression value) {
		this.value = value;
	}

	public CodeField getField() {
		return field;
	}
}
