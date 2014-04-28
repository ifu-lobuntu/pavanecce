package org.pavanecce.uml.reverse.java.sourcemodel.reflect;

import java.lang.reflect.Modifier;

import org.eclipse.uml2.uml.VisibilityKind;

public class JavaReflectUtil {
	public static VisibilityKind toVisibility(int modifiers) {
		if (Modifier.isPublic(modifiers)) {
			return VisibilityKind.PUBLIC_LITERAL;
		} else if (Modifier.isProtected(modifiers)) {
			return VisibilityKind.PROTECTED_LITERAL;
		} else if (Modifier.isPrivate(modifiers)) {
			return VisibilityKind.PRIVATE_LITERAL;
		}
		return VisibilityKind.PACKAGE_LITERAL;

	}
}
