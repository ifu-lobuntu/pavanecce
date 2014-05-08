package org.pavanecce.common.code.metamodel.documentdb;

public class DocumentProperty implements IDocumentElement, IDocumentProperty {

	private boolean required;
	private DocumentNamespace namespace;
	private String name;
	private PropertyType propertyType;
	private boolean multiple;
	private boolean isPath;
	private boolean isUuid;

	public DocumentProperty(String name, DocumentNamespace namespace, PropertyType type, boolean required, boolean isMany) {
		this.name = name;
		this.namespace = namespace;
		this.required = required;
		this.multiple=isMany;
		this.propertyType=type;
	}
	@Override
	public boolean isMultiple() {
		return multiple;
	}
	@Override
	public boolean isMandatory() {
		return required;
	}

	public DocumentNamespace getNamespace() {
		return namespace;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getFullName() {
		return namespace.getPrefix() + ":" + getName();
	}

	@Override
	public PropertyType getPropertyType() {
		return propertyType;
	}
	public void setPath(boolean b) {
		this.isPath=b;
	}
	public boolean isPath() {
		return isPath;
	}
	public boolean isUuid() {
		return isUuid;
	}
	public void setUuid(boolean isUuid) {
		this.isUuid = isUuid;
	}
}

