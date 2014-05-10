package org.pavanecce.cmmn.jbpm.flow.builder;

import org.drools.core.xml.ExtensibleXmlParser;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.TimerEventListener;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class TimerEventHandler extends AbstractCaseElementHandler {

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		TimerEventListener node = new TimerEventListener();
		xmlPackageReader.startElementBuilder(localName, attrs);
		node.setElementId(attrs.getValue("id"));
		node.setId(IdGenerator.next(xmlPackageReader));

		node.setName(attrs.getValue("name"));
		((Case) xmlPackageReader.getParent(Case.class)).addPlanItemDefinition(node);
		return node;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		Element el = xmlPackageReader.endElementBuilder();
		TimerEventListener l = (TimerEventListener) xmlPackageReader.getCurrent();
		l.setTimerExpression(ConstraintExtractor.extractExpression(el, "timerExpression"));
		return super.end(uri, localName, xmlPackageReader);
	}

	@Override
	public Class<?> generateNodeFor() {
		return TimerEventListener.class;
	}

}
