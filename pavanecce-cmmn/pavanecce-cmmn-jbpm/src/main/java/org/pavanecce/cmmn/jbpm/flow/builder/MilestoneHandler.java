package org.pavanecce.cmmn.jbpm.flow.builder;

import org.drools.core.xml.ExtensibleXmlParser;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.Milestone;
import org.pavanecce.cmmn.jbpm.flow.UserEventListener;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class MilestoneHandler extends AbstractCaseElementHandler {

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		Milestone node = new Milestone();
		node.setId(IdGenerator.next(xmlPackageReader));
		xmlPackageReader.startElementBuilder(localName, attrs);
		node.setElementId(attrs.getValue("id"));
		node.setName(attrs.getValue("name"));
		((Case) xmlPackageReader.getParent(Case.class)).addPlanItemDefinition(node);
		return node;
	}

	@Override
	public Class<?> generateNodeFor() {
		return Milestone.class;
	}

}
