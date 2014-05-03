package org.pavanecce.common.code.metamodel.expressions;

import org.pavanecce.common.code.metamodel.CodeExpression;

public class ReadFieldExpression extends CodeExpression {
	private String fieldName;
	
	public ReadFieldExpression(String fieldName) {
		super();
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}
}
