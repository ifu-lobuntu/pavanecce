package org.pavanecce.common.code.metamodel.expressions;

import org.pavanecce.common.code.metamodel.CodeExpression;

public class NotExpression extends CodeExpression {
	private CodeExpression source;

	public NotExpression(CodeExpression source) {
		super();
		this.source = source;
	}

	public CodeExpression getSource() {
		return source;
	};
}
