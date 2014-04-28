package org.pavanecce.uml.reverse.owl;

public interface SourceVariable {

	public abstract boolean isEnumConstant();

	public abstract String getName();
	SourceAnnotation[] getAnnotations();
}