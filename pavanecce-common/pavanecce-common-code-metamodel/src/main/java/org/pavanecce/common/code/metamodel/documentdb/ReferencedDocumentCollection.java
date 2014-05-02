package org.pavanecce.common.code.metamodel.documentdb;

public class ReferencedDocumentCollection extends DocumentAssociation implements IDocumentProperty {

	public ReferencedDocumentCollection(DocumentNamespace namespace, String name, DocumentNodeType type) {
		super(namespace, name, type);
	}
	@Override
	public PropertyType getPropertyType() {
		return PropertyType.REFERENCE;
	}

}
