package org.pavanecce.uml.uml2code.codemodel;

import static org.pavanecce.common.util.NameConverter.capitalize;

import org.eclipse.uml2.uml.Property;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;
import org.pavanecce.common.code.metamodel.PrimitiveTypeReference;

public class EmfCodeModelBuilder extends CodeModelBuilder {
	@Override
	protected String generateGetterName(Property p, CodeField cf, String capitalized) {
		String getterName = "get" + capitalized;
		if (cf.getType() instanceof PrimitiveTypeReference && ((PrimitiveTypeReference) cf.getType()).getKind() == CodePrimitiveTypeKind.BOOLEAN) {
			if (p.getName().startsWith("is")) {
				getterName = p.getName();
			} else {
				getterName = "is" + capitalize(p.getName());
			}
		}
		return getterName;
	}
}
