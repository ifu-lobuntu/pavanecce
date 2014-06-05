package org.pavanecce.uml.uml2code.java;

import java.util.Collection;

import org.pavanecce.common.code.metamodel.AssociationCollectionTypeReference;
import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeCollectionKind;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CollectionTypeReference;
import org.pavanecce.common.code.metamodel.PrimitiveTypeReference;
import org.pavanecce.common.collections.ManyToManyCollection;
import org.pavanecce.common.collections.ManyToManySet;
import org.pavanecce.common.collections.OneToManySet;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.uml2code.AbstractCodeGenerator;
import org.pavanecce.uml.uml2code.jpa.AbstractJavaCodeDecorator;

public class AssociationCollectionCodeDecorator extends AbstractJavaCodeDecorator {

	@Override
	public void decorateFieldDeclaration(JavaCodeGenerator sb, CodeField field) {
		if (field.getType() instanceof AssociationCollectionTypeReference) {
			AssociationCollectionTypeReference type = (AssociationCollectionTypeReference) field.getType();
			if (type.getKind() == CodeCollectionKind.SET) {
				if (type.getOtherFieldType() instanceof AssociationCollectionTypeReference) {
					AssociationCollectionTypeReference otherType = (AssociationCollectionTypeReference) type.getOtherFieldType();
					CodeTypeReference elementType = type.getElementTypes().get(0).getType();
					CodeTypeReference otherElementType = otherType.getElementTypes().get(0).getType();
					sb.append("  @SuppressWarnings(\"serial\")");
					sb.append("  private transient ManyToManySet<").appendType(otherElementType).append(",").appendType(elementType).append("> ");
					sb.append(field.getName()).append("Wrapper");
					sb.append(" = new ").append("ManyToManySet<").appendType(otherElementType).append(",").appendType(elementType).append(">(this){\n");
					sb.append("      public ").appendType(type).append(" getDelegate(){\n");
					sb.append("        return ").append(field.getName()).appendLineEnd();
					sb.append("      }\n");
					sb.append("      @SuppressWarnings(\"unchecked\")\n");
					sb.append("      protected ManyToManyCollection<").appendType(elementType).append(",").appendType(otherElementType)
							.append("> getOtherEnd(").appendType(elementType).append(" other){\n");
					sb.append("        return ").append("(ManyToManyCollection<").appendType(elementType).append(",").appendType(otherElementType)
							.append(">)other.get").append(NameConverter.capitalize(type.getOtherFieldName())).append("()").appendLineEnd();
					sb.append("      }\n");
					sb.append("      public boolean isLoaded(){\n");
					// TODO switching this optimization off until we figure out how to "flush"
					sb.append("         return ").append(type.isChild() ? "true" : "true").appendLineEnd();
					sb.append("      }\n");
					sb.append("      public boolean isInstanceOfChild(Object o){\n");
					sb.append("         return o instanceof ").appendType(elementType).appendLineEnd();
					sb.append("      }\n");
					sb.append("  };\n");
				} else {
					CodeTypeReference otherType = type.getOtherFieldType();
					CodeTypeReference elementType = type.getElementTypes().get(0).getType();
					sb.append("  @SuppressWarnings(\"serial\")");
					sb.append("  private transient OneToManySet<").appendType(otherType).append(",").appendType(elementType).append("> ");
					sb.append(field.getName()).append("Wrapper");
					sb.append(" = new ").append("OneToManySet<").appendType(otherType).append(",").appendType(elementType).append(">(this){\n");
					sb.append("      public ").appendType(type).append(" getDelegate(){\n");
					sb.append("        return ").append(field.getName()).appendLineEnd();
					sb.append("      }\n");
					sb.append("      @SuppressWarnings(\"unchecked\")\n");
					sb.append("      protected OneToManySet<").appendType(otherType).append(",").appendType(elementType).append("> getChildren(")
							.appendType(otherType).append(" parent){\n");
					sb.append("        return ").append("(OneToManySet<").appendType(otherType).append(",").appendType(elementType).append(">)parent.get")
							.append(NameConverter.capitalize(field.getName())).append("()").appendLineEnd();
					sb.append("      }\n");
					sb.append("      public ").appendType(otherType).append(" getParent(").appendType(elementType).append(" child){\n");
					sb.append("        return ").append("(").appendType(otherType).append(")child.get")
							.append(NameConverter.capitalize(type.getOtherFieldName())).append("()").appendLineEnd();
					sb.append("      }\n");
					sb.append("      public void setParent(").appendType(elementType).append(" child, ").appendType(otherType).append(" parent){\n");
					sb.append("        child.zz_internalSet").append(NameConverter.capitalize(type.getOtherFieldName())).append("(parent)").appendLineEnd();
					sb.append("      }\n");
					sb.append("      public boolean isLoaded(){\n");
					// TODO switching this optimization off until we figure out how to "flush" - hibernate optimizes
					// this internally anyway
					sb.append("         return ").append(type.isChild() ? "true" : "true").appendLineEnd();
					sb.append("      }\n");
					sb.append("      public boolean isInstanceOfChild(Object o){\n");
					sb.append("         return o instanceof ").appendType(elementType).appendLineEnd();
					sb.append("      }\n");
					sb.append("  };\n");
				}
			}

		}

	}

	@Override
	public void decorateClassDeclaration(JavaCodeGenerator sb, CodeClass cc) {

	}

	@Override
	public void appendAdditionalImports(JavaCodeGenerator sb, CodeClassifier cc) {
		Collection<CodeField> values = cc.getFields().values();
		for (CodeField field : values) {

			if (field.getType() instanceof AssociationCollectionTypeReference) {
				AssociationCollectionTypeReference type = (AssociationCollectionTypeReference) field.getType();
				if (type.getKind() == CodeCollectionKind.SET) {
					if (type.getOtherFieldType() instanceof AssociationCollectionTypeReference) {
						importClass(sb, ManyToManySet.class);
						importClass(sb, ManyToManyCollection.class);
					} else {
						importClass(sb, OneToManySet.class);
					}
				}
			}
		}

	}

	protected void importClass(AbstractCodeGenerator sb, Class<?> class1) {
		sb.append("import ");
		sb.append(class1.getName());
		sb.append(";\n");
	}

	@Override
	public void appendAdditionalMethods(JavaCodeGenerator sb, CodeClassifier cc) {
		for (CodeField field : cc.getFields().values()) {
			CodeTypeReference type = field.getType();
			if (!(type instanceof CollectionTypeReference || type instanceof PrimitiveTypeReference)) {
				sb.append("  public void zz_internalSet").append(NameConverter.capitalize(field.getName())).append("(").appendType(type).append(" value){\n");
				sb.append("    this.").append(field.getName()).append("=value").appendLineEnd();
				sb.append("  }\n");
			}
		}

	}

	@Override
	public void appendAdditionalFields(JavaCodeGenerator sb, CodeClassifier cc) {

	}
}
