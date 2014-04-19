package org.pavanecce.cmmn.flow.builder;

import org.drools.core.xml.ExtensibleXmlParser;

public class IdGenerator {
	@SuppressWarnings("unchecked")
	public static long next(ExtensibleXmlParser p) {
		Long object = (Long) p.getMetaData().get("HighestId");
		if(object==null){
			object=20l;
		}
		object++;
		p.getMetaData().put("HighestId", object);
		return object;
		
	}
}
