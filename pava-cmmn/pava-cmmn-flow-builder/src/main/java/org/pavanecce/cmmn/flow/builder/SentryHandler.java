package org.pavanecce.cmmn.flow.builder;

import java.util.Collection;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.pavanecce.cmmn.flow.JoiningSentry;
import org.pavanecce.cmmn.flow.OnPart;
import org.pavanecce.cmmn.flow.Sentry;
import org.pavanecce.cmmn.flow.SimpleSentry;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SentryHandler extends AbstractPlanModelElementHandler implements Handler {
	private static final String DEFAULT = Node.CONNECTION_DEFAULT_TYPE;

	public SentryHandler() {
		super.validPeers.add(SimpleSentry.class);
		super.validPeers.add(JoiningSentry.class);
	}

	public Object start(final String uri, final String localName, final Attributes attrs, final ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		SimpleSentry node = new SimpleSentry();
		node.setElementId(attrs.getValue("id"));
		node.setName(attrs.getValue("name"));
		return node;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class generateNodeFor() {
		return Sentry.class;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		Sentry node = (Sentry) xmlPackageReader.getCurrent();
		Collection<? extends OnPart> onParts = node.getOnParts();
		if(onParts.size()>1){
			JoiningSentry js=new JoiningSentry();
			js.setElementId(node.getElementId());
			js.setName(node.getName());
			for (OnPart onPart : onParts) {
				js.addOnPart(onPart);
			}
			node=js;
		}
		node.setId(IdGenerator.next(xmlPackageReader));
		NodeContainer parent = (NodeContainer)xmlPackageReader.getParent();
		parent.addNode(node);
		for (OnPart onPart : onParts) {
			new ConnectionImpl(onPart, DEFAULT, node, DEFAULT);
			parent.addNode(onPart);
		}
		return node;
	}

}
