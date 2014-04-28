package org.pavanecce.uml.reverse.owl;

import java.util.Map;

import org.pavanecce.uml.reverse.java.sourcemodel.SourceAnnotation;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceMethod;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceProperty;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceVariable;
import org.semanticweb.owlapi.model.OWLClass;

public class OwlSourceClass implements SourceClass {
	OWLClass owlClass;

	@Override
	public Map<String, SourceProperty> getPropertyDescriptors() {
		return null;
	}

	@Override
	public SourceClass[] getTypeArguments() {
		return new SourceClass[0];
	}

	@Override
	public boolean isManyType() {
		return false;
	}

	@Override
	public SourceClass getBaseType() {
		return null;
	}

	@Override
	public String getQualifiedName() {
		return null;
	}

	@Override
	public boolean isParameterizedType() {
		return false;
	}

	@Override
	public boolean isInterface() {
		return false;
	}

	@Override
	public boolean isAnnotation() {
		return false;
	}

	@Override
	public String getName() {
		return owlClass.getIRI().toString();
	}

	@Override
	public String getPackageName() {
		return null;
	}

	@Override
	public SourceClass getSuperclass() {
		return null;
	}

	@Override
	public boolean isEnum() {
		return false;
	}

	@Override
	public boolean isEntity() {
		return false;
	}

	@Override
	public boolean isDataType() {
		return false;
	}

	@Override
	public boolean isHelper() {
		return false;
	}

	@Override
	public SourceClass[] getInterfaces() {
		return null;
	}

	@Override
	public SourceAnnotation[] getAnnotations() {
		return null;
	}

	@Override
	public SourceAnnotation getAnnotation(String string) {
		return null;
	}

	@Override
	public SourceMethod[] getDeclaredMethods() {
		return null;
	}

	@Override
	public SourceVariable[] getDeclaredFields() {
		return null;
	}

	@Override
	public boolean isUniqueCollectionType() {
		return false;
	}

	@Override
	public boolean isOrderedCollectionType() {
		return false;
	}

}
