package org.pavanecce.uml.uml2code.ocm;

import java.util.Iterator;
import java.util.Set;

import org.pavanecce.common.code.metamodel.documentdb.DocumentNamespace;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNodeType;
import org.pavanecce.common.code.metamodel.documentdb.IDocumentProperty;
import org.pavanecce.common.code.metamodel.documentdb.ReferencedDocument;
import org.pavanecce.common.code.metamodel.documentdb.ReferencedDocumentCollection;
import org.pavanecce.uml.uml2code.AbstractTextGenerator;

public class CndTextGenerator extends AbstractTextGenerator {
	public String generate(DocumentNamespace n) {
		pushNewStringBuilder();
		append("<mix='http://www.jcp.org/jcr/mix/1.0'>").endLine();
		appendNamespaces(n);
		for (DocumentNodeType documentNode : n.getNodeTypes()) {
			append("[").append(documentNode.getFullName()).append("] > ");
			Set<DocumentNodeType> superNodeTypes = documentNode.getSuperNodeTypes();
			if(superNodeTypes==null || superNodeTypes.isEmpty()){
				append(" mix:lifecycle, mix:referenceable").endLine();
			}else{
				Iterator<DocumentNodeType> iterator = superNodeTypes.iterator();
				while (iterator.hasNext()) {
					DocumentNodeType type = (DocumentNodeType) iterator.next();
					append(" ").append(type.getFullName());
					if(iterator.hasNext()){
						append(", ");
					}else{
						endLine();
					}
				}
			}
			for (IDocumentProperty p : documentNode.getProperties()) {
				append("  - ").append(p.getFullName()).append(" (").appendType(p);
			}
		}
		return popStringBuilder().toString();
	}
	private CndTextGenerator appendType(IDocumentProperty p) {
		append(" ( ").append(p.getPropertyType().name()).append(" ) ");
		if(p instanceof ReferencedDocumentCollection){
			append(" multiple > ").append(((ReferencedDocumentCollection) p).getType().getFullName());
		}else if(p instanceof ReferencedDocument){
			append(((ReferencedDocument) p).getType().getFullName());
		}
		return this;
	}
	@Override
	public CndTextGenerator append(String string) {
		return (CndTextGenerator) super.append(string);
	}
	public CndTextGenerator endLine(){
		append("\n");
		return this;
	}

	protected void appendNamespaces(DocumentNamespace n) {
		for (DocumentNamespace child : n.getChildren()) {
			append("<").append(child.getPrefix()).append("='").append(child.getName()).append("'>").endLine();
			appendNamespaces(child);
		}
	}
}

