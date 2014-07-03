package org.pavanecce.uml.uml2code.ocm;

import java.util.Iterator;
import java.util.Set;

import org.pavanecce.common.code.metamodel.documentdb.ChildDocument;
import org.pavanecce.common.code.metamodel.documentdb.ChildDocumentCollection;
import org.pavanecce.common.code.metamodel.documentdb.DocumentEnumProperty;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNamespace;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNodeType;
import org.pavanecce.common.code.metamodel.documentdb.IChildDocument;
import org.pavanecce.common.code.metamodel.documentdb.IDocumentProperty;
import org.pavanecce.common.code.metamodel.documentdb.IReferencedDocumentProperty;
import org.pavanecce.uml.uml2code.AbstractTextGenerator;

public class CndTextGenerator extends AbstractTextGenerator {
	public String generate(DocumentNamespace n) {
		pushNewStringBuilder();
		append("<mix='http://www.jcp.org/jcr/mix/1.0'>").endLine();
		appendChildNamespaces(n);// Assume the ROOT namespace is just a
									// container
		appendNodeTypes(n);
		return popStringBuilder().toString();
	}

	protected void appendNodeTypes(DocumentNamespace n) {
		if (n.getNodeTypes() != null) {
			for (DocumentNodeType documentNode : n.getNodeTypes()) {
				append("[").append(documentNode.getFullName()).append("] > ");
				Set<DocumentNodeType> superNodeTypes = documentNode.getSuperNodeTypes();
				if (superNodeTypes == null || superNodeTypes.isEmpty()) {
					append(" mix:lifecycle, mix:referenceable").endLine();
				} else {
					Iterator<DocumentNodeType> iterator = superNodeTypes.iterator();
					while (iterator.hasNext()) {
						DocumentNodeType type = iterator.next();
						append(" ").append(type.getFullName());
						if (iterator.hasNext()) {
							append(", ");
						} else {
							endLine();
						}
					}
				}
				if (documentNode.getProperties() != null) {
					for (IDocumentProperty p : documentNode.getProperties()) {
						appendProperty(p);
					}
				}
				if (documentNode.getChildren() != null) {
					for (IChildDocument p : documentNode.getChildren()) {
						appendChild(p);
					}
				}
				appendExtraFields();
				endLine();
				if (documentNode.getChildren() != null) {
					for (IChildDocument p : documentNode.getChildren()) {
						if (p instanceof ChildDocumentCollection) {
							appendCollectionHolderType((ChildDocumentCollection) p);
						}
					}
				}
			}
		}
		Set<DocumentNamespace> children = n.getChildren();
		if (children != null) {
			for (DocumentNamespace child : children) {
				appendNodeTypes(child);
			}
		}
	}

	public void appendExtraFields() {

	}

	private void appendCollectionHolderType(ChildDocumentCollection p) {
		append("[").append(p.getFullName()).append("]").endLine();
		String typeName = p.getType().getFullName();
		append("  + ").append(typeName).append(" (").append(typeName).append(" ) = ").append(typeName).append(" multiple").endLine();
		endLine();
	}

	private CndTextGenerator multiplicity(IDocumentProperty p) {
		if (p.isMultiple()) {
			append(" multiple");
		} else if (p.isMandatory()) {
			append(" mandatory");
		}
		return this;
	}

	private void appendChild(IChildDocument p) {
		String typeName = p.getType().getFullName();
		String fullName = p.getFullName();
		if (p instanceof ChildDocument) {
			append("  + ").append(fullName).append(" (").append(typeName).append(" )").endLine();
		} else {
			append("  + ").append(fullName).append(" (").append(fullName).append(" ) = ").append(fullName).endLine();
		}
	}

	private void appendProperty(IDocumentProperty p) {
		if (p instanceof DocumentEnumProperty) {
			append("  - ").append(p.getFullName()).append(" ( ").append(p.getPropertyType().name()).append(" )").multiplicity(p).append(" < ");
			Iterator<String> iterator = ((DocumentEnumProperty) p).getEnumeratedType().getLiterals().iterator();
			while (iterator.hasNext()) {
				append("'").append(iterator.next()).append("'");
				if (iterator.hasNext()) {
					append(", ");
				}
			}
		} else {
			append("  - ").append(p.getFullName()).append(" ( ").append(p.getPropertyType().name()).append(" ) ").multiplicity(p);
			if (p instanceof IReferencedDocumentProperty) {
				append(" < ").append(((IReferencedDocumentProperty) p).getType().getFullName());
			}
		}
		endLine();
	}

	@Override
	public CndTextGenerator append(String string) {
		return (CndTextGenerator) super.append(string);
	}

	public CndTextGenerator endLine() {
		append("\n");
		return this;
	}

	protected void appendChildNamespaces(DocumentNamespace n) {
		if (n.getChildren() != null) {
			for (DocumentNamespace child : n.getChildren()) {
				append("<").append(child.getPrefix()).append("='").append(child.getName()).append("'>").endLine();
				appendChildNamespaces(child);
			}
		}
	}
}
