package org.pavanecce.cmmn.jbpm.flow.builder;

import java.util.Arrays;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class BindingRefinementHandler extends BaseAbstractHandler implements Handler {
	@Override
	public Class<?> generateNodeFor() {
		return DataAssociation.class;
	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		xmlPackageReader.startElementBuilder(localName, attrs);
		CaseParameter parent = (CaseParameter) xmlPackageReader.getParent();
		Object grandParent = xmlPackageReader.getParents().get(xmlPackageReader.getParents().size() - 2);
		DataAssociation dataAssociation = new DataAssociation((String) null, parent.getName(), null, null);
		if (grandParent instanceof WorkItemNode) {
			WorkItemNode task = (WorkItemNode) grandParent;
			task.addInAssociation(dataAssociation);
		}
		return dataAssociation;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		Element endElementBuilder = xmlPackageReader.endElementBuilder();
		NodeList bodies = endElementBuilder.getElementsByTagName("body");
		DataAssociation dataAssociation = (DataAssociation) xmlPackageReader.getCurrent();
		if (bodies.getLength() > 0) {
			Element bodyElement = (Element) bodies.item(0);
			String body = bodyElement.getFirstChild().getNodeValue();
			dataAssociation.setSources(Arrays.asList(body));
		}
		return dataAssociation;
	}
}
