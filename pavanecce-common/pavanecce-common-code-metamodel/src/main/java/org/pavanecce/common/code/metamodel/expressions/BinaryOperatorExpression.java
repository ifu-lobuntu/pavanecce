package org.pavanecce.common.code.metamodel.expressions;

import org.pavanecce.common.code.metamodel.CodeExpression;

public class BinaryOperatorExpression extends CodeExpression {
	private CodeExpression arg1;
	private CodeExpression arg2;
	private String operator;

	public BinaryOperatorExpression(CodeExpression arg1, String operator, CodeExpression arg2) {
		super();
		this.arg1 = arg1;
		this.operator = operator;
		this.arg2 = arg2;
	}

	public CodeExpression getArg1() {
		return arg1;
	}

	public CodeExpression getArg2() {
		return arg2;
	}

	public String getOperator() {
		return operator;
	};
	
}
