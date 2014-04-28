package org.pavanecce.common.code.metamodel.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pavanecce.common.code.metamodel.CodeExpression;

public class MethodCallExpression extends CodeExpression {
	private String methodName;
	private List<CodeExpression> arguments=new ArrayList<CodeExpression>();
	private CodeExpression target;
	public MethodCallExpression(String methodName, CodeExpression ... argument) {
		this.methodName=methodName;
		this.arguments.addAll(Arrays.asList(argument));
	}
	public MethodCallExpression(CodeExpression target, String methodName,CodeExpression ... argument) {
		this(methodName,argument);
		this.target=target;
	}
	public MethodCallExpression(CodeExpression target, String methodName, List<CodeExpression> args) {
		this(methodName,args);
		this.target=target;
	}
	public MethodCallExpression(String methodName, List<CodeExpression> args) {
		this.methodName=methodName;
		this.arguments.addAll(args);
	}
	public CodeExpression getTarget() {
		return target;
	}
	public String getMethodName() {
		return methodName;
	}
	public List<CodeExpression> getArguments() {
		return arguments;
	}

}
