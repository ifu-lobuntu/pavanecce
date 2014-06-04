package org.pavanecce.cmmn.jbpm.xml.handler;

import java.util.HashSet;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemStartTrigger;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.jbpm.flow.Sentry;
import org.pavanecce.cmmn.jbpm.flow.TimerEvent;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CaseFileItemOnPartHandler extends BaseAbstractHandler implements Handler {
	public CaseFileItemOnPartHandler() {
		super.validParents = new HashSet<Class<?>>();
		validParents.add(Sentry.class);
		validParents.add(TimerEvent.class);
		super.validParents.add(Sentry.class);
		super.validPeers = new HashSet<Class<?>>();
		validPeers.add(CaseFileItemOnPart.class);
		validPeers.add(CaseFileItemStartTrigger.class);
		validPeers.add(null);

	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		CaseFileItemOnPart part = null;
		if (localName.equals("caseFileItemStartTrigger")) {
			part = new CaseFileItemStartTrigger();
		} else {
			part = new CaseFileItemOnPart();
		}
		part.setName(attrs.getValue("id"));
		part.setId(IdGenerator.getIdAsUniqueAsUuid(parser, part));
		Object parent = parser.getParent();
		if (parent instanceof Sentry) {
			((Sentry) parent).addOnPart(part);
		} else {
			((TimerEvent) parent).setStartTrigger((CaseFileItemStartTrigger) part);
		}
		part.setSourceRef(attrs.getValue("sourceRef"));
		part.setRelationRef(attrs.getValue("relationRef"));
		return part;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		CaseFileItemOnPart part = (CaseFileItemOnPart) parser.getCurrent();
		NodeList standardEvents = parser.endElementBuilder().getElementsByTagName("standardEvent");
		part.setStandardEvent(CaseFileItemTransition.resolveByName(standardEvents.item(0).getFirstChild().getNodeValue()));
		return parser.getCurrent();
	}

	@Override
	public Class<?> generateNodeFor() {
		return CaseFileItemOnPart.class;
	}

}
