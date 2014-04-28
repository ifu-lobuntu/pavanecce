package org.pavanecce.common.code.metamodel;

import java.util.HashMap;
import java.util.Map;

public class CodeMappedType {
	private Map<String, String> qualifiedNames = new HashMap<String, String>();
	private String persistentName;

	public CodeMappedType(String qualifiedJavaName) {
		super();
		this.qualifiedNames.put("java", qualifiedJavaName);
	}

	public CodeMappedType(Map<String, String> mappings) {
		this.qualifiedNames=mappings;
	}

	public String getQualifiedJavaName() {
		return qualifiedNames.get("java");
	}

	public void setQualifiedJavaName(String qualifiedJavaName) {
		qualifiedNames.put("java", qualifiedJavaName);
	}

	public String getPersistentName() {
		return persistentName;
	}

	public Map<String, String> getMappings() {
		return qualifiedNames;
	}

}
