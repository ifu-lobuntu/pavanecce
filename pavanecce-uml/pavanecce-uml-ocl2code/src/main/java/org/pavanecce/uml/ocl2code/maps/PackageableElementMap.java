package org.pavanecce.uml.ocl2code.maps;

import org.eclipse.ocl.expressions.CollectionKind;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.VisibilityKind;
import org.pavanecce.common.code.metamodel.CodeVisibilityKind;
import org.pavanecce.uml.common.util.EmfPropertyUtil;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;

public class PackageableElementMap {
	protected NamedElement e = null;
	protected UmlToCodeMaps codeUtil;

	public PackageableElementMap(UmlToCodeMaps CodeUtil, NamedElement e) {
		super();
		this.codeUtil = CodeUtil;
		this.e = e;
	}

	public CodeVisibilityKind javaVisibility() {
		if (e.getVisibility() == VisibilityKind.PUBLIC_LITERAL)
			return CodeVisibilityKind.PUBLIC;
		if (e.getVisibility() == VisibilityKind.PRIVATE_LITERAL)
			return CodeVisibilityKind.PRIVATE;
		if (e.getVisibility() == VisibilityKind.PROTECTED_LITERAL)
			return CodeVisibilityKind.PROTECTED;
		return CodeVisibilityKind.PUBLIC;
	}

	public static CollectionKind getCollectionKind(MultiplicityElement exp) {
		boolean multiValued = exp.isMultivalued();
		if (exp instanceof Property) {
			multiValued = EmfPropertyUtil.isMany((Property) exp);
		}
		if (multiValued) {
			if (exp.isOrdered()) {
				if (exp.isUnique()) {
					return CollectionKind.ORDERED_SET_LITERAL;
				} else {
					return CollectionKind.SEQUENCE_LITERAL;
				}
			} else {
				if (exp.isUnique()) {
					return CollectionKind.SET_LITERAL;
				} else {
					return CollectionKind.BAG_LITERAL;
				}

			}
		}
		return null;
	}
}
