package org.pavanecce.common.code.metamodel.statements;

import org.pavanecce.common.code.metamodel.CodeBlock;

public class CodeSimpleStatement extends CodeStatement {

	public CodeSimpleStatement(String string, CodeBlock block) {
		super(string, block);
	}

	public CodeSimpleStatement(CodeBlock block) {
		super(block);
	}

	public CodeSimpleStatement(String string) {
		super(string);
	}

}