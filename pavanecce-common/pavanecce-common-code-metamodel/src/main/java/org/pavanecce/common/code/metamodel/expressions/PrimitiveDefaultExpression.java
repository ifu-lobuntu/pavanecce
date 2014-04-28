package org.pavanecce.common.code.metamodel.expressions;

import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;

public class PrimitiveDefaultExpression extends CodeExpression {
	private CodePrimitiveTypeKind primitiveTypeKind;

	public PrimitiveDefaultExpression(CodePrimitiveTypeKind primitiveTypeKind) {
		super();
		this.primitiveTypeKind = primitiveTypeKind;
	}

	public CodePrimitiveTypeKind getPrimitiveTypeKind() {
		return primitiveTypeKind;
	}
}
