package org.pavanecce.common.code.metamodel.documentdb;

public class DocumentAssociation implements IDocumentElement {
	private DocumentNamespace namespace;
	private String name;
	private DocumentNodeType type;

	public DocumentAssociation(DocumentNamespace namespace, String name, DocumentNodeType type) {
		this.namespace = namespace;
		this.name = name;
		this.type = type;
	}

	public DocumentNamespace getNamespace() {
		return namespace;
	}

	public String getName() {
		return name;
	}

	public DocumentNodeType getType() {
		return type;
	}

	public String getFullName() {
		return namespace.getPrefix() + ":" + getName();
	}

}
