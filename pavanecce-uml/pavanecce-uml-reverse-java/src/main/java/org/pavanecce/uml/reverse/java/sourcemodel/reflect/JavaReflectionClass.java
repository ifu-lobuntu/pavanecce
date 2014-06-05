package org.pavanecce.uml.reverse.java.sourcemodel.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceMethod;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceProperty;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceVariable;

public class JavaReflectionClass extends JavaAnnotated implements SourceClass {
	private Type typeBinding;
	private Map<String, SourceProperty> propertyDescriptors = new HashMap<String, SourceProperty>();
	private SourceMethod[] methods;
	private SourceVariable[] fields;
	private SourceClass[] typeArguments;
	private SourceClass basetype;
	private SourceClass superClass;
	private SourceClass[] interfaces;

	public JavaReflectionClass(Type typeBinding, JavaDescriptorFactory factory) {
		super(factory);
		this.typeBinding = typeBinding;
	}

	@Override
	public String toString() {
		return getQualifiedName();
	}

	public void init() {
		super.init(resolveAnnotations(typeBinding));
		for (JavaReflectionProperty pd : JavaReflectionProperty.getPropertyDescriptors(this.typeBinding, this.factory)) {
			propertyDescriptors.put(pd.getName(), pd);
		}
		if (typeBinding instanceof ParameterizedType) {
			Type[] typeArguments = ((ParameterizedType) this.typeBinding).getActualTypeArguments();
			this.typeArguments = new SourceClass[typeArguments.length];
			for (int i = 0; i < typeArguments.length; i++) {
				this.typeArguments[i] = factory.getClassDescriptor(typeArguments[i]);
			}
		} else {
			typeArguments = new SourceClass[0];
		}
		if (isArray()) {
			basetype = this.factory.getClassDescriptor(((Class<?>) this.typeBinding).getComponentType());
		} else if (isCollectionType()) {
			ParameterizedType pt = (ParameterizedType) this.typeBinding;
			basetype = factory.getClassDescriptor(pt.getActualTypeArguments()[0]);
		} else {
			basetype = this;
		}
		final Class<?> clss = implicatedClass(this.typeBinding);
		if (clss != null) {
			initNormalClass(this.factory, clss);
		} else {
			initGenericType();
		}
	}

	private void initGenericType() {
		interfaces = new SourceClass[0];
		fields = new JavaReflectionVariable[0];
	}

	private void initNormalClass(JavaDescriptorFactory factory, final Class<?> clss) {
		superClass = factory.getClassDescriptor(clss.getSuperclass());
		Class<?>[] sourceInterfaces = clss.getInterfaces();
		interfaces = new SourceClass[sourceInterfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			interfaces[i] = factory.getClassDescriptor(sourceInterfaces[i]);
		}
		Method[] sourceMethods = clss.getDeclaredMethods();
		this.methods = new SourceMethod[sourceMethods.length];
		for (int i = 0; i < methods.length; i++) {
			methods[i] = new JavaReflectionMethod(sourceMethods[i], factory);
		}
		Field[] sourceFields = clss.getDeclaredFields();
		this.fields = new SourceVariable[sourceFields.length];
		for (int i = 0; i < sourceFields.length; i++) {
			fields[i] = factory.getField(sourceFields[i]);
		}
	}

	@Override
	public Map<String, SourceProperty> getPropertyDescriptors() {
		return propertyDescriptors;
	}

	@Override
	public SourceClass[] getTypeArguments() {
		return typeArguments;
	}

	@Override
	public boolean isManyType() {
		return isArray() || isCollectionType();
	}

	private boolean isArray() {
		return typeBinding instanceof Class && ((Class<?>) typeBinding).isArray();
	}

	private boolean isCollectionType() {
		if (typeBinding instanceof ParameterizedType) {
			final ParameterizedType pt = (ParameterizedType) typeBinding;
			return pt.getRawType() instanceof Class<?> && Collection.class.isAssignableFrom((Class<?>) pt.getRawType());
		} else {
			return false;
		}
	}

	@Override
	public SourceClass getBaseType() {
		return basetype;
	}

	@Override
	public String getQualifiedName() {
		final Class<?> clss = implicatedClass(typeBinding);
		if (clss != null) {
			return clss.getName();
		} else {
			return typeBinding.toString();
		}
	}

	@Override
	public boolean isParameterizedType() {
		return typeBinding instanceof ParameterizedType;
	}

	@Override
	public boolean isInterface() {
		if (isCollectionType()) {
			// Collections are treated as manyTypes
			return false;
		} else {
			Class<?> clss = implicatedClass(typeBinding);
			return clss != null && clss.isInterface();
		}
	}

	protected static Class<?> implicatedClass(final Type type) {
		if (type instanceof Class<?>) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return implicatedClass(((ParameterizedType) type).getRawType());
		} else {
			return null;
		}
	}

	@Override
	public boolean isAnnotation() {
		return typeBinding instanceof Class && ((Class<?>) typeBinding).isAnnotation();
	}

	@Override
	public String getName() {
		final Class<?> clss = implicatedClass(typeBinding);
		if (clss != null) {
			return clss.getSimpleName();
		} else {
			return typeBinding.toString();
		}
	}

	@Override
	public String getPackageName() {
		final Class<?> clss = implicatedClass(typeBinding);
		if (clss != null) {
			if (clss.getPackage() == null) {
				// Primitive types
				return "java.lang";
			}
			return clss.getPackage().getName();
		} else {
			return typeBinding.toString();
		}
	}

	@Override
	public SourceClass getSuperclass() {
		return superClass;
	}

	@Override
	public boolean isEnum() {
		final Class<?> clss = implicatedClass(typeBinding);
		if (clss != null) {
			return clss.isEnum();
		} else {
			return false;
		}
	}

	@Override
	public SourceClass[] getInterfaces() {
		return interfaces;
	}

	@Override
	public SourceMethod[] getDeclaredMethods() {
		return methods;
	}

	@Override
	public SourceVariable[] getDeclaredFields() {
		return fields;
	}

	@Override
	public boolean isEntity() {
		return getAnnotation("javax.persistence.Entity") != null;
	}

	@Override
	public boolean isDataType() {
		SourceClass[] interfaces = getInterfaces();
		for (SourceClass inf : interfaces) {
			if (inf.getQualifiedName().equals("java.io.Serializable")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isHelper() {
		return false;
	}

	@Override
	public boolean isUniqueCollectionType() {
		return isCollectionType() && isUnique();
	}

	private boolean isUnique() {
		final ParameterizedType pt = (ParameterizedType) typeBinding;
		return Set.class.isAssignableFrom((Class<?>) pt.getRawType());
	}

	@Override
	public boolean isOrderedCollectionType() {
		return isCollectionType() && isOrdered();
	}

	private boolean isOrdered() {
		final ParameterizedType pt = (ParameterizedType) typeBinding;
		return List.class.isAssignableFrom((Class<?>) pt.getRawType());
	}
}
