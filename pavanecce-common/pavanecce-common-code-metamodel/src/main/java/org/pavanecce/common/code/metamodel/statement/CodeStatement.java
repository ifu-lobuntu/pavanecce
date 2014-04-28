package org.pavanecce.common.code.metamodel.statement;

import org.pavanecce.common.code.metamodel.CodeBlock;
import org.pavanecce.common.code.metamodel.CodeElement;

public class CodeStatement extends CodeElement {

	public CodeStatement( String string,CodeBlock block) {
		super(string);
		block.getStatements().add(this);
	}

	public CodeStatement( CodeBlock block) {
		this("",block);
	}

	public CodeStatement(String string) {
		super(string);
	}

}
