package org.pavanecce.common.code.metamodel.statements;

import org.pavanecce.common.code.metamodel.CodeBlock;
import org.pavanecce.common.code.metamodel.CodeExpression;

public class CodeIfStatement extends CodeStatement {
	private CodeExpression condition;
	private CodeBlock thenBlock = new CodeBlock();
	private CodeBlock elseBlock ;

	public CodeIfStatement(CodeBlock block, CodeExpression condition, CodeStatement thenStatement) {
		this(block,condition);
		this.thenBlock.getStatements().add(thenStatement);
	}
	public CodeIfStatement(CodeBlock block, CodeExpression condition) {
		super("");
		block.getStatements().add(this);
		this.condition = condition;
	}
	public boolean hasElse(){
		return elseBlock!=null;
	}

	public CodeExpression getCondition() {
		return condition;
	}

	public CodeBlock getThenBlock() {
		return thenBlock;
	}
	public CodeBlock getElseBlock() {
		return elseBlock==null?elseBlock=new CodeBlock():elseBlock;
	}

}
