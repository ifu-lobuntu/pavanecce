package org.pavanecce.uml.reverse.java.sourcemodel.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.uml2.uml.VisibilityKind;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceAnnotation;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceProperty;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceVariable;

public class JavaReflectionProperty extends JavaAnnotated implements SourceProperty {
	private String name;
	private Method getter;
	private boolean isReadOnly = true;
	private JavaReflectionVariable field;
	private Boolean isBidirectional;
	private JavaReflectionProperty otherEnd;
	private String mappedBy;
	private SourceClass type;
	private SourceClass declaringType;
	private Method setter;
	private VisibilityKind visibility;
	private boolean isStatic = false;

	/**
	 * Called for Annotation members
	 * 
	 * @param getter
	 * @param f
	 */
	public JavaReflectionProperty(Method getter, JavaDescriptorFactory f) {
		super(f);
		visibility = VisibilityKind.PUBLIC_LITERAL;
		this.getter = getter;
		this.isReadOnly = false;
		this.name = getter.getName();
		this.type = factory.getClassDescriptor(getter.getGenericReturnType());
		this.declaringType = factory.getClassDescriptor(getter.getDeclaringClass());
		init(getter.getAnnotations());
	}

	public JavaReflectionProperty(Method getter, Method setter, JavaDescriptorFactory f) {
		super(f);
		isStatic = Modifier.isStatic(getter.getModifiers());
		this.visibility = JavaReflectUtil.toVisibility(getter.getModifiers());
		this.getter = getter;
		this.setter = setter;
		this.declaringType = f.getClassDescriptor(getter.getDeclaringClass());
		this.name = NameConverter.extractPropertyName(getter.getName());
		this.isReadOnly = setter == null;
		this.type = f.getClassDescriptor(getter.getGenericReturnType());
		Set<String> possibleFieldNames = new HashSet<String>();
		possibleFieldNames.add(name);
		if (name.startsWith("is")) {
			possibleFieldNames.add(NameConverter.decapitalize(name.substring(2)));
		}
		Field field = findField(possibleFieldNames);
		Annotation[] annotationArray = consolidateAnnotations(f, field);
		init(annotationArray);
	}

	public JavaReflectionProperty(Field field, JavaDescriptorFactory f) {
		super(f);
		isStatic = Modifier.isStatic(field.getModifiers());
		this.visibility = JavaReflectUtil.toVisibility(field.getModifiers());
		this.field = f.getField(field);
		this.declaringType = f.getClassDescriptor(field.getDeclaringClass());
		this.name = field.getName();
		this.isReadOnly = Modifier.isFinal(field.getModifiers());
		this.type = f.getClassDescriptor(field.getGenericType());
		Set<String> possibleFieldNames = new HashSet<String>();
		possibleFieldNames.add(name);
		if (name.startsWith("is")) {
			possibleFieldNames.add(NameConverter.decapitalize(name.substring(2)));
		}
		Annotation[] annotationArray = consolidateAnnotations(f, field);
		init(annotationArray);
	}

	public Method getGetter() {
		return getter;
	}

