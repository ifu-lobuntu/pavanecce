package org.pavanecce.common.code.metamodel.documentdb;

public class DocumentEnumProperty extends DocumentProperty {

	private DocumentEnumeratedType type;

	public DocumentEnumProperty(String name, DocumentNamespace buildNamespace, boolean required, boolean many, DocumentEnumeratedType type) {
		super(name, buildNamespace, PropertyType.STRING, required, many);
		this.type = type;
	}

	@Override
	public PropertyType getPropertyType() {
		return PropertyType.STRING;
	}

	public DocumentEnumeratedType getEnumeratedType() {
		return type;
	}

}
