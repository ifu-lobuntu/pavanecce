package org.pavanecce.common.code.metamodel;

public class CollectionTypeReference extends CodeTypeReference {

	private CodeCollectionKind kind;
	private boolean isImplementation = false;
	private CollectionTypeReference implementation;

	public CollectionTypeReference(CodeCollectionKind kind) {
		super(false, "Collections", kind.name());
		this.kind = kind;
	}

	public CollectionTypeReference(CodeCollectionKind kind, boolean isImplementation) {
		this(kind);
		this.isImplementation = isImplementation;
	}

	public CollectionTypeReference(CodeCollectionKind kind, CodeTypeReference typeReference) {
		this(kind);
		super.addToElementTypes(typeReference);
	}

	@Override
	public CollectionTypeReference getCopy() {
		return new CollectionTypeReference(kind, isImplementation);
	}

	@Override
	public int compareTo(CodeTypeReference o) {
		if (o instanceof CollectionTypeReference) {
			CollectionTypeReference other = (CollectionTypeReference) o;
			int result = kind.compareTo(other.getKind());
			if (result == 0) {
				if (this.isImplementation && !other.isImplementation) {
					result = 1;

				} else if (!this.isImplementation && other.isImplementation) {
					result = -11;
				}
			}
			return result;
		} else {
			return super.compareTo(o);
		}
	}

	public CollectionTypeReference getImplementation() {
		if (implementation == null && !isImplementation) {
			implementation = new CollectionTypeReference(kind, true);
		}
		return implementation;
	}

	public boolean isImplementation() {
		return isImplementation;
	}

	public CodeCollectionKind getKind() {
		return kind;
	}
}
