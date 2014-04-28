package org.pavanecce.eclipse.uml.reverse.java.jdt;

import java.lang.reflect.Modifier;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.uml2.uml.VisibilityKind;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceCode;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceMethod;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceProperty;
import org.pavanecce.uml.reverse.java.sourcemodel.reflect.JavaReflectUtil;

public class JavaJdtMethod extends JdtAnnotated implements SourceMethod {

	private IMethodBinding binding;
	private SourceClass[] parameterTypes;
	private JavaJdtClass returnType;
	private JavaJdtClass declaringClass;

	public JavaJdtMethod(IMethodBinding binding, JavaDescriptorFactory factory) {
		super(factory);
		this.binding = binding;
		this.returnType = factory.getClassDescriptor(binding.getReturnType());
		ITypeBinding[] source = binding.getParameterTypes();
		this.parameterTypes = new JavaJdtClass[source.length];
		for (int i = 0; i < source.length; i++) {
			parameterTypes[i] = factory.getClassDescriptor(source[i]);
		}
		declaringClass = factory.getClassDescriptor(binding.getDeclaringClass());
		init(binding.getAnnotations());
	}

	public String[] getParameterNames() {
		if (binding.getJavaElement() instanceof IMethod) {
			IMethod javaElement = (IMethod) binding.getJavaElement();
			try {
				return javaElement.getParameterNames();
			} catch (JavaModelException e) {
				throw new RuntimeException(e);
			}
		}
		String[] result = new String[getParameterTypes().length];
		for (int i = 0; i < result.length; i++) {
			result[i] = "param" + i;

		}
		return result;
	}

	@Override
	public boolean isConstructor() {
		return binding.isConstructor();
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
	public SourceCode getSource() {
		return factory.getSource(binding);
	}

	@Override
	public boolean isAccessor() {
		String propertyName = NameConverter.extractPropertyName(binding.getName());
		SourceProperty pd = declaringClass.getPropertyDescriptors().get(propertyName);
		if(pd==null){
			if(getName().startsWith("set") && getParameterTypes().length==1 && getParameterTypes()[0].getName().equals("boolean")){
				pd=declaringClass.getPropertyDescriptors().get("is"+NameConverter.capitalize(propertyName));
			}
		}
		if (pd instanceof JavaJdtProperty) {
			JavaJdtProperty jrp = (JavaJdtProperty) pd;
			return binding.equals(jrp.getGetter()) || binding.equals(jrp.getSetter());
		} else {
			return false;
		}
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(binding.getModifiers());
	}

	@Override
	public VisibilityKind getVisibility() {
		return JavaReflectUtil.toVisibility(binding.getModifiers());
	}

}
