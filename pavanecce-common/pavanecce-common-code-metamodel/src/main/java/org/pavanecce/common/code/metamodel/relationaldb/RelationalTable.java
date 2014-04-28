package org.pavanecce.common.code.metamodel.relationaldb;

import java.util.List;
import java.util.Map;

public class RelationalTable implements IRelationalElement {
	private String tableName;
	private Map<String, List<String>> indices;
	private String primaryKeyField;

	public RelationalTable(String tableName, String primaryKeyField, Map<String, List<String>> indices) {
		super();
		this.tableName = tableName;
		this.primaryKeyField = primaryKeyField;
		this.indices = indices;
	}

	public String getTableName() {
		return tableName;
	}

	public Map<String, List<String>> getIndices() {
		return indices;
	}

	public String getPrimaryKeyField() {
		return primaryKeyField;
	}

}
