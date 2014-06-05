package org.pavanecce.common.code.metamodel.documentdb;

public class ChildDocumentCollection extends DocumentAssociation implements IChildDocument {

	public ChildDocumentCollection(DocumentNamespace namespace, String name, DocumentNodeType type) {
		super(namespace, name, type);
	}

}
