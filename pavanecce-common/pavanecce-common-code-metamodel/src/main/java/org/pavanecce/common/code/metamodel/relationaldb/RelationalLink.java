package org.pavanecce.common.code.metamodel.relationaldb;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RelationalLink implements IRelationalElement{
	private String linkName;
	private Map<String,String> columnMap=new HashMap<String,String>();
	public RelationalLink(String linkName, LinkedHashMap<String, String> columnMap) {
		super();
		this.linkName = linkName;
		this.columnMap = columnMap;
	}
	public String getLinkName() {
		return linkName;
	}
	public Map<String, String> getColumnMap() {
		return columnMap;
	}
	
}
