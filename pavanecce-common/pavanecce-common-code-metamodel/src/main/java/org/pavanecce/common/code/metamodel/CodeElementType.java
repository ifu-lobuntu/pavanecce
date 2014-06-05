package org.pavanecce.common.code.metamodel;

public class CodeElementType {
	private boolean isExtends = false;
	private CodeTypeReference type;

	public CodeElementType(CodeTypeReference ctr) {
		this.type = ctr;
	}

	public boolean isExtends() {
		return isExtends;
	}

	public CodeTypeReference getType() {
		return type;
	}

	public void setExtends(boolean b) {
		isExtends = b;

	}

}
