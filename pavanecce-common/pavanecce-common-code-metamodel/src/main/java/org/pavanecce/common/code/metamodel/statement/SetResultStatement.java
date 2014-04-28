package org.pavanecce.common.code.metamodel.statement;

import org.pavanecce.common.code.metamodel.CodeBlock;
import org.pavanecce.common.code.metamodel.CodeExpression;

public class SetResultStatement extends AssignmentStatement {

	public SetResultStatement(CodeBlock block, CodeExpression value) {
		super(block,"result", value);
	}

	public SetResultStatement(CodeExpression value ) {
		super("result",value);
		
	}

}
