package org.pavanecce.uml.uml2code.java;

import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CollectionTypeReference;
import org.pavanecce.common.code.metamodel.PrimitiveTypeReference;

public class EmfJavaCodeGenerator extends JavaCodeGenerator {
	@Override
	protected String defaultValue(CodePrimitiveTypeKind kind) {
		return super.defaultValue(kind);
	}

	@Override
	protected String getMappedName(CodeTypeReference type) {
		if (type instanceof PrimitiveTypeReference) {
			PrimitiveTypeReference ptr = (PrimitiveTypeReference) type;
			switch (ptr.getKind()) {
			case STRING:
				return "java.lang.String";
			case INTEGER:
				return "int";
			case REAL:
				return "double";
			default:
				return "boolean";
			}
		} else if (type instanceof CollectionTypeReference) {
			CollectionTypeReference ctr = (CollectionTypeReference) type;
			if (ctr.isImplementation()) {
				switch (ctr.getKind()) {
				case BAG:
					return "org.eclipse.emf.common.util.BasicEList";
				case SEQUENCE:
					return "org.eclipse.emf.common.util.BasicEList";
				case ORDERED_SET:
					return "org.eclipse.emf.common.util.BasicEList";
				default:
					return "org.eclipse.emf.common.util.BasicEList";
				}
			} else {
				switch (ctr.getKind()) {
				case BAG:
					return "org.eclipse.emf.common.util.EList";
				case SEQUENCE:
					return "org.eclipse.emf.common.util.EList";
				case ORDERED_SET:
					return "org.eclipse.emf.common.util.EList";
				default:
					return "org.eclipse.emf.common.util.EList";
				}
			}
		}
		return super.getMappedName(type);
	}

	@Override
	protected String defaultValue(CollectionTypeReference kind) {
		return "new BasicEList<" + super.elementTypeLastName(kind) + ">()";
	}
}
