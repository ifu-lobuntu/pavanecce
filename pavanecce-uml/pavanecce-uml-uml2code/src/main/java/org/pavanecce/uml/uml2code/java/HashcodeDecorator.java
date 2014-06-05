package org.pavanecce.uml.uml2code.java;

import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.uml.uml2code.jpa.AbstractJavaCodeDecorator;

public class HashcodeDecorator extends AbstractJavaCodeDecorator {

	@Override
	public void decorateFieldDeclaration(JavaCodeGenerator sb, CodeField field) {
	}

	@Override
	public void decorateClassDeclaration(JavaCodeGenerator sb, CodeClass cc) {
	}

	@Override
	public void appendAdditionalImports(JavaCodeGenerator sb, CodeClassifier cc) {
	}

	@Override
	public void appendAdditionalMethods(JavaCodeGenerator sb, CodeClassifier cc) {
		sb.append("  public int hashCode(){\n");
		sb.append("    return getUuid().hashCode()").appendLineEnd();
		sb.append("  }\n");
		sb.append("  public boolean equals(Object o){\n");
		sb.append("    return o instanceof ").append(cc.getName()).append(" && ((").append(cc.getName()).append(")o).getUuid().equals(getUuid())")
				.appendLineEnd();
		sb.append("  }\n");
		sb.append("  public String getUuid(){\n");
		sb.append("    if(uuid==null){\n");
		sb.append("      uuid=java.util.UUID.randomUUID().toString()").appendLineEnd();
		sb.append("    }\n");
		sb.append("    return uuid").appendLineEnd();
		sb.append("  }\n");
		sb.append("  public void setUuid(String uuid){\n");
		sb.append("    this.uuid=uuid").appendLineEnd();
		sb.append("  }\n");
	}

	@Override
	public void appendAdditionalFields(JavaCodeGenerator sb, CodeClassifier cc) {
		sb.append("  private String uuid=getUuid()").appendLineEnd();
	}

}
