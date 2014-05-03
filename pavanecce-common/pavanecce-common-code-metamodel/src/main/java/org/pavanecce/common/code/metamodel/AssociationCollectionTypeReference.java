package org.pavanecce.common.code.metamodel;

public class AssociationCollectionTypeReference extends CollectionTypeReference {
	String otherFieldName;
	CodeTypeReference otherFieldType;
	private boolean isChild;

	public AssociationCollectionTypeReference(CodeCollectionKind kind, String otherFieldName, boolean isChild) {
		super(kind);
		this.otherFieldName = otherFieldName;
		this.isChild = isChild;
	}

	public String getOtherFieldName() {
		return otherFieldName;
	}

	public boolean isChild() {
		return isChild;
	}

	public CodeTypeReference getOtherFieldType() {
		return otherFieldType;
	}

	public void setOtherFieldType(CodeTypeReference otherFieldType) {
		this.otherFieldType = otherFieldType;
	}
}
