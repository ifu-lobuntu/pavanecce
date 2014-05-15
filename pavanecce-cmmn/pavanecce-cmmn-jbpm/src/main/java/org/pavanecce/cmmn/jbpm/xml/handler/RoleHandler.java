package org.pavanecce.cmmn.jbpm.xml.handler;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.Role;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class RoleHandler extends AbstractCaseElementHandler implements Handler {

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		Role role = new Role();
		role.setElementId(attrs.getValue("id"));
		role.setName(attrs.getValue("name"));
		Case process = (Case) parser.getParent();
		process.addRole(role);
		parser.getParent();
		return role;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();
		return parser.getCurrent();
	}

	@Override
	public Class<?> generateNodeFor() {
		return Role.class;
	}

}
