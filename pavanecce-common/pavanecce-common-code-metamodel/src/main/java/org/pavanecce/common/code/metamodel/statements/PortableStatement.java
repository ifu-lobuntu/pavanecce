package org.pavanecce.common.code.metamodel.statements;

import org.pavanecce.common.code.metamodel.CodeBlock;

public class PortableStatement extends CodeSimpleStatement {
	private String statement;

	public PortableStatement(String statement) {
		super("");
		this.statement = statement;
	}

	public PortableStatement(CodeBlock body, String statement) {
		super(body);
		this.statement = statement;
	}

	public String getStatement() {
		return statement;
	}

}
