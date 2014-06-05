package org.pavanecce.uml.uml2code.jpa;

import java.util.HashSet;
import java.util.Set;

import org.pavanecce.common.code.metamodel.CodeField;
import org.pavanecce.common.code.metamodel.relationaldb.RelationalColumn;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;

public enum JpaRelationalDataType implements JpaDataTypeStrategy {
	INT() {
		private Set<String> imports = new HashSet<String>();
		{
			imports.add("javax.persistence.Basic");
			imports.add("javax.persistence.Column");
		}

		@Override
		public void beforeField(String padding, JavaCodeGenerator sb, CodeField field, RelationalColumn column) {
			sb.append(padding);
			sb.append("@Basic(");
			if (column.isRequired()) {
				sb.append("optional=false");
			}
			sb.append(")\n");
			sb.append(padding);
			sb.append("@Column(name=\"");
			sb.append(column.getColumnName());
			sb.append("\")\n");
		}

		@Override
		public Set<String> getImports() {
			return imports;
		}
	},
	DATETIME() {
		private Set<String> imports = new HashSet<String>();
		{
			imports.add("javax.persistence.Temporal");
			imports.add("javax.persistence.TemporalType");
			imports.add("javax.persistence.Column");
		}

		@Override
		public void beforeField(String padding, JavaCodeGenerator sb, CodeField field, RelationalColumn col) {
			sb.append(padding);
			sb.append("@Temporal(TemporalType.TIMESTAMP)\n");
			sb.append(padding);
			sb.append("@Column(name=\"");
			sb.append(col.getColumnName());
			sb.append("\")\n");
		}

		@Override
		public Set<String> getImports() {
			return imports;
		}
	},
	DATE() {
		private Set<String> imports = new HashSet<String>();
		{
			imports.add("javax.persistence.Temporal");
			imports.add("javax.persistence.TemporalType");
			imports.add("javax.persistence.Column");
		}

		@Override
		public void beforeField(String padding, JavaCodeGenerator sb, CodeField field, RelationalColumn col) {
			sb.append(padding);
			sb.append("@Temporal(TemporalType.DATE)\n");
			sb.append(padding);
			sb.append("@Column(name=\"");
			sb.append(col.getColumnName());
			sb.append("\")\n");
		}

		@Override
		public Set<String> getImports() {
			return imports;
		}
	},
	TIME() {
		private Set<String> imports = new HashSet<String>();
		{
			imports.add("javax.persistence.Temporal");
			imports.add("javax.persistence.TemporalType");
			imports.add("javax.persistence.Column");
		}

		@Override
		public void beforeField(String padding, JavaCodeGenerator sb, CodeField field, RelationalColumn col) {
			sb.append(padding);
			sb.append("@Temporal(TemporalType.TIME)\n");
			sb.append(padding);
			sb.append("@Column(name=\"");
			sb.append(col.getColumnName());
			sb.append("\")\n");
		}

		@Override
		public Set<String> getImports() {
			return imports;
		}
	},
	STRING() {
		private Set<String> imports = new HashSet<String>();
		{
			imports.add("javax.persistence.Basic");
			imports.add("javax.persistence.Column");
		}

		@Override
		public void beforeField(String padding, JavaCodeGenerator sb, CodeField field, RelationalColumn col) {
			sb.append(padding);
			sb.append("@Basic()\n");
			sb.append(padding);
			sb.append("@Column(name=\"");
			sb.append(col.getColumnName());
			sb.append("\")\n");
		}

		@Override
		public Set<String> getImports() {
			return imports;
		}
	},
	BOOLEAN() {
		private Set<String> imports = new HashSet<String>();
		{
			imports.add("javax.persistence.Basic");
			imports.add("javax.persistence.Column");
		}

		@Override
		public void beforeField(String padding, JavaCodeGenerator sb, CodeField field, RelationalColumn col) {
			sb.append(padding);
			sb.append("@Basic()\n");
			sb.append(padding);
			sb.append("@Column(name=\"");
			sb.append(col.getColumnName());
			sb.append("\")\n");
		}

		@Override
		public Set<String> getImports() {
			return imports;
		}
	},
	DOUBLE() {
		private Set<String> imports = new HashSet<String>();
		{
			imports.add("javax.persistence.Basic");
			imports.add("javax.persistence.Column");
		}

		@Override
		public void beforeField(String padding, JavaCodeGenerator sb, CodeField field, RelationalColumn col) {
			sb.append(padding);
			sb.append("@Basic()\n");
			sb.append(padding);
			sb.append("@Column(name=\"");
			sb.append(col.getColumnName());
			sb.append("\")\n");
		}

		@Override
		public Set<String> getImports() {
			return imports;
		}
	},
	TEXT() {
		private Set<String> imports = new HashSet<String>();
		{
			imports.add("javax.persistence.Lob");
			imports.add("javax.persistence.Column");
		}

		@Override
		public void beforeField(String padding, JavaCodeGenerator sb, CodeField field, RelationalColumn col) {
			sb.append(padding);
			sb.append("@Lob()\n");
			sb.append(padding);
			sb.append("@Column(name=\"");
			sb.append(col.getColumnName());
			sb.append("\")\n");
		}

		@Override
		public Set<String> getImports() {
			return imports;
		}
	},
	BINARY() {
		private Set<String> imports = new HashSet<String>();
		{
			imports.add("javax.persistence.Basic");
			imports.add("javax.persistence.Column");
			imports.add("javax.persistence.Lob");
		}

		@Override
		public void beforeField(String padding, JavaCodeGenerator sb, CodeField field, RelationalColumn col) {
			sb.append(padding);
			sb.append("@Lob()\n");
			sb.append(padding);
			sb.append("@Column(name=\"");
			sb.append(col.getColumnName());
			sb.append("\")\n");
		}

		@Override
		public Set<String> getImports() {
			return imports;
		}
	}

}
