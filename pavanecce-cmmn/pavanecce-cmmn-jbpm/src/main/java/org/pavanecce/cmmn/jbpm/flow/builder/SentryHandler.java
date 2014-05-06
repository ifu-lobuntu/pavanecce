package org.pavanecce.cmmn.jbpm.flow.builder;

import java.util.Collection;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.ConstraintImpl;
import org.pavanecce.cmmn.jbpm.flow.JoiningSentry;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.cmmn.jbpm.flow.Sentry;
import org.pavanecce.cmmn.jbpm.flow.SimpleSentry;
import org.pavanecce.cmmn.jbpm.flow.Stage;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SentryHandler extends AbstractPlanModelElementHandler implements Handler {
	private static final String DEFAULT = Node.CONNECTION_DEFAULT_TYPE;

	public SentryHandler() {
		super.validPeers.add(SimpleSentry.class);
		super.validPeers.add(JoiningSentry.class);
		super.validParents.add(Stage.class);
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
		Element el = xmlPackageReader.endElementBuilder();
		if (onParts.size() > 1) {
			JoiningSentry js = new JoiningSentry();
			js.setElementId(node.getElementId());
			js.setName(node.getName());
			for (OnPart onPart : onParts) {
				js.addOnPart(onPart);
			}
			node = js;
		}
		ConstraintImpl constraint = maybeCreateConstraint(node, el);
		node.setCondition(constraint);
		node.setId(IdGenerator.next(xmlPackageReader));
		NodeContainer parent = (NodeContainer) xmlPackageReader.getParent();
		parent.addNode(node);
		for (OnPart onPart : onParts) {
			new ConnectionImpl(onPart, DEFAULT, node, DEFAULT);
			parent.addNode(onPart);
		}
		return node;
	}

	protected ConstraintImpl maybeCreateConstraint(Sentry node, Element el) {
		NodeList ifPartList = el.getElementsByTagName("ifPart");
		ConstraintImpl result=null;
		if (ifPartList.getLength() == 1) {
			Element ifPart = (Element) ifPartList.item(0);
			String name = "condition";
			result=extractExpression(ifPart, name);
		}
		return result;
	}

	public static ConstraintImpl extractExpression(Element parentElement, String epxressionElementName) {
		NodeList conditionList = parentElement.getElementsByTagName(epxressionElementName);
		ConstraintImpl constraint = null;
		if (conditionList.getLength() == 1) {
			Element condition = (Element) conditionList.item(0);
			String dialect = getDialect(condition);
			String bodyText = "";
			NodeList bodyList = condition.getElementsByTagName("body");
			if (bodyList.getLength() == 1) {
				Element body = (Element) bodyList.item(0);
				bodyText = body.getFirstChild().getNodeValue();
				constraint = new ConstraintImpl();
				constraint.setConstraint(bodyText);
				constraint.setDialect(dialect);
			}
		}
		return constraint;
	}

	protected static String getDialect(Element condition) {
		String dialect = null;
		String language = condition.getAttribute("language");
		if (language == null) {
			language = "mvel";
		} else {
			language = language.toLowerCase();
		}
		if (language.endsWith("java")) {
			dialect = "java";
		} else if (language.endsWith("mvel")) {
			dialect = "mvel";
		} else if (language.endsWith("ocl")) {
			dialect = "ocl";
		} else if (language.endsWith("xpath")) {
			dialect = "xpath";
		} else {
			dialect = "mvel";
		}
		return dialect;
	}

}
