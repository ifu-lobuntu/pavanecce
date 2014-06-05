package org.pavanecce.uml.reverse.java.sourcemodel.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JavaDescriptorFactory {
	private Map<Type, JavaReflectionClass> classDescriptors = new HashMap<Type, JavaReflectionClass>();
	private Map<Field, JavaReflectionVariable> fieldDescriptors = new HashMap<Field, JavaReflectionVariable>();

	public JavaReflectionClass getClassDescriptor(Type b) {
		JavaReflectionClass result = classDescriptors.get(b);
		if (result == null && b != null) {
			classDescriptors.put(b, result = new JavaReflectionClass(b, this));
			result.init();
		}
		return result;
	}

	public JavaReflectionVariable getField(Field vb) {
		JavaReflectionVariable result = fieldDescriptors.get(vb);
		if (result == null) {
			fieldDescriptors.put(vb, result = new JavaReflectionVariable(vb, this));
		}
		return result;
	}
}
