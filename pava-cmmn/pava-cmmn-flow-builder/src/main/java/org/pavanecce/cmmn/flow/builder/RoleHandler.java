package org.pavanecce.cmmn.flow.builder;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.flow.Case;
import org.pavanecce.cmmn.flow.Role;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class RoleHandler extends AbstractCaseElementHandler implements Handler{

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		xmlPackageReader.startElementBuilder(localName, attrs);
		Role role = new Role();
		role.setElementId(attrs.getValue("id"));
		role.setName(attrs.getValue("name"));
		Case process = (Case) xmlPackageReader.getParent();
		process.addRole(role);
		xmlPackageReader.getParent();
		return role;
	}


	@Override
	public Class<?> generateNodeFor() {
		return Role.class;
	}

}
