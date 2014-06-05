package org.pavanecce.common.code.metamodel;

import java.util.ArrayList;
import java.util.List;

import org.pavanecce.common.code.metamodel.statements.CodeStatement;

public class CodeBlock extends CodeElement {
	private List<CodeField> locals = new ArrayList<CodeField>();
	private List<CodeStatement> statements = new ArrayList<CodeStatement>();

	public CodeBlock(String name2) {
		super(name2);
	}

	public CodeBlock() {
		super("");
	}

	public List<CodeField> getLocals() {
		return locals;
	}

	public List<CodeStatement> getStatements() {
		return statements;
	}
}
