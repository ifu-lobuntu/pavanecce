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
public class UmlCndFileVisitorAdaptor extends AbstractUmlVisitorAdaptor<DocumentNamespace, DocumentNodeType, CndFileGenerator> {

	protected DocumentNamespace codeModel = new DocumentNamespace("root","root");

	public UmlCndFileVisitorAdaptor() {
		super();
	}
	@Override
	public void startVisiting(CndFileGenerator builder, Model model) {
		super.startVisiting(builder, model);
	}

	public UmlCndFileVisitorAdaptor(Map<String, Classifier> interfacesToImplement) {
		super(interfacesToImplement);
	}

	public DocumentNamespace getCodeModel() {
		return codeModel;
	}

	@Override
	protected void doArtificialInterfaceImplementation(DocumentNodeType codeClass, Classifier toImplement, CndFileGenerator builder) {

	}
}