package org.pavanecce.uml.uml2code.ocm;

import java.util.Map;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNamespace;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNodeType;
import org.pavanecce.uml.uml2code.AbstractUmlVisitorAdaptor;

/**
 * 
 */
public class UmlDocumentModelFileVisitorAdaptor extends AbstractUmlVisitorAdaptor<DocumentNamespace, DocumentNodeType, DocumentModelBuilder> {

	protected DocumentNamespace codeModel = new DocumentNamespace("root", "root");

	public UmlDocumentModelFileVisitorAdaptor() {
		super();
	}

	@Override
	public void startVisiting(DocumentModelBuilder builder, Model model) {
		super.startVisiting(builder, model);
	}

	public UmlDocumentModelFileVisitorAdaptor(Map<String, Classifier> interfacesToImplement) {
		super(interfacesToImplement);
	}

	@Override
	public DocumentNamespace getCodeModel() {
		return codeModel;
	}

	@Override
	protected void doArtificialInterfaceImplementation(DocumentNodeType codeClass, Classifier toImplement, DocumentModelBuilder builder) {

	}
}