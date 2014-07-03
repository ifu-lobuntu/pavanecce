package org.pavanecce.common.code.metamodel.documentdb;

import java.util.ArrayList;
import java.util.List;

public class DocumentEnumeratedType extends DocumentNodeType {
	private List<String> literals = new ArrayList<String>();

	public DocumentEnumeratedType(DocumentNamespace namespace, String nodeTypeName) {
		super(namespace, nodeTypeName);
	}

	public List<String> getLiterals() {
		return literals;
	}

	public void addLiteral(String s) {
		literals.add(s);
	}
}
