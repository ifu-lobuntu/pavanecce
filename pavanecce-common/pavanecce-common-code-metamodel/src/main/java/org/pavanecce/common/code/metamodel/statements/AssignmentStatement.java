package org.pavanecce.common.code.metamodel.statements;

import org.pavanecce.common.code.metamodel.CodeBlock;
import org.pavanecce.common.code.metamodel.CodeExpression;

public class AssignmentStatement extends CodeSimpleStatement {
	private String variableName;
	private CodeExpression value;

	public AssignmentStatement(CodeBlock owner, String variableName, CodeExpression value) {
		super(owner);
		init(variableName, value);
	}

	public AssignmentStatement(String variableName, CodeExpression value) {
		super("");
		init(variableName, value);
	}

	private void init(String variableName, CodeExpression value) {
		this.variableName = variableName;
		this.value = value;
	}

	public String getVariableName() {
		return variableName;
	}

	public CodeExpression getValue() {
		return value;
	}

}
