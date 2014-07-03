package org.pavanecce.uml.uml2code.codemodel;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Property;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.EmfPropertyUtil;

public class DocumentNameUtil {

	public static String name(Property p) {
		if (EmfPropertyUtil.isMany(p) && p.getType().getName().equalsIgnoreCase(p.getName())) {
			return NameConverter.decapitalize(NameConverter.toValidVariableName(p.getName())) + "Collection";

		} else {
			return NameConverter.decapitalize(NameConverter.toValidVariableName(p.getName()));
		}
	}

	public static String name(Classifier c) {
		return NameConverter.decapitalize(NameConverter.toValidVariableName(c.getName()));
	}
}
