package org.pavanecce.common.code.metamodel.expressions;

import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeTypeReference;

public class NewInstanceExpression extends CodeExpression {
	private CodeTypeReference type;

	public NewInstanceExpression(CodeTypeReference type) {
		super();
		this.type = type;
	}
	public CodeTypeReference getType() {
		return type;
	}
}
