package org.pavanecce.common.code.metamodel.documentdb;

import java.util.HashSet;
import java.util.Set;

public class DocumentNamespace implements IDocumentElement {
	private String name;
	private String prefix;
	private Set<DocumentNamespace> children;
	private Set<DocumentNodeType> nodeTypes;

	public DocumentNamespace(String name, String prefix) {
		super();
		this.name = name;
		this.prefix = prefix;
	}

	public String getName() {
		return name;
	}

	public String getPrefix() {
		return prefix;
	}

	public void addChild(DocumentNamespace ns) {
		(children == null ? children = new HashSet<DocumentNamespace>() : children).add(ns);
	}

	public Set<DocumentNamespace> getChildren() {
		return children;
	}

	public void addNodeType(DocumentNodeType ns) {
		(nodeTypes == null ? nodeTypes = new HashSet<DocumentNodeType>() : nodeTypes).add(ns);
	}

	public Set<DocumentNodeType> getNodeTypes() {
		return nodeTypes;
	}
}
