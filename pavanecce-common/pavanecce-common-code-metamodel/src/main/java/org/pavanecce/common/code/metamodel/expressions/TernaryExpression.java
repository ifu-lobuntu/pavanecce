package org.pavanecce.common.code.metamodel.expressions;

import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeTypeReference;

public class TernaryExpression extends CodeExpression {
	private CodeExpression condition;
	private CodeExpression thenExpression;
	private CodeExpression elseExpression;
	private CodeTypeReference convertTo;

	public TernaryExpression(CodeExpression condition, CodeExpression thenExpression, CodeExpression elseExpression) {
		super();
		this.condition = condition;
		this.thenExpression = thenExpression;
		this.elseExpression = elseExpression;
	}

	public CodeExpression getCondition() {
		return condition;
	}

	public CodeExpression getThenExpression() {
		return thenExpression;
	}

	public CodeExpression getElseExpression() {
		return elseExpression;
	}

	public CodeTypeReference getConvertTo() {
		return convertTo;
	}

	public void setConvertTo(CodeTypeReference convertTo) {
		this.convertTo = convertTo;
	}

}
