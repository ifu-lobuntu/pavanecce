package org.pavanecce.common.code.metamodel.documentdb;

import java.util.HashSet;
import java.util.Set;

public class DocumentNodeType implements IDocumentElement {
	private DocumentNamespace namespace;
	private String nodeTypeName;
	private String uuidField;
	private String pathField;
	private Set<IDocumentProperty> properties;
	private Set<IChildDocument> children;
	private Set<DocumentNodeType> superNodeTypes;

	public DocumentNodeType(DocumentNamespace namespace, String nodeTypeName) {
		super();
		this.namespace = namespace;
		this.nodeTypeName = nodeTypeName;
	}

	public DocumentNamespace getNamespace() {
		return namespace;
	}

	public String getFullName() {
		return namespace.getPrefix() + ":" + getNodeTypeName();
	}

	public String getNodeTypeName() {
		return nodeTypeName;
	}

	public String getUuidField() {
		return uuidField;
	}

	public void setUuidField(String uuidField) {
		this.uuidField = uuidField;
	}

	public String getPathField() {
		return pathField;
	}

	public void setPathField(String pathField) {
		this.pathField = pathField;
	}

	public void addProperty(IDocumentProperty e) {
		(properties == null ? properties = new HashSet<IDocumentProperty>() : properties).add(e);
	}

	public void addChild(IChildDocument e) {
		(children == null ? children = new HashSet<IChildDocument>() : children).add(e);
	}

	public void addSuperNodeType(DocumentNodeType e) {
		(superNodeTypes == null ? superNodeTypes = new HashSet<DocumentNodeType>() : superNodeTypes).add(e);
	}

	public Set<DocumentNodeType> getSuperNodeTypes() {
		return superNodeTypes;
	}

	public Set<IDocumentProperty> getProperties() {
		return properties;
	}

	public Set<IChildDocument> getChildren() {
		return children;
	}
}
