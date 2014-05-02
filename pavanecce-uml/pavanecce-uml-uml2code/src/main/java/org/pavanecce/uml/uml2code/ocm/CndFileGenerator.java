package org.pavanecce.uml.uml2code.ocm;

import java.util.SortedSet;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNamespace;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNodeType;
import org.pavanecce.common.code.metamodel.documentdb.IChildDocument;
import org.pavanecce.common.code.metamodel.documentdb.IDocumentElement;
import org.pavanecce.common.code.metamodel.documentdb.IDocumentProperty;
import org.pavanecce.uml.uml2code.AbstractBuilder;
import org.pavanecce.uml.uml2code.codemodel.DocumentUtil;

public class CndFileGenerator  extends AbstractBuilder<DocumentNamespace, DocumentNodeType> {
	DocumentNamespace rootNamespace;
	private DocumentUtil documentUtil;
	

	@Override
	public DocumentNamespace visitModel(Model model) {
		return rootNamespace;
	}

	@Override
	public void initialize(SortedSet<Model> models, DocumentNamespace p) {
		this.rootNamespace=p;
		this.documentUtil=new DocumentUtil();
	}

	@Override
	public DocumentNamespace visitPackage(Package model, DocumentNamespace parent) {
		return documentUtil.buildNamespace(model);
	}

	@Override
	public DocumentNodeType visitClass(Class cl, DocumentNamespace parent) {
		return documentUtil.getDocumentNode(cl);
	}

	@Override
	public void visitProperty(Property p, DocumentNodeType parent) {
		IDocumentElement e = documentUtil.buildDocumentElement(p);
		if(e instanceof IDocumentProperty){
			parent.addProperty((IDocumentProperty) e);
		}else if(e instanceof IChildDocument){
			parent.addChild((IChildDocument) e);
		}
	}

	@Override
	public void visitOperation(Operation o, DocumentNodeType parent) {
	}

}
