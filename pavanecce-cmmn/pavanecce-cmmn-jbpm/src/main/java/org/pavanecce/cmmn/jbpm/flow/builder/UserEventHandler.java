package org.pavanecce.cmmn.jbpm.flow.builder;

import org.drools.core.xml.ExtensibleXmlParser;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.UserEventListener;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class UserEventHandler extends AbstractCaseElementHandler {

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		UserEventListener node = new UserEventListener();
		xmlPackageReader.startElementBuilder(localName, attrs);
		node.setId(IdGenerator.next(xmlPackageReader));

		node.setElementId(attrs.getValue("id"));
		node.setName(attrs.getValue("name"));
		node.setEventName(attrs.getValue("name"));
		((Case) xmlPackageReader.getParent(Case.class)).addPlanItemDefinition(node);
		return node;
	}

	@Override
	public Class<?> generateNodeFor() {
		return UserEventListener.class;
	}

}
