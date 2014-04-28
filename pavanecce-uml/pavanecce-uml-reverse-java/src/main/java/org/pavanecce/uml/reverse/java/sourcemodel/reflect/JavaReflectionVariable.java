package org.pavanecce.uml.reverse.java.sourcemodel.reflect;

import java.lang.reflect.Field;

import org.pavanecce.uml.reverse.java.sourcemodel.SourceVariable;

public class JavaReflectionVariable extends JavaAnnotated implements SourceVariable {
	private Field binding;

	public JavaReflectionVariable(Field binding, JavaDescriptorFactory factory) {
		super(factory);
		this.binding = binding;
		this.factory = factory;
		init(binding.getAnnotations());
	}

	@Override
	public boolean isEnumConstant() {
		return binding.isEnumConstant();
	}

	@Override
	public String getName() {
		return binding.getName();
	}

}
