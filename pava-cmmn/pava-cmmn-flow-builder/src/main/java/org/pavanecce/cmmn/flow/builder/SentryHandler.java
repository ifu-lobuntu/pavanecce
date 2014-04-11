package org.pavanecce.cmmn.flow.builder;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.workflow.core.NodeContainer;
import org.pavanecce.cmmn.flow.Sentry;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SentryHandler extends AbstractPlanModelElementHandler implements Handler {
	public SentryHandler() {
	}

	public Object start(final String uri, final String localName, final Attributes attrs, final ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);

		NodeContainer nodeContainer = (NodeContainer) parser.getParent();

		final Sentry node = new Sentry();

		node.setId(IdGenerator.next(parser));
		node.setElementId(attrs.getValue("id"));

		final String name = attrs.getValue("name");
		node.setName(name);

		nodeContainer.addNode(node);

		return node;
	}

	@SuppressWarnings("unchecked")
	public Class generateNodeFor() {
		return Sentry.class;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		return xmlPackageReader.getCurrent();
	}

}
