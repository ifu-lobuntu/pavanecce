package org.pavanecce.cmmn.jbpm.xml.handler;

import org.drools.core.xml.ExtensibleXmlParser;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.TimerEvent;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class TimerEventHandler extends AbstractCaseElementHandler {

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		TimerEvent node = new TimerEvent();
		parser.startElementBuilder(localName, attrs);
		node.setElementId(attrs.getValue("id"));
		Case theCase = (Case) parser.getParent(Case.class);
		node.setId(IdGenerator.getIdAsUniqueAsUuid(parser, node));
		node.setName(attrs.getValue("name"));
		theCase.addPlanItemDefinition(node);
		return node;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		Element el = parser.endElementBuilder();
		TimerEvent l = (TimerEvent) parser.getCurrent();
		l.setTimerExpression(ConstraintExtractor.extractExpression(el, "timerExpression"));
		return l;
	}

	@Override
	public Class<?> generateNodeFor() {
		return TimerEvent.class;
	}

}
