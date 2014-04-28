package org.pavanecce.common.code.metamodel.expressions;

import org.pavanecce.common.code.metamodel.CodeExpression;

public class IsNullExpression extends CodeExpression {
	private CodeExpression source;

	public IsNullExpression(CodeExpression arg1) {
		this.source = arg1;
	}

	public CodeExpression getSource() {
		return source;
	}
}
