package org.pavanecce.cmmn.jbpm.xml.handler;

import java.util.HashSet;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.flow.Sentry;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PlanItemOnPartHandler extends BaseAbstractHandler implements Handler {
	public PlanItemOnPartHandler() {
		super.validParents=new HashSet<Class<?>>();
		validParents.add(Sentry.class);
		super.validParents.add(Sentry.class);
		super.validPeers=new HashSet<Class<?>>();
		validPeers.add(PlanItemOnPart.class);
		validPeers.add(CaseFileItemOnPart.class);
		validPeers.add(null);

	}
	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		xmlPackageReader.startElementBuilder(localName, attrs);
		PlanItemOnPart part = new PlanItemOnPart();
		part.setId(IdGenerator.next(xmlPackageReader));
		part.setName(attrs.getValue("id"));
		part.setSourceRef(attrs.getValue("sourceRef"));
		((Sentry)xmlPackageReader.getParent()).addOnPart(part);
		return part;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		PlanItemOnPart part =(PlanItemOnPart) xmlPackageReader.getCurrent();
		NodeList elementsByTagName = xmlPackageReader.endElementBuilder().getElementsByTagName("standardEvent");
		part.setStandardEvent(PlanItemTransition.resolveByName(elementsByTagName.item(0).getFirstChild().getNodeValue()));
		return xmlPackageReader.getCurrent();
	}

	@Override
	public Class<?> generateNodeFor() {
		return PlanItemOnPart.class;
	}

}
