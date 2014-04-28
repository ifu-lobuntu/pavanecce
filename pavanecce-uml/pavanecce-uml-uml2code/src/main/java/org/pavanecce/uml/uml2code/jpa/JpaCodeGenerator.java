package org.pavanecce.uml.uml2code.jpa;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.pavanecce.common.code.metamodel.CodeClass;
import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.CodePrimitiveTypeKind;
import org.pavanecce.common.code.metamodel.CodeTypeReference;
import org.pavanecce.common.code.metamodel.CollectionTypeReference;
import org.pavanecce.common.code.metamodel.PrimitiveTypeReference;
import org.pavanecce.common.code.metamodel.relationaldb.IRelationalElement;
import org.pavanecce.common.code.metamodel.relationaldb.RelationalColumn;
import org.pavanecce.common.code.metamodel.relationaldb.RelationalInverseLink;
import org.pavanecce.common.code.metamodel.relationaldb.RelationalLink;
import org.pavanecce.common.code.metamodel.relationaldb.RelationalTable;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

public class JpaCodeGenerator extends JavaCodeGenerator {
	Map<CodeTypeReference, JpaDataTypeStrategy> dataTypeStrategies = new HashMap<CodeTypeReference, JpaDataTypeStrategy>();
	{
		dataTypeStrategies.put(new PrimitiveTypeReference(CodePrimitiveTypeKind.INTEGER), JpaRelationalDataType.INT);
		dataTypeStrategies.put(new PrimitiveTypeReference(CodePrimitiveTypeKind.STRING), JpaRelationalDataType.STRING);
		dataTypeStrategies.put(new PrimitiveTypeReference(CodePrimitiveTypeKind.BOOLEAN), JpaRelationalDataType.BOOLEAN);
		dataTypeStrategies.put(new PrimitiveTypeReference(CodePrimitiveTypeKind.REAL), JpaRelationalDataType.DOUBLE);
		dataTypeStrategies.put(new CodeTypeReference(false,"StandardSimpleTypes", "DateTime"), JpaRelationalDataType.DATETIME);
		dataTypeStrategies.put(new CodeTypeReference(false,"StandardSimpleTypes", "Date"), JpaRelationalDataType.DATE);
		dataTypeStrategies.put(new CodeTypeReference(false,"StandardSimpleTypes", "Time"), JpaRelationalDataType.TIME);
		dataTypeStrategies.put(new CodeTypeReference(false,"StandardSimpleTypes", "LargeText"), JpaRelationalDataType.TEXT);
		dataTypeStrategies.put(new CodeTypeReference(false,"StandardSimpleTypes", "BinaryLargeObject"), JpaRelationalDataType.BINARY);
	}

	protected void appendImports(StringBuilder sb, CodeClass cc) {
		sb.append("import javax.persistence.Entity;\n");
		sb.append("import javax.persistence.Table;\n");
		SortedSet<String> imports = new TreeSet<String>();
		Set<Entry<String, CodeField>> entrySet = cc.getFields().entrySet();
		for (Entry<String, CodeField> entry : entrySet) {
			CodeField field = entry.getValue();
			IRelationalElement data = field.getData(IRelationalElement.class);
			if (data instanceof RelationalInverseLink && field.getType() instanceof CollectionTypeReference) {
				imports.add("javax.persistence.OneToMany");
			}
			if (data instanceof RelationalLink && !(field.getType() instanceof CollectionTypeReference)) {
				imports.add("javax.persistence.ManyToOne");
				imports.add("javax.persistence.JoinColumns");
				imports.add("javax.persistence.JoinColumn");
			}
			if (data instanceof RelationalColumn) {
				JpaDataTypeStrategy jpaDataTypeStrategy = dataTypeStrategies.get(field.getType());
				if (jpaDataTypeStrategy != null) {
					imports.addAll(jpaDataTypeStrategy.getImports());
				}
			}
		}
		for (String string : imports) {
			sb.append("import ");
			sb.append(string);
			appendLineEnd(sb);
		}
		super.appendImports(sb, cc);
	}

	@Override
	protected void appendClassDeclaration(StringBuilder sb, CodeClass cc) {
		IRelationalElement element = cc.getData(IRelationalElement.class);
		if (element instanceof RelationalTable) {
			sb.append("@Entity(name=\"");
			sb.append(cc.getName());
			sb.append("\")\n");
			sb.append("@Table(name=\"");
			sb.append(((RelationalTable) element).getTableName());
			sb.append("\")\n");
		}
		super.appendClassDeclaration(sb, cc);
	}

	@Override
	protected void appendFieldDeclaration(StringBuilder sb, CodeField field) {
		IRelationalElement element = field.getData(IRelationalElement.class);
		if (element instanceof RelationalLink) {
			sb.append("  @ManyToOne()\n");
			sb.append("  @JoinColumns(value=\n");
			Set<Entry<String, String>> entrySet = ((RelationalLink) element).getColumnMap().entrySet();
			Iterator<Entry<String, String>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, String> entry = iterator.next();
				sb.append("        ");
				sb.append("@JoinColumn(name=\"");
				sb.append(entry.getKey());
				sb.append("\",referencedColumnName=\"");
				sb.append(entry.getValue());
				sb.append("\")");
				if(iterator.hasNext()){
					sb.append(",");
					sb.append("\n");
				}
			}
			sb.append(")\n");
		} else if (element instanceof RelationalInverseLink) {
			sb.append("  @OneToMany(mappedBy=\"");
			sb.append(((RelationalInverseLink) element).getLinkProperty());
			sb.append("\")\n");
		} else if (element instanceof RelationalColumn) {
			JpaDataTypeStrategy jpaDataTypeStrategy = dataTypeStrategies.get(field.getType());
			if (jpaDataTypeStrategy != null) {
				jpaDataTypeStrategy.beforeField("  ", sb, field, (RelationalColumn) element);
			}

		}
		super.appendFieldDeclaration(sb, field);
	}
}
