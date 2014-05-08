package org.pavanecce.common.code.metamodel;

import org.pavanecce.common.code.metamodel.expressions.PortableExpression;

public class CodeField extends CodeElement {
	private CodeTypeReference type;
	private CodeExpression initialization;
	private CodeVisibilityKind visibility = CodeVisibilityKind.PRIVATE;
	private boolean isTransient;
	private boolean isStatic;
	private boolean isConstant;
	public CodeField(CodeClassifier cls, String name) {
		super(name);
		cls.getFields().put(name, this);
	}

	public CodeField(CodeBlock cls, String name) {
		super(name);
		cls.getLocals().add(this);
	}

	public CodeField(CodeClassifier cls, String name, CodeTypeReference type) {
		this(cls, name);
		this.type = type;
	}

	public CodeField(CodeBlock owner, String name, CodeTypeReference type) {
		this(owner, name);
		this.type = type;
	}

	public CodeTypeReference getType() {
		return type;
	}

	public void setType(CodeTypeReference type) {
		this.type = type;
	}

	public CodeExpression getInitialization() {
		return initialization;
	}

	public void setInitialization(String initialization) {
		this.initialization = new PortableExpression(initialization);
	}

	public void setInitialization(CodeExpression initialization) {
		this.initialization = initialization;
	}

	public CodeVisibilityKind getVisibility() {
		return visibility;
	}

	public void setVisibility(CodeVisibilityKind visibility) {
		this.visibility = visibility;
	}

	public void setInitExp(String value) {
		setInitialization(value);

	}

	@Override
	public String toString() {
		if (this.type != null) {
			return getName() + ":" + this.type.toString();
		} else {
			return getName();
		}
	}

	public boolean isTransient() {
		return isTransient;
	}

	public void setTransient(boolean isTransient) {
		this.isTransient = isTransient;
	}

	public void setStatic(boolean b) {
		this.isStatic=b;
	}
	public boolean isStatic() {
		return isStatic;
	}

	public boolean isConstant() {
		return isConstant;
	}

	public void setConstant(boolean isConstant) {
		this.isConstant = isConstant;
	}

}
