package org.pavanecce.uml.uml2code.codemodel;

import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeEnumeration;
import org.pavanecce.common.code.metamodel.CodePackage;

public class PersistedEnumCodeModelBuilder extends CodeModelBuilder {
	@Override
	public CodeEnumeration visitEnum(Enumeration en, CodePackage parent) {
		CodeEnumeration result = super.visitEnum(en, parent);
		result.setName(result.getName()+"Enum");
		return result;
	}

	@Override
	public void visitEnumerationLiteral(EnumerationLiteral el, CodeClassifier parent) {
		super.visitEnumerationLiteral(el, parent);
	}
}
