package org.pavanecce.uml.uml2code.codemodel;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNamespace;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.EmfElementFinder;

public class DocumentNameUtil {

	public static String name(Property p) {
		return NameConverter.decapitalize(NameConverter.toValidVariableName(p.getName()));
	}

	public static String name(Class c) {
		return NameConverter.decapitalize(NameConverter.toValidVariableName(c.getName()));
	}
}
