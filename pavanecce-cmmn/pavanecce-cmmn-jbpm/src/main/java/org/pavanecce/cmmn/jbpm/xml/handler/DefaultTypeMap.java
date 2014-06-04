package org.pavanecce.cmmn.jbpm.xml.handler;

import java.util.HashMap;
import java.util.Map;

public class DefaultTypeMap implements TypeMap {
	Map<String, String> types = new HashMap<String, String>();

	@Override
	public String getType(String sourceType) {
		String[] split = sourceType.split("\\#");
		String result = split[split.length - 1];
		return result.replace("/", ".").replace("::", ".");
	}

	public void putType(String s, String t) {
		types.put(s, t);
	}

}
