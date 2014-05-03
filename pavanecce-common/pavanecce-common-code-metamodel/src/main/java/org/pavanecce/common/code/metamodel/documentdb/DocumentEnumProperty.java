package org.pavanecce.common.code.metamodel.documentdb;

public class DocumentEnumProperty extends DocumentProperty {

	public DocumentEnumProperty(String name, DocumentNamespace buildNamespace, boolean required, boolean many) {
		super(name, buildNamespace, PropertyType.STRING, required,many);
	}
	@Override
	public PropertyType getPropertyType() {
		return PropertyType.STRING;
	}

	
}
