package org.pavanecce.uml.reverse.java.sourcemodel;

import org.eclipse.uml2.uml.VisibilityKind;

public interface SourceProperty {

	Object getInitialValue();

	SourceClass getDeclaringType();

	boolean isComposite();

	boolean isStatic();

	SourceAnnotation getAnnotation(String string);

	SourceProperty getOtherEnd();

	String getName();

	boolean isDerived();

	SourceVariable getField();

	SourceClass getType();

	SourceClass getBaseType();

	boolean isMany();

	SourceAnnotation[] getAnnotations();

	boolean isReadOnly();

	String getMappedBy();

	boolean isUnique();

	boolean isOrdered();

	VisibilityKind getVisibility();

}