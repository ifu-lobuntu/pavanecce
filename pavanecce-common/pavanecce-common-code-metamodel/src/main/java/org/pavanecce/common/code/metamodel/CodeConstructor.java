package org.pavanecce.common.code.metamodel;

public class CodeConstructor extends CodeBehaviour {

	public CodeConstructor(CodeClassifier clss) {
		super(clss, "");
	}

	public CodeConstructor() {
		super("");
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public void setDeclaringClass(CodeClassifier clss) {
		super.setDeclaringClass(declaringClass);
		if (clss instanceof CodeEnumeration) {
			((CodeEnumeration) clss).getConstructors().put(generateIdentifier(), this);
		} else if (clss instanceof CodeClass) {
			((CodeClass) clss).getConstructors().put(generateIdentifier(), this);
		}
	}

}
