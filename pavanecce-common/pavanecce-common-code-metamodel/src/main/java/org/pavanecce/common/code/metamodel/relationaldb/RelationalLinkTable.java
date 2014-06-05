package org.pavanecce.common.code.metamodel.relationaldb;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RelationalLinkTable implements IRelationalElement {
	private String tableName;
	private Map<String, String> fromColumnMap = new HashMap<String, String>();
	private Map<String, String> toColumnMap = new HashMap<String, String>();

	public RelationalLinkTable(String linkName, LinkedHashMap<String, String> fromColumnMap, LinkedHashMap<String, String> toColumnMap) {
		super();
		this.tableName = linkName;
		this.fromColumnMap = fromColumnMap;
		this.toColumnMap = toColumnMap;
	}

	public String getTableName() {
		return tableName;
	}

	public Map<String, String> getFromColumnMap() {
		return fromColumnMap;
	}

	public Map<String, String> getToColumnMap() {
		return toColumnMap;
	}

}
