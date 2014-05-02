package org.pavanecce.common.code.metamodel;

public class AssociationCollectionTypeReference extends CollectionTypeReference {
	String otherFieldName;
	CodeTypeReference otherFieldType;

	public AssociationCollectionTypeReference(CodeCollectionKind kind, String otherFieldName) {
		super(kind);
		this.otherFieldName = otherFieldName;
	}

	public String getOtherFieldName() {
		return otherFieldName;
	}

	public CodeTypeReference getOtherFieldType() {
		return otherFieldType;
	}
	public void setOtherFieldType(CodeTypeReference otherFieldType) {
		this.otherFieldType = otherFieldType;
	}
}
