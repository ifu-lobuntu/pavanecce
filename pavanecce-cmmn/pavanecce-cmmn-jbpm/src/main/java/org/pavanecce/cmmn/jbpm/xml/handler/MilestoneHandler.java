package org.pavanecce.cmmn.jbpm.xml.handler;

import org.drools.core.xml.ExtensibleXmlParser;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.Milestone;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class MilestoneHandler extends AbstractCaseElementHandler {

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		Milestone node = new Milestone();
		node.setElementId(attrs.getValue("id"));
		node.setName(attrs.getValue("name"));
		node.setId(IdGenerator.getIdAsUniqueAsUuid(parser, node));
		((Case) parser.getParent(Case.class)).addPlanItemDefinition(node);
		return node;
	}

	@Override
	public Class<?> generateNodeFor() {
		return Milestone.class;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();
		return parser.getCurrent();
	}

}
