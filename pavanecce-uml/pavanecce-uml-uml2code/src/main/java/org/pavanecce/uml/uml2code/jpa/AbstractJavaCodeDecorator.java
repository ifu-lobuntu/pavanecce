package org.pavanecce.uml.uml2code.jpa;

import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeMethod;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

public abstract class AbstractJavaCodeDecorator {

	public AbstractJavaCodeDecorator() {
		super();
	}

	public abstract void decorateFieldDeclaration(JavaCodeGenerator sb, CodeField field);

	public abstract void decorateClassDeclaration(JavaCodeGenerator sb, CodeClass cc);

	public abstract void appendAdditionalImports(JavaCodeGenerator sb, CodeClassifier cc);

	public abstract void appendAdditionalMethods(JavaCodeGenerator sb, CodeClassifier cc);
	public void appendAdditionalConstructors(JavaCodeGenerator sb, CodeClassifier cc){
		
	}

	public abstract void appendAdditionalFields(JavaCodeGenerator sb, CodeClassifier cc);

	public void appendAdditionalInnerClasses(JavaCodeGenerator sb, CodeClassifier cc) {

	}

	public void decorateMethodDeclaration(JavaCodeGenerator sb, CodeMethod value) {

	}

}