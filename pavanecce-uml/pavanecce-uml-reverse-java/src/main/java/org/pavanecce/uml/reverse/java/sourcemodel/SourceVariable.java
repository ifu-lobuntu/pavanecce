package org.pavanecce.uml.reverse.java.sourcemodel;

public interface SourceVariable {

	public abstract boolean isEnumConstant();

	public abstract String getName();

	SourceAnnotation[] getAnnotations();
}