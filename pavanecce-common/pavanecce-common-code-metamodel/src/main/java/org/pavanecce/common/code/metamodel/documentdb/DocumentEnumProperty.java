package org.pavanecce.common.code.metamodel.documentdb;

public class DocumentEnumProperty extends DocumentProperty {

	public DocumentEnumProperty(String name, DocumentNamespace buildNamespace, boolean required) {
		super(name, buildNamespace, required);
	}
	@Override
	public PropertyType getPropertyType() {
		return PropertyType.STRING;
	}

	
}
