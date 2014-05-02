package org.pavanecce.common.code.metamodel.documentdb;

public class ParentDocument extends DocumentAssociation {

	private boolean childIsMany;

	public ParentDocument(DocumentNamespace namespace, String name, DocumentNodeType type, boolean childIsMany) {
		super(namespace, name, type);
		this.childIsMany = childIsMany;
	}

	public boolean isChildIsMany() {
		return childIsMany;
	}
}
