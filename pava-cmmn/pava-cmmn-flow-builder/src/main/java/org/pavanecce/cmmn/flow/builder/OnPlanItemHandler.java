package org.pavanecce.cmmn.flow.builder;

import java.util.HashSet;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.flow.OnCaseFileItemPart;
import org.pavanecce.cmmn.flow.OnPlanItemPart;
import org.pavanecce.cmmn.flow.PlanItem;
import org.pavanecce.cmmn.flow.PlanItemTransition;
import org.pavanecce.cmmn.flow.Sentry;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class OnPlanItemHandler extends BaseAbstractHandler implements Handler {
	public OnPlanItemHandler() {
		super.validParents=new HashSet<Class<?>>();
		validParents.add(Sentry.class);
		super.validPeers=new HashSet<Class<?>>();
		validPeers.add(OnPlanItemPart.class);
		validPeers.add(OnCaseFileItemPart.class);
		validPeers.add(null);

	}
	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		xmlPackageReader.startElementBuilder(localName, attrs);
		OnPlanItemPart part = new OnPlanItemPart();
		part.setSourceRef(attrs.getValue("sourceRef"));
		((Sentry)xmlPackageReader.getParent()).addOnPart(part);
		return part;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		OnPlanItemPart part =(OnPlanItemPart) xmlPackageReader.getCurrent();
		NodeList elementsByTagName = xmlPackageReader.endElementBuilder().getElementsByTagName("standardEvent");
		part.setStandardEvent(PlanItemTransition.resolveByName(elementsByTagName.item(0).getTextContent()));
		return xmlPackageReader.getCurrent();
	}

	@Override
	public Class<?> generateNodeFor() {
		return OnPlanItemPart.class;
	}

}
