package org.pavanecce.common.code.metamodel.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pavanecce.common.code.metamodel.CodeExpression;
import org.pavanecce.common.code.metamodel.CodeTypeReference;

public class StaticMethodCallExpression extends CodeExpression {
	private String methodName;
	private List<CodeExpression> arguments = new ArrayList<CodeExpression>();
	private CodeTypeReference type;

	public StaticMethodCallExpression(CodeTypeReference type, String methodName, CodeExpression... argument) {
		this.type=type;
		this.methodName = methodName;
		this.arguments.addAll(Arrays.asList(argument));
	}

	public StaticMethodCallExpression(CodeTypeReference type, String methodName, List<CodeExpression> args) {
		this.type=type;
		this.methodName = methodName;
		this.arguments.addAll(args);
	}

	public CodeTypeReference getType() {
		return type;
	}

	public String getMethodName() {
		return methodName;
	}

	public List<CodeExpression> getArguments() {
		return arguments;
	}

}
