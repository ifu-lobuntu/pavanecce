package org.pavanecce.common.code.metamodel.relationaldb;

public class RelationalColumn implements IRelationalElement {
	private String columnName;
	private boolean isRequired;
	private boolean isEnumeration;

	public String getColumnName() {
		return columnName;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public RelationalColumn(String columnName, boolean isRequired) {
		super();
		this.columnName = columnName;
		this.isRequired = isRequired;
	}

	public RelationalColumn(String persistentName, boolean required, boolean b) {
		this(persistentName, required);
		this.isEnumeration = b;
	}

	public boolean isEnumeration() {
		return isEnumeration;
	}

}
