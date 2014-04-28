package org.pavanecce.common.code.metamodel.expressions;

import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeTypeReference;

public class StaticFieldExpression extends CodeExpression {
	private String fieldName;
	private CodeTypeReference type;

	public StaticFieldExpression(CodeTypeReference type, String fieldName) {
		this.fieldName = fieldName;
		this.type = type;
	}

	public CodeTypeReference getType() {
		return type;
	}

	public String getFieldName() {
		return fieldName;
	}
}
