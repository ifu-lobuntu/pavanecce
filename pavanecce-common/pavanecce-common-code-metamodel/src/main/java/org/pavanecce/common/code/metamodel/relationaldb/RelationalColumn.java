package org.pavanecce.common.code.metamodel.relationaldb;

public class RelationalColumn implements IRelationalElement {
	private String columnName;
	private boolean isRequired;

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

}
