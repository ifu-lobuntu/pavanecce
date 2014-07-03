package org.pavanecce.common.code.metamodel.expressions;

import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;

public class LiteralPrimitiveExpression extends CodeExpression {
	private CodePrimitiveTypeKind primitiveTypeKind;
	private String value;

	public LiteralPrimitiveExpression(CodePrimitiveTypeKind primitiveTypeKind, String value) {
		super();
		this.primitiveTypeKind = primitiveTypeKind;
		this.value = value;
	}

	public CodePrimitiveTypeKind getPrimitiveTypeKind() {
		return primitiveTypeKind;
	}

	public String getValue() {
		return value;
	}
}
