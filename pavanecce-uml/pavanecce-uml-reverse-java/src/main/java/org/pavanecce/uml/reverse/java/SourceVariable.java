package org.pavanecce.uml.reverse.java;

public interface SourceVariable {

	public abstract boolean isEnumConstant();

	public abstract String getName();
	SourceAnnotation[] getAnnotations();
}