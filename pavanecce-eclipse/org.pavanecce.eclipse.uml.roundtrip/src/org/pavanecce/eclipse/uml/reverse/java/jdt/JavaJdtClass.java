package org.pavanecce.eclipse.uml.reverse.java.jdt;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.pavanecce.common.util.IntrospectionUtil;
import org.pavanecce.uml.reverse.java.SourceClass;
import org.pavanecce.uml.reverse.java.SourceMethod;
import org.pavanecce.uml.reverse.java.SourceProperty;
import org.pavanecce.uml.reverse.java.SourceVariable;

public class JavaJdtClass extends JdtAnnotated implements SourceClass {
	ITypeBinding typeBinding;
	private Map<String, SourceProperty> propertyDescriptors = new HashMap<String, SourceProperty>();
	private SourceMethod[] methods;
	private AbstractTypeDeclaration ast;
	private SourceVariable[] fields;
	private SourceClass[] typeArguments;
	private SourceClass[] interfaces;
	private SourceClass baseType;

	public JavaJdtClass(ITypeBinding type, JavaDescriptorFactory factory) {
		super(factory);
		this.typeBinding = type;
	}

	public void init() {
		super.init(typeBinding.getAnnotations());
		for (JavaJdtProperty pd : JavaJdtProperty.getPropertyDescriptors(typeBinding, factory)) {
			propertyDescriptors.put(pd.getName(), pd);
		}
		IVariableBinding[] sourceFields = typeBinding.getDeclaredFields();
		this.fields = new SourceVariable[sourceFields.length];
		for (int i = 0; i < sourceFields.length; i++) {
			fields[i] = new JavaJdtVariable(sourceFields[i], factory);
		}
		ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
		this.typeArguments = new SourceClass[typeArguments.length];
		for (int i = 0; i < typeArguments.length; i++) {
			this.typeArguments[i] = factory.getClassDescriptor(typeArguments[i]);
		}
		ITypeBinding[] interfaces = typeBinding.getInterfaces();
		this.interfaces = new SourceClass[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			this.interfaces[i] = factory.getClassDescriptor(interfaces[i]);
		}
		IMethodBinding[] sourceMethods = typeBinding.getDeclaredMethods();
		this.methods = new SourceMethod[sourceMethods.length];
		for (int i = 0; i < sourceMethods.length; i++) {
			methods[i] = new JavaJdtMethod(sourceMethods[i], factory);
		}
		if (isArray()) {
			baseType = factory.getClassDescriptor(typeBinding.getComponentType());
		} else if (isCollection() && getTypeArguments().length == 1) {
			baseType = getTypeArguments()[0];
		}else{
			baseType=this;
		}
	}

	public AbstractTypeDeclaration getAst() {
		return ast;
	}

	public JavaJdtClass(ITypeBinding key, AbstractTypeDeclaration value, JavaDescriptorFactory factory) {
		this(key, factory);
		this.ast = value;
	}

	@Override
	public Map<String, SourceProperty> getPropertyDescriptors() {
		return propertyDescriptors;
	}

	@Override
	public SourceClass[] getTypeArguments() {
		return this.typeArguments;
	}

	@Override
	public boolean isManyType() {
		return isArray() || isCollection();
	}

	private boolean isCollection() {
		try {
			return Collection.class.isAssignableFrom(IntrospectionUtil.classForName(typeBinding.getBinaryName()));
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isArray() {
		return typeBinding.isArray();
	}

	@Override
	public String getQualifiedName() {
		if(typeBinding.isPrimitive()){
			return getName();
		}
		return typeBinding.getBinaryName();
	}

	@Override
	public boolean isParameterizedType() {
		return typeBinding.isParameterizedType();
	}

	@Override
	public boolean isInterface() {
		return typeBinding.isInterface();
	}

	@Override
	public boolean isAnnotation() {
		return typeBinding.isAnnotation();
	}

	@Override
	public String getName() {
		return typeBinding.getName();
	}

	@Override
	public String getPackageName() {
		if (typeBinding == null || typeBinding.getPackage() == null) {
			System.out.println();
			return null;
		}
		return typeBinding.getPackage().getName();
	}

	@Override
	public SourceClass getSuperclass() {
		return factory.getClassDescriptor(typeBinding.getSuperclass());
	}

	@Override
	public boolean isEnum() {
		return typeBinding.isEnum();
	}

	@Override
	public SourceClass[] getInterfaces() {
		return interfaces;
	}

	@Override
	public SourceMethod[] getDeclaredMethods() {

		return methods;
	}

	public SourceClass getBaseType() {
		return baseType;
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
		return Set.class.isAssignableFrom(IntrospectionUtil.classForName(typeBinding.getBinaryName()));
	}

	@Override
	public boolean isOrderedCollectionType() {
		return List.class.isAssignableFrom(IntrospectionUtil.classForName(typeBinding.getBinaryName()));
	}

	public static String getIdentifier(ITypeBinding b) {
		if (b.isParameterizedType() && b.getQualifiedName().contains("?")) {
			return b.getBinaryName();
		}
		return b.getQualifiedName();
	}
}
