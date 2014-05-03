package org.pavanecce.common.code.metamodel.documentdb;

public class ReferencedDocument extends DocumentAssociation implements IDocumentProperty,IReferencedDocumentProperty {

	private boolean mandatory;

	public ReferencedDocument(DocumentNamespace namespace, String name, DocumentNodeType type, boolean mandatory) {
		super(namespace, name, type);
		this.mandatory = mandatory;
	}

	@Override
	public PropertyType getPropertyType() {
		return PropertyType.REFERENCE;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	@Override
	public boolean isMultiple() {
		return false;
	}
}
