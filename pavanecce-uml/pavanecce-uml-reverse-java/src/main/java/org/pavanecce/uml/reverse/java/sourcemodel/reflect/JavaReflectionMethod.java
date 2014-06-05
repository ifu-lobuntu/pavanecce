package org.pavanecce.uml.reverse.java.sourcemodel.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.eclipse.uml2.uml.VisibilityKind;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceCode;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceMethod;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceProperty;

public class JavaReflectionMethod extends JavaAnnotated implements SourceMethod {

	private Method binding;
	private JavaReflectionClass[] parameterTypes;
	private JavaReflectionClass returnType;
	private JavaReflectionClass declaringClass;

	public JavaReflectionMethod(Method binding, JavaDescriptorFactory factory) {
		super(factory);
		this.binding = binding;
		this.factory = factory;
		this.returnType = factory.getClassDescriptor(binding.getGenericReturnType());
		if (this.parameterTypes == null) {
			Type[] source = binding.getGenericParameterTypes();
			this.parameterTypes = new JavaReflectionClass[source.length];
			for (int i = 0; i < source.length; i++) {
				parameterTypes[i] = factory.getClassDescriptor(source[i]);
			}
		}
		declaringClass = factory.getClassDescriptor(binding.getDeclaringClass());
		init(binding.getAnnotations());
	}

	@Override
	public String[] getParameterNames() {
		String[] result = new String[getParameterTypes().length];
		for (int i = 0; i < result.length; i++) {
			result[i] = "param" + i;

		}
		return result;

	}

	@Override
	public VisibilityKind getVisibility() {
		return JavaReflectUtil.toVisibility(binding.getModifiers());
	}

	@Override
	public boolean isConstructor() {
		return false;
	}

	@Override
	public SourceClass getReturnType() {
		return returnType;
	}

	@Override
	public SourceClass[] getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public String getName() {
		return binding.getName();
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(binding.getModifiers());
	}

	@Override
	public SourceCode getSource() {
		return null;
	}

	@Override
	public boolean isAccessor() {
		String propertyName = NameConverter.extractPropertyName(binding.getName());
		SourceProperty pd = declaringClass.getPropertyDescriptors().get(propertyName);
		if (pd == null) {
			if (getName().startsWith("set") && getParameterTypes().length == 1 && getParameterTypes()[0].getName().equals("boolean")) {
				pd = declaringClass.getPropertyDescriptors().get("is" + NameConverter.capitalize(propertyName));
			}
		}
		if (pd instanceof JavaReflectionProperty) {
			JavaReflectionProperty jrp = (JavaReflectionProperty) pd;
			return binding.equals(jrp.getGetter()) || binding.equals(jrp.getSetter());
		} else {
			return false;
		}
	}

}
