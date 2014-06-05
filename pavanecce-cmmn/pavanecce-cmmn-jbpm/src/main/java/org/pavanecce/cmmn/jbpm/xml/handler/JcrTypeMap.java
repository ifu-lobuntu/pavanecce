package org.pavanecce.cmmn.jbpm.xml.handler;

public class JcrTypeMap implements TypeMap {

	@Override
	public String getType(String sourceType) {
		return "javax.jcr.Node";
	}
}
