package org.pavanecce.eclipse.uml.reverse.java.jdt;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.uml2.uml.VisibilityKind;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceAnnotation;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceProperty;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceVariable;
import org.pavanecce.uml.reverse.java.sourcemodel.reflect.JavaReflectUtil;

public class JavaJdtProperty extends JdtAnnotated implements SourceProperty {
	private String name;
	private IMethodBinding getter;
	private boolean isReadOnly = true;
	private JavaJdtVariable field;
	private Boolean isBidirectional;
	private JavaJdtProperty otherEnd;
	private String mappedBy;
	private SourceClass type;
	private SourceClass declaringType;
	private VisibilityKind visibility;
	private boolean isStatic;
	private IMethodBinding setter;

	public JavaJdtProperty(IMethodBinding getter, JavaDescriptorFactory f) {
		super(f);
		visibility = VisibilityKind.PUBLIC_LITERAL;
		this.getter = getter;
		this.isReadOnly = false;
		this.name = getter.getName();
		this.type = factory.getClassDescriptor(getter.getReturnType());
		this.declaringType = factory.getClassDescriptor(getter.getDeclaringClass());
		init(getter.getAnnotations());
	}

	public JavaJdtProperty(IMethodBinding getter, IMethodBinding setter, JavaDescriptorFactory f) {
		super(f);
		isStatic = Modifier.isStatic(getter.getModifiers());
		this.visibility = JavaReflectUtil.toVisibility(getter.getModifiers());
		this.getter = getter;
		this.setter = setter;
		this.declaringType = f.getClassDescriptor(getter.getDeclaringClass());
		this.name = NameConverter.extractPropertyName(getter.getName());
		this.isReadOnly = setter == null;
		this.type = f.getClassDescriptor(getter.getReturnType());
		Set<String> possibleFieldNames = new HashSet<String>();
		possibleFieldNames.add(name);
		if (name.startsWith("is")) {
			possibleFieldNames.add(NameConverter.decapitalize(name.substring(2)));
		}
		IVariableBinding field = findField(possibleFieldNames);
		IAnnotationBinding[] annotationArray = consolidateAnnotations(f, field);
		init(annotationArray);
	}

	public JavaJdtProperty(IVariableBinding field, JavaDescriptorFactory f) {
		super(f);
		isStatic = Modifier.isStatic(field.getModifiers());
		this.visibility = JavaReflectUtil.toVisibility(field.getModifiers());
		this.field = f.getField(field);
		this.declaringType = f.getClassDescriptor(field.getDeclaringClass());
		this.name = field.getName();
		this.isReadOnly = Modifier.isFinal(field.getModifiers());
		this.type = f.getClassDescriptor(field.getType());
		Set<String> possibleFieldNames = new HashSet<String>();
		possibleFieldNames.add(name);
		if (name.startsWith("is")) {
			possibleFieldNames.add(NameConverter.decapitalize(name.substring(2)));
		}
		IAnnotationBinding[] annotationArray = consolidateAnnotations(f, field);
		init(annotationArray);
	}

