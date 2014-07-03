package org.pavanecce.common.code.metamodel;

import org.pavanecce.common.code.metamodel.expressions.PortableExpression;

public class CodeField extends CodeElement {
	private CodeTypeReference type;
	private CodeExpression initialization;
	private CodeVisibilityKind visibility = CodeVisibilityKind.PRIVATE;
	private boolean isTransient;
	private boolean isStatic;
	private boolean isConstant;
	private CodeElement owner;

	public CodeField(CodeClassifier cls, String name) {
		super(name);
		cls.getFields().put(name, this);
		owner = cls;
	}

	public CodeField(CodeBlock block, String name) {
		super(name);
		block.getLocals().add(this);
		owner = block;
	}

	public CodeField(CodeClassifier cls, String name, CodeTypeReference type) {
		this(cls, name);
		this.type = type;
		owner = cls;
	}

	public CodeField(CodeBlock block, String name, CodeTypeReference type) {
		this(block, name);
		this.type = type;
		this.owner = block;
	}

	public CodeElement getOwner() {
		return owner;
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
		this.isStatic = b;
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
