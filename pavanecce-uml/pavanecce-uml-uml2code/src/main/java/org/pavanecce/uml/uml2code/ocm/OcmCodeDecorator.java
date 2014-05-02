package org.pavanecce.uml.uml2code.ocm;

import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeClassifier;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.documentdb.ChildDocument;
import org.pavanecce.common.code.metamodel.documentdb.ChildDocumentCollection;
import org.pavanecce.common.code.metamodel.documentdb.DocumentEnumProperty;
import org.pavanecce.common.code.metamodel.documentdb.DocumentNodeType;
import org.pavanecce.common.code.metamodel.documentdb.DocumentProperty;
import org.pavanecce.common.code.metamodel.documentdb.IDocumentElement;
import org.pavanecce.common.code.metamodel.documentdb.ParentDocument;
import org.pavanecce.common.code.metamodel.documentdb.ReferencedDocument;
import org.pavanecce.common.code.metamodel.documentdb.ReferencedDocumentCollection;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;
import org.pavanecce.uml.uml2code.jpa.AbstractJavaCodeDecorator;

public class OcmCodeDecorator extends AbstractJavaCodeDecorator {
	@Override
	public void appendAdditionalFields(JavaCodeGenerator sb, CodeClassifier cc) {
		IDocumentElement element = cc.getData(IDocumentElement.class);
		if (element instanceof DocumentNodeType) {
			DocumentNodeType relationalTable = (DocumentNodeType) element;
			if (!hasPkField(cc, relationalTable)) {
				sb.append("  @Field(uuid=true)\n");
				sb.append("  String ").append(getPkFieldName(relationalTable)).appendLineEnd();
			}
		}
	}

	protected boolean hasPkField(CodeClassifier cc, DocumentNodeType relationalTable) {
		return cc.getFields().get(getPkFieldName(relationalTable)) != null;
	}

	protected String getPkFieldName(DocumentNodeType relationalTable) {
		return relationalTable.getUuidField() == null ? "id" : relationalTable.getUuidField();
	}

	@Override
	public void appendAdditionalMethods(JavaCodeGenerator sb, CodeClassifier cc) {
		IDocumentElement element = cc.getData(IDocumentElement.class);
		if (element instanceof DocumentNodeType) {
			DocumentNodeType relationalTable = (DocumentNodeType) element;
			if (!hasPkField(cc, relationalTable)) {
				String pkFieldName = getPkFieldName(relationalTable);
				sb.append("  public String get");
				sb.append(NameConverter.capitalize(pkFieldName));
				sb.append("(){\n");
				sb.append("    return this.");
				sb.append(pkFieldName);
				sb.appendLineEnd();
				sb.append("  }\n");
				sb.append("  public void set");
				sb.append(NameConverter.capitalize(pkFieldName));
				sb.append("(String value){\n");
				sb.append("    this.");
				sb.append(pkFieldName);
				sb.append("=value");
				sb.appendLineEnd();
				sb.append("  }\n");
			}
		}
	}

	@Override
	public void appendAdditionalImports(JavaCodeGenerator sb, CodeClassifier cc) {
		sb.append("import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node").appendLineEnd();
		sb.append("import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field").appendLineEnd();
		sb.append("import javax.persistence.Table;\n");
		SortedSet<String> imports = new TreeSet<String>();
		Set<Entry<String, CodeField>> entrySet = cc.getFields().entrySet();
		for (Entry<String, CodeField> entry : entrySet) {
			CodeField field = entry.getValue();
			IDocumentElement data = field.getData(IDocumentElement.class);
			if (data instanceof ChildDocumentCollection) {
				imports.add("org.apache.jackrabbit.ocm.mapper.impl.annotation.Field");
			} else if (data instanceof ReferencedDocumentCollection) {
				imports.add("org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection");
				imports.add("org.apache.jackrabbit.ocm.manager.collectionconverter.impl.BeanReferenceCollectionConverterImpl");
			} else if (data instanceof ReferencedDocument) {
				imports.add("org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean");
				imports.add("org.apache.jackrabbit.ocm.manager.beanconverter.impl.ReferenceBeanConverterImpl");
			} else if (data instanceof ChildDocument) {
				imports.add("org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean");
			} else if (data instanceof DocumentProperty) {
				imports.add("org.apache.jackrabbit.ocm.mapper.impl.annotation.Field");
				if (data instanceof DocumentEnumProperty) {
					imports.add("org.apache.jackrabbit.ocm.manager.enumconverter.EnumTypeConverter");
				}
			} else if (data instanceof ParentDocument) {
				if (((ParentDocument) data).isChildIsMany()) {
					imports.add("org.pavanecce.cmmn.jbpm.ocm.GrandParentBeanConverterImpl");
				} else {
					imports.add("org.apache.jackrabbit.ocm.manager.beanconverter.impl.ParentBeanConverterImpl");
				}

			}
		}
		for (String string : imports) {
			sb.append("import ");
			sb.append(string);
			sb.appendLineEnd();
		}
	}

	@Override
	public void decorateClassDeclaration(JavaCodeGenerator sb, CodeClass cc) {
		IDocumentElement element = cc.getData(IDocumentElement.class);
		if (element instanceof DocumentNodeType) {
			sb.append("@Node(name=\"").append(((DocumentNodeType) element).getFullName()).append("\", discriminator = false))\n");
		}
	}

	@Override
	public void decorateFieldDeclaration(JavaCodeGenerator sb, CodeField field) {
		IDocumentElement element = field.getData(IDocumentElement.class);
		if (element instanceof ChildDocument) {
			sb.append("  @Bean(jcrName = \"").append(((ChildDocument) element).getFullName()).append("\")\n");
		} else if (element instanceof ParentDocument) {
			String converter = null;
			ParentDocument parentDocument = (ParentDocument) element;
			if (parentDocument.isChildIsMany()) {
				converter = "GrandParentBeanConverterImpl";
			} else {
				converter = "ParentBeanConverterImpl";
			}
			sb.append("  @Bean(jcrName = \"").append(parentDocument.getFullName()).append("\", converter=").append(converter).append(".class)\n");
		} else if (element instanceof ReferencedDocument) {
			ReferencedDocument ref = (ReferencedDocument) element;
			sb.append("  @Bean(jcrName = \"").append(ref.getFullName()).append("\", converter = ReferenceBeanConverterImpl.class)\n");
		} else if (element instanceof ChildDocumentCollection) {
			ChildDocumentCollection children = (ChildDocumentCollection) element;
			sb.append("  @Collection(jcrName = \"").append(children.getFullName()).append("\", jcrElementName = \")").append(children.getType().getFullName()).append("\")\n");
		} else if (element instanceof ReferencedDocumentCollection) {
			ReferencedDocumentCollection refs = (ReferencedDocumentCollection) element;
			sb.append("  @Collection(jcrName = \"").append(refs.getFullName()).append("\", collectionConverter =  BeanReferenceCollectionConverterImpl.class)\n");
		} else if (element instanceof DocumentEnumProperty) {
			DocumentEnumProperty prop = (DocumentEnumProperty) element;
			sb.append("  @Field(jcrName = \"").append(prop.getFullName()).append("\", converter = EnumTypeConverter.class)\n");
		}else{
			DocumentProperty prop = (DocumentProperty) element;
			sb.append("  @Field(jcrName = \"").append(prop.getFullName()).append("\")\n");
		}
	}
}
