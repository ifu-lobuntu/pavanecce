package org.pavanecce.common.code.metamodel.documentdb;

public class ChildDocument extends DocumentAssociation implements IChildDocument{

	public ChildDocument(DocumentNamespace namespace, String name, DocumentNodeType type) {
		super(namespace, name, type);
	}

}
