package org.pavanecce.common.code.metamodel.documentdb;

public class DocumentProperty implements IDocumentElement, IDocumentProperty {

	private boolean required;
	private DocumentNamespace namespace;
	private String name;
	private PropertyType propertyType;

	public DocumentProperty(String name, DocumentNamespace buildNamespace, boolean required) {
		this.name = name;
		this.namespace = buildNamespace;
		this.required = true;
	}

	public boolean isRequired() {
		return required;
	}

	public DocumentNamespace getNamespace() {
		return namespace;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return namespace.getPrefix() + ":" + getName();
	}

	public void setPropertyType(PropertyType propertyType) {
		this.propertyType = propertyType;
	}

	@Override
	public PropertyType getPropertyType() {
		return propertyType;
	}
}
