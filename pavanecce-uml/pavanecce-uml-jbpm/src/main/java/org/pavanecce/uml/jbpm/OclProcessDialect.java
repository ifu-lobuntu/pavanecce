package org.pavanecce.uml.jbpm;

import org.jbpm.process.builder.ReturnValueEvaluatorBuilder;
import org.jbpm.process.builder.dialect.java.JavaProcessDialect;

public class OclProcessDialect extends JavaProcessDialect {
	ReturnValueEvaluatorBuilder returnValueEvaluatorBuilder = new OclReturnValueEvaluatorBuilder();

	@Override
	public ReturnValueEvaluatorBuilder getReturnValueEvaluatorBuilder() {
		return returnValueEvaluatorBuilder;
	}
}
