package org.pavanecce.uml.uml2code.codemodel;

import java.util.Map;

import org.eclipse.uml2.uml.Classifier;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.code.metamodel.CodePackage;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.uml.uml2code.AbstractUmlVisitorAdaptor;

/**
 * This class implements the visiting logic required to traverse a UML model. When arriving at significant UML elements,
 * it invokes callbacks on the CodeModelBuilder provided as input for startVisiting(DefaultCodeModelBuilder, Model)
 * 
 * @author ampie
 * 
 */
public class UmlCodeModelVisitorAdaptor extends AbstractUmlVisitorAdaptor<CodePackage, CodeClassifier, DefaultCodeModelBuilder> {

	protected CodeModel codeModel = new CodeModel();

	public UmlCodeModelVisitorAdaptor() {
		super();
	}

	public UmlCodeModelVisitorAdaptor(Map<String, Classifier> interfacesToImplement) {
		super(interfacesToImplement);
	}

	@Override
	public CodeModel getCodeModel() {
		return codeModel;
	}

	@Override
	protected void doArtificialInterfaceImplementation(CodeClassifier codeClassifier, Classifier toImplement, DefaultCodeModelBuilder builder) {
		if (codeClassifier instanceof CodeClass) {
			// TODO find a better place for this - this was just to make
			// uml.Classifer compile
			CodeClass codeClass = (CodeClass) codeClassifier;
			codeClass.setSuperClass(new CodeTypeReference(false, org.eclipse.emf.ecore.impl.EModelElementImpl.class.getName().split("\\.")));
			codeClass.addToImplementedInterfaces(builder.calculateTypeReference(toImplement));
		}
	}
}