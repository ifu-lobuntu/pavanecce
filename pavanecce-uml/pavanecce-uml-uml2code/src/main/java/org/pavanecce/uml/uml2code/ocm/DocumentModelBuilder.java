package org.pavanecce.uml.uml2code.ocm;

import java.util.SortedSet;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.pavanecce.common.code.metamodel.documentdb.DocumentEnumeratedType;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNamespace;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNodeType;
import org.pavanecce.common.code.metamodel.documentdb.IChildDocument;
import org.pavanecce.common.code.metamodel.documentdb.IDocumentElement;
import org.pavanecce.common.code.metamodel.documentdb.IDocumentProperty;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.uml2code.AbstractBuilder;
import org.pavanecce.uml.uml2code.codemodel.DocumentUtil;

public class DocumentModelBuilder extends AbstractBuilder<DocumentNamespace, DocumentNodeType> {
	DocumentNamespace rootNamespace;
	private DocumentUtil documentUtil;

	@Override
	public DocumentNamespace visitModel(Model model) {
		DocumentNamespace result = documentUtil.buildNamespace(model);
		rootNamespace.addChild(result);
		return result;
	}

	@Override
	public void initialize(SortedSet<Model> models, DocumentNamespace p) {
		this.rootNamespace = p;
		this.documentUtil = new DocumentUtil();
	}

	@Override
	public DocumentNamespace visitPackage(Package model, DocumentNamespace parent) {
		DocumentNamespace result = documentUtil.buildNamespace(model);
		parent.addChild(result);
		return result;
	}

	@Override
	public DocumentNodeType visitClass(Class cl, DocumentNamespace parent) {
		DocumentNodeType result = documentUtil.getDocumentNode(cl);
		parent.addNodeType(result);
		return result;
	}

	@Override
	public void visitProperty(Property p, DocumentNodeType parent) {
		IDocumentElement e = documentUtil.buildDocumentElement(p);
		if (e instanceof IDocumentProperty) {
			parent.addProperty((IDocumentProperty) e);
		} else if (e instanceof IChildDocument) {
			parent.addChild((IChildDocument) e);
		}
	}

	@Override
	public void visitOperation(Operation o, DocumentNodeType parent) {
	}

	public DocumentNamespace getDocumentModel() {
		return rootNamespace;
	}

	@Override
	public DocumentNodeType visitEnum(Enumeration en, DocumentNamespace parent) {
		DocumentNodeType result = documentUtil.getDocumentNode(en);
		parent.addNodeType(result);
		return result;
	}

	@Override
	public void visitEnumerationLiteral(EnumerationLiteral el, DocumentNodeType parent) {
		DocumentEnumeratedType type = (DocumentEnumeratedType) parent;
		type.addLiteral(NameConverter.toUnderscoreStyle(NameConverter.toValidVariableName(el.getName())).toUpperCase());
	}

}
