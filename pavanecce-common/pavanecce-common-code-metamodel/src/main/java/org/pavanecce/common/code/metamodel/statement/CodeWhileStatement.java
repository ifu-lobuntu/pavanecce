package org.pavanecce.common.code.metamodel.statement;

import org.pavanecce.common.code.metamodel.CodeBlock;
import org.pavanecce.common.code.metamodel.CodeExpression;

public class CodeWhileStatement extends CodeStatement {
	private CodeExpression condition;
	private CodeBlock body = new CodeBlock();

	public CodeWhileStatement(CodeBlock block, CodeExpression condition) {
		super("");
		block.getStatements().add(this);
		this.condition = condition;
	}
	

	public CodeExpression getCondition() {
		return condition;
	}

	public CodeBlock getBody() {
		return body;
	}

}
