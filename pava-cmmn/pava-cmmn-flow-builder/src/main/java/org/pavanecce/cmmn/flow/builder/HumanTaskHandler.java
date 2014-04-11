package org.pavanecce.cmmn.flow.builder;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.flow.HumanTaskNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HumanTaskHandler extends AbstractPlanModelElementHandler implements Handler {
	public HumanTaskHandler() {

	}

	@Override
	public Class<?> generateNodeFor() {
		return HumanTaskNode.class;
	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		xmlPackageReader.startElementBuilder(localName, attrs);
		HumanTaskNode node = new HumanTaskNode();
		node.setElementId(attrs.getValue("id"));
		node.setBlocking(!"false".equals(attrs.getValue("isBlocking")));
		node.setPerformerRef(attrs.getValue("performerRef"));
		node.getWork().setName(attrs.getValue("name"));
		return node;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		return xmlPackageReader.getCurrent();
	}

}
