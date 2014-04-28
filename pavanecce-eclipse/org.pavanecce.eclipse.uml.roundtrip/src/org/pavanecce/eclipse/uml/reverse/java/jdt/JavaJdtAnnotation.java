package org.pavanecce.eclipse.uml.reverse.java.jdt;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceAnnotation;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;

public class JavaJdtAnnotation  extends JdtAnnotated implements SourceAnnotation {

	private SourceClass type;
	private Map<String, Object> attributeValues = new HashMap<String, Object>();

	public JavaJdtAnnotation(IAnnotationBinding binding, JavaDescriptorFactory factory) {
		super(factory);
		this.type = factory.getClassDescriptor(binding.getAnnotationType());
		final IMemberValuePairBinding[] declaredMemberValuePairs = binding.getDeclaredMemberValuePairs();
		for (IMemberValuePairBinding v : declaredMemberValuePairs) {
			if (v.getValue() instanceof Object[]) {
				Object[] values = (Object[]) v.getValue();
				Object[] convertedValues = new Object[values.length];
				for (int i = 0; i < values.length; i++) {
					convertedValues[i] = convertSingleValue(values[i]);
				}
				attributeValues.put(v.getName(), convertedValues);
			} else {
				Object value = convertSingleValue(v.getValue());
				attributeValues.put(v.getName(), value);
			}
		}
		init(binding.getAnnotations());
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
		} else if (fromValue instanceof IVariableBinding) {
			IVariableBinding vb = (IVariableBinding) fromValue;
			if (vb.isEnumConstant()) {
				// Remember annotations wont have associations to enums nor
				// inheritance - this will work:
				value = new JavaJdtVariable(vb,factory);
			}
		} else if (fromValue instanceof ITypeBinding) {
			value = this.factory.getClassDescriptor((ITypeBinding) fromValue);
		} else if (fromValue instanceof IAnnotationBinding) {
			value = new JavaJdtAnnotation((IAnnotationBinding) fromValue, factory);
		}
		return value;
	}

	@Override
	public Set<Entry<String, Object>>  getMemberValuePairs() {
		return this.attributeValues.entrySet();
	}

}
