package org.pavanecce.cmmn.flow.builder;

import java.util.HashSet;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.flow.CaseFileItemTransition;
import org.pavanecce.cmmn.flow.OnCaseFileItemPart;
import org.pavanecce.cmmn.flow.OnCaseFileItemPart;
import org.pavanecce.cmmn.flow.PlanItem;
import org.pavanecce.cmmn.flow.PlanItemTransition;
import org.pavanecce.cmmn.flow.Sentry;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class OnCaseFileItemHandler extends BaseAbstractHandler implements Handler {
	public OnCaseFileItemHandler() {
		super.validParents=new HashSet<Class<?>>();
		validParents.add(Sentry.class);
		super.validPeers=new HashSet<Class<?>>();
		validPeers.add(OnCaseFileItemPart.class);
		validPeers.add(OnCaseFileItemPart.class);
		validPeers.add(null);

	}
	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		xmlPackageReader.startElementBuilder(localName, attrs);
		OnCaseFileItemPart part = new OnCaseFileItemPart();
		((Sentry)xmlPackageReader.getParent()).addOnPart(part);
		part.setSourceRef(attrs.getValue("sourceRef"));
		return part;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		OnCaseFileItemPart part =(OnCaseFileItemPart) xmlPackageReader.getCurrent();
		NodeList elementsByTagName = xmlPackageReader.endElementBuilder().getElementsByTagName("standardEvent");
		part.setStandardEvent(CaseFileItemTransition.resolveByName(elementsByTagName.item(0).getTextContent()));
		return xmlPackageReader.getCurrent();
	}

	@Override
	public Class<?> generateNodeFor() {
		return OnCaseFileItemPart.class;
	}

}
