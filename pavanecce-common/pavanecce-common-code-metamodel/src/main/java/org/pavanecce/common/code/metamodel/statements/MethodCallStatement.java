package org.pavanecce.common.code.metamodel.statements;

import java.util.Arrays;
import java.util.List;

import org.pavanecce.common.code.metamodel.CodeBlock;
import org.pavanecce.common.code.metamodel.CodeExpression;

public class MethodCallStatement extends CodeSimpleStatement{
	private String methodName;
	private List<CodeExpression> arguments;

	public MethodCallStatement(CodeBlock block, String methodName, CodeExpression ... value) {
		super("",block);
		this.methodName = methodName;
		arguments.addAll(Arrays.asList(value));
	}

	public List<CodeExpression> getArguments() {
		return arguments;
	}

	public String getMethodName() {
		return methodName;
	}

}
