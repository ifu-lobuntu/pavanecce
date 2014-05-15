package org.pavanecce.cmmn.jbpm.xml.handler;

import org.drools.core.xml.ExtensibleXmlParser;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.TimerEventListener;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class TimerEventHandler extends AbstractCaseElementHandler {

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		TimerEventListener node = new TimerEventListener();
		parser.startElementBuilder(localName, attrs);
		node.setElementId(attrs.getValue("id"));
		node.setId(IdGenerator.next(parser));

		node.setName(attrs.getValue("name"));
		((Case) parser.getParent(Case.class)).addPlanItemDefinition(node);
		return node;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		Element el = parser.endElementBuilder();
		TimerEventListener l = (TimerEventListener) parser.getCurrent();
		l.setTimerExpression(ConstraintExtractor.extractExpression(el, "timerExpression"));
		return l;
	}

	@Override
	public Class<?> generateNodeFor() {
		return TimerEventListener.class;
	}

}
