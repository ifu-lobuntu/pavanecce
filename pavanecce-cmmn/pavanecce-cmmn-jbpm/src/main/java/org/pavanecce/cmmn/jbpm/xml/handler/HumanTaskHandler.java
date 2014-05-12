package org.pavanecce.cmmn.jbpm.xml.handler;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HumanTaskHandler extends AbstractPlanModelElementHandler implements Handler {
	public HumanTaskHandler() {

	}

	@Override
	public Class<?> generateNodeFor() {
		return HumanTask.class;
	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		xmlPackageReader.startElementBuilder(localName, attrs);
		HumanTask node = new HumanTask();
		node.setElementId(attrs.getValue("id"));
		node.setBlocking(!"false".equals(attrs.getValue("isBlocking")));
		node.setPerformerRef(attrs.getValue("performerRef"));
		node.setName(attrs.getValue("name"));
		node.setWaitForCompletion(node.isBlocking());
		((Case) xmlPackageReader.getParent(Case.class)).addPlanItemDefinition(node);
		return node;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		return xmlPackageReader.getCurrent();
	}

}
