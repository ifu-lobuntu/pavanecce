package org.pavanecce.common.code.metamodel.documentdb;

public class ChildDocument extends DocumentAssociation implements IChildDocument {

	private boolean isRequired;

	public ChildDocument(DocumentNamespace namespace, String name, DocumentNodeType type, boolean required) {
		super(namespace, name, type);
		this.isRequired = required;
	}

	public boolean isRequired() {
		return isRequired;
	}

}
