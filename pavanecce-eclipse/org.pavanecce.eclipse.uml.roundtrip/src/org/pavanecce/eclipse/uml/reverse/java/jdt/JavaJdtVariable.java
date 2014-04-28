package org.pavanecce.eclipse.uml.reverse.java.jdt;

import org.eclipse.jdt.core.dom.IVariableBinding;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceVariable;

public class JavaJdtVariable extends JdtAnnotated implements SourceVariable {
	IVariableBinding binding;

	public JavaJdtVariable(IVariableBinding binding, JavaDescriptorFactory factory) {
		super(factory);
		this.binding = binding;
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

	public String getInitialValue() {
		Object fromValue = binding.getConstantValue();
		String value = null;
		if (fromValue instanceof String) {
			value = "\"" + fromValue + "\"";
		} else if (fromValue instanceof Number || fromValue instanceof Boolean) {
			value = fromValue.toString();
		}
		return value;
	}

}
