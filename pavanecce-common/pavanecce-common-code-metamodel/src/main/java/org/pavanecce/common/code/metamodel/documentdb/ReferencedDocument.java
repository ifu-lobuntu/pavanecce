package org.pavanecce.common.code.metamodel.documentdb;

public class ReferencedDocument extends DocumentAssociation implements IDocumentProperty {

	public ReferencedDocument(DocumentNamespace namespace, String name, DocumentNodeType type) {
		super(namespace, name, type);
	}

	@Override
	public PropertyType getPropertyType() {
		return PropertyType.REFERENCE;
	}

}
