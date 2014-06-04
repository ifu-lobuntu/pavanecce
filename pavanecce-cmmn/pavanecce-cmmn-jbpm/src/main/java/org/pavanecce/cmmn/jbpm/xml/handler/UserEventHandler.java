package org.pavanecce.cmmn.jbpm.xml.handler;

import org.drools.core.xml.ExtensibleXmlParser;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.UserEvent;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class UserEventHandler extends AbstractCaseElementHandler {

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		UserEvent node = new UserEvent();
		parser.startElementBuilder(localName, attrs);

		node.setElementId(attrs.getValue("id"));
		node.setName(attrs.getValue("name"));
		node.setEventName(attrs.getValue("name"));
		node.setId(IdGenerator.getIdAsUniqueAsUuid(parser, node));
		((Case) parser.getParent(Case.class)).addPlanItemDefinition(node);
		return node;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();
		return parser.getCurrent();
	}

	@Override
	public Class<?> generateNodeFor() {
		return UserEvent.class;
	}

}