	private Field findField(Set<String> possibleFieldNames) {
		for (String fieldName : possibleFieldNames) {
			try {
				return getter.getDeclaringClass().getDeclaredField(fieldName);
			} catch (Exception e) {
			}
		}
		return null;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	@Override
	public VisibilityKind getVisibility() {
		return visibility;
	}

	private Annotation[] consolidateAnnotations(JavaDescriptorFactory f, Field field) {
		Map<Class<?>, Annotation> annotations = new HashMap<Class<?>, Annotation>();
		if (getter != null) {
			for (Annotation annotation : getter.getAnnotations()) {
				annotations.put(annotation.annotationType(), annotation);
			}
		}
		// Override existing annotations from field
		if (field != null) {
			this.field = f.getField(field);
			for (Annotation annotation : field.getAnnotations()) {
				annotations.put(annotation.annotationType(), annotation);
			}
		}
		Annotation[] annotationArray = annotations.values().toArray(new Annotation[annotations.size()]);
		return annotationArray;
	}

	public Method getSetter() {
		return setter;
	}

	@Override
	public String toString() {
		return declaringType.getName() + "." + name;
	}

	@Override
	public boolean isComposite() {
		if (declaringType.isAnnotation()) {
			return true;
		} else {
			Object[] cascade = findAnnotationAttributeValue("cascade", Object[].class);
			if (cascade != null && cascade.length == 1 && ((SourceVariable) cascade[0]).getName().equals("ALL")) {
				return true;
			}
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T findAnnotationAttributeValue(String attributeName, Class<T> attributeType) {
		T cascade = null;
		for (SourceAnnotation ab : field.getAnnotations()) {
			for (Entry<String, Object> mvp : ab.getMemberValuePairs()) {
				if (mvp.getKey().equals(attributeName)) {
					if (attributeType.isInstance(mvp.getValue())) {
						cascade = (T) mvp.getValue();
					}
				}
			}
		}
		return cascade;
	}

	@Override
	public SourceClass getDeclaringType() {
		return declaringType;
	}

	@Override
	public SourceProperty getOtherEnd() {
		if (isBidirectional == null) {
			String mappedBy = getMappedBy();
			if (mappedBy.isEmpty()) {
				for (SourceProperty pd : getBaseType().getPropertyDescriptors().values()) {
					if (pd.getMappedBy().equals(getName()) && pd.getBaseType().equals(getDeclaringType())) {
						this.otherEnd = (JavaReflectionProperty) pd;
						isBidirectional = Boolean.TRUE;
						break;
					}
				}
			} else {
				isBidirectional = Boolean.TRUE;
				this.otherEnd = (JavaReflectionProperty) getBaseType().getPropertyDescriptors().get(mappedBy);
			}
			if (isBidirectional == null) {
				isBidirectional = Boolean.FALSE;
			}
			if (otherEnd != null) {
				otherEnd.otherEnd = this;
				otherEnd.isBidirectional = Boolean.TRUE;
			}
		}
		return this.otherEnd;
	}

	@Override
	public String getMappedBy() {
		if (mappedBy == null) {
			mappedBy = findAnnotationAttributeValue("mappedBy", String.class);
			if (mappedBy == null) {
				mappedBy = ""; // lookup only once
			}
		}
		return mappedBy;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public SourceVariable getField() {
		return field;
	}

	@Override
	public SourceClass getType() {
		return type;
	}

	@Override
	public SourceClass getBaseType() {
		return this.type.getBaseType();
	}

	@Override
	public boolean isMany() {
		return this.type.isManyType();
	}

	public static Collection<JavaReflectionProperty> getPropertyDescriptors(Type type, JavaDescriptorFactory factory) {
		Class<?> clss = classOf(type);
		Map<String, JavaReflectionProperty> results = new HashMap<String, JavaReflectionProperty>();
		if (clss != null) {
			if (clss.isAnnotation()) {
				Method[] methods = clss.getDeclaredMethods();
				for (Method method : methods) {
					JavaReflectionProperty pd = new JavaReflectionProperty(method, factory);
					results.put(pd.getName(), pd);
				}
			} else {
				Method[] declaredMethods = clss.getMethods();
				for (Method method : declaredMethods) {
					Method getter = method;
					if (method.getDeclaringClass() == clss && !Modifier.isStatic(method.getModifiers())) {
						if ((method.getName().startsWith("get") || method.getName().startsWith("is")) && method.getParameterTypes().length == 0
								&& method.getReturnType() != void.class) {
							String propertyName = NameConverter.extractPropertyName(method.getName());
							if (!(results.containsKey(propertyName))) {
								// missed by BeanInfo - happens with
								// boolean/non-boolean combinations
								JavaReflectionProperty jp = new JavaReflectionProperty(getter, findSetter(method), factory);
								results.put(jp.getName(), jp);
							}
						}
					}
				}
				for (Field field : clss.getDeclaredFields()) {
					if (!(results.containsKey(field.getName()) || field.isEnumConstant())) {
						JavaReflectionProperty pd = new JavaReflectionProperty(field, factory);
						results.put(pd.getName(), pd);
					}
				}
			}

		}
		return results.values();
	}

	private static Method findSetter(Method method) {
		String propName = NameConverter.extractPropertyName(method.getName());
		propName = NameConverter.capitalize(propName);
		try {
			return method.getDeclaringClass().getDeclaredMethod("set" + propName, method.getReturnType());
		} catch (Exception e) {
			try {
				return method.getDeclaringClass().getDeclaredMethod("setIs" + propName, method.getReturnType());
			} catch (Exception e1) {
				if (propName.startsWith("Is")) {
					try {
						return method.getDeclaringClass().getDeclaredMethod("set" + propName.substring(2), method.getReturnType());
					} catch (Exception e3) {
					}
				}
			}
		}
		return null;
	}

	private static Class<?> classOf(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return classOf(((ParameterizedType) type).getRawType());
		}
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return isReadOnly;
	}

	@Override
	public boolean isUnique() {
		return type.isUniqueCollectionType();
	}

	@Override
	public boolean isOrdered() {
		return type.isOrderedCollectionType();
	}

	@Override
	public boolean isDerived() {
		return field == null && getter != null && setter == null;
	}

	@Override
	public Object getInitialValue() {
		return null;
	}

}