	private IAnnotationBinding[] consolidateAnnotations(JavaDescriptorFactory f, IVariableBinding field) {
		Map<ITypeBinding, IAnnotationBinding> annotations = new HashMap<ITypeBinding, IAnnotationBinding>();
		if (getter != null) {
			for (IAnnotationBinding annotation : getter.getAnnotations()) {
				annotations.put(annotation.getAnnotationType(), annotation);
			}
		}
		// Override existing annotations from field
		if (field != null) {
			this.field = f.getField(field);
			for (IAnnotationBinding annotation : field.getAnnotations()) {
				annotations.put(annotation.getAnnotationType(), annotation);
			}
		}
		IAnnotationBinding[] annotationArray = annotations.values().toArray(new IAnnotationBinding[annotations.size()]);
		return annotationArray;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	public SourceClass getDeclaringType() {
		return declaringType;
	}

	@Override
	public VisibilityKind getVisibility() {
		return visibility;
	}

	@Override
	public boolean isComposite() {
		if (type.isAnnotation()) {
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
		for (SourceAnnotation ab : getAnnotations()) {
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
	public SourceProperty getOtherEnd() {
		if (isBidirectional == null) {
			String mappedBy = getMappedBy();
			if (mappedBy.isEmpty()) {
				for (SourceProperty pd : getBaseType().getPropertyDescriptors().values()) {
					if (pd.getMappedBy().equals(getName()) && pd.getBaseType().equals(getDeclaringType())) {
						this.otherEnd = (JavaJdtProperty) pd;
						isBidirectional = Boolean.TRUE;
						break;
					}
				}
			} else {
				isBidirectional = Boolean.TRUE;
				this.otherEnd = (JavaJdtProperty) getBaseType().getPropertyDescriptors().get(mappedBy);
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

	public String getMappedBy() {
		if (mappedBy == null) {
			mappedBy = findAnnotationAttributeValue("mappedBy", String.class);
			if (mappedBy == null) {
				mappedBy = ""; // lookup only once
			}
		}
		return mappedBy;
	}

	public static String propertyName(IMethodBinding getter) {
		return NameConverter.extractPropertyName(getter.getName());
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
		return type.isManyType();
	}

	public static Collection<JavaJdtProperty> getPropertyDescriptors(ITypeBinding type, JavaDescriptorFactory f) {
		IMethodBinding[] methods = type.getDeclaredMethods();
		Map<String, JavaJdtProperty> results = new HashMap<String, JavaJdtProperty>();
		for (IMethodBinding getter : methods) {
			if (isGetter(getter)) {
				JavaJdtProperty pd = new JavaJdtProperty(getter, findSetter(getter), f);
				results.put(pd.getName(), pd);
			} else if (getter.isAnnotationMember()) {
				JavaJdtProperty pd = new JavaJdtProperty(getter, f);
				results.put(pd.getName(), pd);
			}

		}
		for (IVariableBinding vb : type.getDeclaredFields()) {
			if (!(results.containsKey(vb.getName()) || vb.isEnumConstant())) {
				if(vb.getType().getQualifiedName().equals("boolean") && results.containsKey("is"+NameConverter.capitalize(vb.getName()))){
					continue;
				}
				JavaJdtProperty pd = new JavaJdtProperty(vb, f);
				results.put(pd.getName(), pd);
			}
		}
		return results.values();
	}

	private IVariableBinding findField(Set<String> possibleFieldNames) {
		for (String fieldName : possibleFieldNames) {
			ITypeBinding declaringClass = getter.getDeclaringClass();
			for (IVariableBinding vb : declaringClass.getDeclaredFields()) {
				if (vb.getName().equals(fieldName)) {
					return vb;
				}
			}
		}
		return null;
	}

	private static boolean isGetter(IMethodBinding method) {
		return (Modifier.isPublic(method.getModifiers()) && hasGetterName(method) && method.getParameterTypes().length == 0);
	}

	private static boolean hasGetterName(IMethodBinding method) {
		return hasNormalGetterName(method) || hasBooleanGetterName(method);
	}

	private static boolean hasNormalGetterName(IMethodBinding method) {
		return method.getName().startsWith("get") && !method.getReturnType().getName().equals("void");
	}

	private static boolean hasBooleanGetterName(IMethodBinding method) {
		return method.getReturnType().getName().equals("boolean") && method.getName().startsWith("is");
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

	private static IMethodBinding findSetter(IMethodBinding method) {
		String propName = NameConverter.extractPropertyName(method.getName());
		propName = NameConverter.capitalize(propName);
		if (propName.startsWith("Is")) {
			return getDeclaredSetter(method, "set" + propName, "set" + propName.substring(2), "setIs" + propName);
		} else {
			return getDeclaredSetter(method, "set" + propName, "setIs" + propName);
		}
	}

	protected static IMethodBinding getDeclaredSetter(IMethodBinding method, String... methodName) {
		for (IMethodBinding mb : method.getDeclaringClass().getDeclaredMethods()) {
			if (mb.getParameterTypes().length == 1 && mb.getParameterTypes()[0].equals(method.getReturnType())) {
				for (String name : methodName) {
					if (mb.getName().equals(name)) {
						return mb;
					}
				}
			}
		}
		return null;
	}

	public IMethodBinding getSetter() {
		return setter;
	}

	public IMethodBinding getGetter() {
		return getter;
	}

	@Override
	public boolean isDerived() {
		return field == null && getter != null && setter == null;
	}

	@Override
	public Object getInitialValue() {
		if (getter == null) {
			if (field != null) {
				return field.getInitialValue();
			}
			return null;
		} else if (isDerived()) {
			return factory.getSource(getter);
		} else {
			return null;
		}
	}
}
