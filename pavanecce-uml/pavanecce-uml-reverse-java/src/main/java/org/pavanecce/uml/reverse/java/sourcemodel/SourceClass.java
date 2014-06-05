package org.pavanecce.uml.reverse.java.sourcemodel;

import java.util.Map;

public interface SourceClass {

	Map<String, SourceProperty> getPropertyDescriptors();

	SourceClass[] getTypeArguments();

	boolean isManyType();

	SourceClass getBaseType();

	String getQualifiedName();

	boolean isParameterizedType();

	boolean isInterface();

	boolean isAnnotation();

	String getName();

	String getPackageName();

	SourceClass getSuperclass();

	boolean isEnum();

	boolean isEntity();

	boolean isDataType();

	boolean isHelper();

	SourceClass[] getInterfaces();

	SourceAnnotation[] getAnnotations();

	SourceAnnotation getAnnotation(String string);

	SourceMethod[] getDeclaredMethods();

	SourceVariable[] getDeclaredFields();

	boolean isUniqueCollectionType();

	boolean isOrderedCollectionType();

}