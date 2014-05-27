package org.pavanecce.cmmn.jbpm.xml.handler;

import java.util.HashSet;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemStartTrigger;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.flow.Sentry;
import org.pavanecce.cmmn.jbpm.flow.TimerEvent;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PlanItemOnPartHandler extends BaseAbstractHandler implements Handler {
	public PlanItemOnPartHandler() {
		super.validParents = new HashSet<Class<?>>();
		validParents.add(Sentry.class);
		validParents.add(TimerEvent.class);
		super.validParents.add(Sentry.class);
		super.validPeers = new HashSet<Class<?>>();
		validPeers.add(PlanItemOnPart.class);
		validPeers.add(CaseFileItemOnPart.class);
		validPeers.add(null);

	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		PlanItemOnPart part = localName.equals("planItemOnPart") ? new PlanItemOnPart() : new PlanItemStartTrigger();
		part.setName(attrs.getValue("id"));
		part.setId(IdGenerator.getIdAsUniqueAsUuid(parser, part));
		part.setSourceRef(attrs.getValue("sourceRef"));

		Object parent = parser.getParent();
		if (parent instanceof Sentry) {
			((Sentry) parent).addOnPart(part);
		} else {
			((TimerEvent) parent).setStartTrigger(part);
		}
		return part;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		PlanItemOnPart part = (PlanItemOnPart) parser.getCurrent();
		NodeList elementsByTagName = parser.endElementBuilder().getElementsByTagName("standardEvent");
		part.setStandardEvent(PlanItemTransition.resolveByName(elementsByTagName.item(0).getFirstChild().getNodeValue()));
		return parser.getCurrent();
	}

	@Override
	public Class<?> generateNodeFor() {
		return PlanItemOnPart.class;
	}

}
