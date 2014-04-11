package org.pavanecce.cmmn.flow.builder;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.pavanecce.cmmn.flow.Role;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class RoleHandler extends BaseAbstractHandler implements Handler{

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		xmlPackageReader.startElementBuilder(localName, attrs);
		Role role = new Role();
		role.setName(attrs.getValue("name"));
		RuleFlowProcess process = (RuleFlowProcess) xmlPackageReader.getParent();
		xmlPackageReader.getParent();
		return role;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		return xmlPackageReader.getCurrent();
	}

	@Override
	public Class<?> generateNodeFor() {
		return Role.class;
	}

}
