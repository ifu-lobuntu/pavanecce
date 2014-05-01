package org.pavanecce.uml.uml2code.jpa;

import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeMethod;

public abstract class AbstractJavaCodeDecorator {

	public AbstractJavaCodeDecorator() {
		super();
	}

	public abstract void decorateFieldDeclaration(StringBuilder sb, CodeField field);

	public abstract void decorateClassDeclaration(StringBuilder sb, CodeClass cc);

	public abstract void appendAdditionalImports(StringBuilder sb, CodeClassifier cc);

	public abstract void appendAdditionalMethods(StringBuilder sb, CodeClassifier cc);

	public abstract void appendAdditionalFields(StringBuilder sb, CodeClassifier cc);

	public StringBuilder appendLineEnd(StringBuilder sb) {
		return sb.append(";\n");
	}

	public void decorateMethodDeclaration(StringBuilder sb, CodeMethod value) {
		
	}

}