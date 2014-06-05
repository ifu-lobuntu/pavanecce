package org.pavanecce.uml.reverse.java.sourcemodel.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.pavanecce.uml.reverse.java.sourcemodel.SourceAnnotation;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;

public class JavaReflectionAnnotation implements SourceAnnotation {

	private JavaDescriptorFactory factory;
	private SourceClass type;
	private Map<String, Object> attributeValues = new HashMap<String, Object>();

	public JavaReflectionAnnotation(Annotation binding, JavaDescriptorFactory factory) {
		this.factory = factory;
		this.type = factory.getClassDescriptor(binding.annotationType());
		Method[] declaredMemberValuePairs = binding.annotationType().getDeclaredMethods();
		for (Method v : declaredMemberValuePairs) {
			if (v.getParameterTypes().length == 0) {
				try {
					Object value = v.invoke(binding);
					if (value instanceof Object[]) {
						Object[] values = (Object[]) value;
						Object[] convertedValues = new Object[values.length];
						for (int i = 0; i < values.length; i++) {
							convertedValues[i] = convertSingleValue(values[i]);
						}
						attributeValues.put(v.getName(), convertedValues);
					} else {
						attributeValues.put(v.getName(), convertSingleValue(value));
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public SourceClass getAnnotationType() {
		return type;
	}

	@Override
	public Object getAnnotationAttribute(String string) {
		return attributeValues.get(string);
	}

	private Object convertSingleValue(Object fromValue) {
		Object value = null;
		if (fromValue instanceof String || fromValue instanceof Number || fromValue instanceof Boolean) {
			value = fromValue;
		} else if (fromValue instanceof Enum<?>) {
			try {
				Field field = fromValue.getClass().getDeclaredField(((Enum<?>) fromValue).name());
				value = new JavaReflectionVariable(field, factory);
			} catch (NoSuchFieldException | SecurityException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (fromValue instanceof Type) {
			value = this.factory.getClassDescriptor((Type) fromValue);
		} else if (fromValue instanceof Annotation) {
			value = new JavaReflectionAnnotation((Annotation) fromValue, factory);
		}
		return value;
	}

	@Override
	public Set<Entry<String, Object>> getMemberValuePairs() {
		return this.attributeValues.entrySet();
	}

}
