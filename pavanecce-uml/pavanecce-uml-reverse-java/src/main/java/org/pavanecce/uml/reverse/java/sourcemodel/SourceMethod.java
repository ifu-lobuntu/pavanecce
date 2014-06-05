package org.pavanecce.uml.reverse.java.sourcemodel;

import org.eclipse.uml2.uml.VisibilityKind;

public interface SourceMethod {

	VisibilityKind getVisibility();

	boolean isConstructor();

	SourceClass getReturnType();

	SourceClass[] getParameterTypes();

	String getName();

	SourceCode getSource();

	boolean isAccessor();

	SourceAnnotation[] getAnnotations();

	String[] getParameterNames();

	boolean isStatic();

}