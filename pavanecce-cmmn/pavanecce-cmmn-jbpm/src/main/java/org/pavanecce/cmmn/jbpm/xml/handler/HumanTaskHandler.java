package org.pavanecce.cmmn.jbpm.xml.handler;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HumanTaskHandler extends AbstractCaseElementHandler implements Handler {
	public HumanTaskHandler() {

	}

	@Override
	public Class<?> generateNodeFor() {
		return HumanTask.class;
	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		HumanTask node = new HumanTask();
		node.setElementId(attrs.getValue("id"));
		node.setBlocking(!"false".equals(attrs.getValue("isBlocking")));
		node.setPerformerRef(attrs.getValue("performerRef"));
		node.setName(attrs.getValue("name"));
		((Case) parser.getParent(Case.class)).addPlanItemDefinition(node);
		node.setId(IdGenerator.getIdAsUniqueAsUuid(parser, node));
		return node;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();
		return parser.getCurrent();
	}

}
