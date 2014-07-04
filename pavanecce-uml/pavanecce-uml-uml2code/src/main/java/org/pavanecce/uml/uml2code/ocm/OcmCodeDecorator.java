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
			DocumentNodeType nodeType = (DocumentNodeType) element;
			if (!hasPkField(cc, nodeType)) {
				sb.append("  @Field(uuid=true)\n");
				sb.append("  String ").append(getPkFieldName(nodeType)).appendLineEnd();
			}
			if (!hasPathField(cc, nodeType)) {
				sb.append("  @Field(path=true)\n");
				sb.append("  String ").append(getPathFieldName(nodeType)).append(" = \"/").append(cc.getName()).append("Collection\"").appendLineEnd();
			}
		}
	}

	@Override
	public void appendAdditionalInnerClasses(JavaCodeGenerator sb, CodeClassifier cc) {
		for (CodeField codeField : cc.getFields().values()) {
			if (codeField.getData(IDocumentElement.class) instanceof DocumentEnumProperty) {
				sb.append("  public static class ConverterFor").append(codeField.getName()).append(" extends GenericEnumTypeConverter<")
						.append(sb.typeLastName(codeField.getType())).append(">{};\n");
			}
		}
	}

	@Override
	public void appendAdditionalConstructors(JavaCodeGenerator sb, CodeClassifier cc) {
		if (cc instanceof CodeClass) {
			sb.append("  public ").append(cc.getName()).append("(String path){\n");
			sb.append("    this.path = path;\n");
			sb.append("  }\n");
		}
	}

	protected boolean hasPkField(CodeClassifier cc, DocumentNodeType nodeType) {
		return cc.getFields().get(getPkFieldName(nodeType)) != null;
	}

	protected boolean hasPathField(CodeClassifier cc, DocumentNodeType nodeType) {
		return cc.getFields().get(getPathFieldName(nodeType)) != null;
	}

	private String getPathFieldName(DocumentNodeType nodeType) {
		return nodeType.getPathField() == null ? "path" : nodeType.getPathField();
	}

	protected String getPkFieldName(DocumentNodeType nodeType) {
		return nodeType.getUuidField() == null ? "id" : nodeType.getUuidField();
	}

	@Override
	public void appendAdditionalMethods(JavaCodeGenerator sb, CodeClassifier cc) {
		IDocumentElement element = cc.getData(IDocumentElement.class);
		if (element instanceof DocumentNodeType) {
			DocumentNodeType nodeType = (DocumentNodeType) element;
			if (!hasPkField(cc, nodeType)) {
				String pkFieldName = getPkFieldName(nodeType);
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
			if (!hasPathField(cc, nodeType)) {
				String pathFieldName = getPathFieldName(nodeType);
				sb.append("  public String get");
				sb.append(NameConverter.capitalize(pathFieldName));
				sb.append("(){\n");
				sb.append("    return this.");
				sb.append(pathFieldName);
				sb.appendLineEnd();
				sb.append("  }\n");
				sb.append("  public void set");
				sb.append(NameConverter.capitalize(pathFieldName));
				sb.append("(String value){\n");
				sb.append("    this.");
				sb.append(pathFieldName);
				sb.append("=value");
				sb.appendLineEnd();
				sb.append("  }\n");
			}
			// TODO name fields
		}
	}

	@Override
	public void appendAdditionalImports(JavaCodeGenerator sb, CodeClassifier cc) {
		sb.append("import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node").appendLineEnd();
		sb.append("import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field").appendLineEnd();
		SortedSet<String> imports = new TreeSet<String>();
		Set<Entry<String, CodeField>> entrySet = cc.getFields().entrySet();
		for (Entry<String, CodeField> entry : entrySet) {
			CodeField field = entry.getValue();
			IDocumentElement data = field.getData(IDocumentElement.class);
			if (data instanceof ChildDocumentCollection) {
				imports.add("org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection");
			} else if (data instanceof ReferencedDocumentCollection) {
				imports.add("org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection");
				imports.add("org.apache.jackrabbit.ocm.manager.collectionconverter.impl.BeanReferenceCollectionConverterImpl");
			} else if (data instanceof ReferencedDocument) {
				imports.add("org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean");
				imports.add("org.apache.jackrabbit.ocm.manager.beanconverter.impl.ReferenceBeanConverterImpl");
			} else if (data instanceof ChildDocument) {
				imports.add("org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean");
			} else if (data instanceof DocumentProperty) {
				if (data instanceof DocumentEnumProperty) {
					imports.add("org.pavanecce.common.ocm.GenericEnumTypeConverter");
				}
			} else if (data instanceof ParentDocument) {
				imports.add("org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean");
				if (((ParentDocument) data).isChildIsMany()) {
					imports.add("org.pavanecce.common.ocm.GrandParentBeanConverterImpl");
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
			sb.append("@Node(jcrType = \"").append(((DocumentNodeType) element).getFullName()).append("\", discriminator = false)\n");
		}

	}

	@Override
	public void decorateFieldDeclaration(JavaCodeGenerator sb, CodeField field) {
		if (field.getOwner() instanceof CodeClass) {
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
				sb.append("  @Collection(jcrName = \"").append(children.getFullName()).append("\", jcrElementName = \"")
						.append(children.getType().getFullName()).append("\")\n");
			} else if (element instanceof ReferencedDocumentCollection) {
				ReferencedDocumentCollection refs = (ReferencedDocumentCollection) element;
				sb.append("  @Collection(jcrName = \"").append(refs.getFullName())
						.append("\", collectionConverter =  BeanReferenceCollectionConverterImpl.class)\n");
			} else if (element instanceof DocumentEnumProperty) {
				DocumentEnumProperty prop = (DocumentEnumProperty) element;
				sb.append("  @Field(jcrName = \"").append(prop.getFullName()).append("\", converter = ConverterFor").append(field.getName())
						.append(".class)\n");
			} else if (element instanceof DocumentProperty) {
				DocumentProperty prop = (DocumentProperty) element;
				sb.append("  @Field(");
				if (prop.isUuid()) {
					sb.append("uuid = true)\n");
				} else {
					sb.append("jcrName = \"").append(prop.getFullName());
					sb.append("\"");
					if (prop.isPath()) {
						sb.append(", path = true");
					}
					sb.append(", jcrType = \"");
					sb.append(prop.getPropertyType().name());
					sb.append("\")\n");
				}
			}
		}
	}
}
