package org.pavanecce.cmmn.jbpm.xml.handler;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseTask;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CaseTaskHandler extends AbstractCaseElementHandler implements Handler {
	public CaseTaskHandler() {

	}

	@Override
	public Class<?> generateNodeFor() {
		return CaseTask.class;
	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		CaseTask node = new CaseTask();
		node.setElementId(attrs.getValue("id"));
		node.setBlocking(!"false".equals(attrs.getValue("isBlocking")));
		node.setName(attrs.getValue("name"));
		String caseRef = attrs.getValue("caseRef");
		if (caseRef != null) {
			String[] split = caseRef.split("\\#");
			if (split.length == 1) {
				node.setProcessId(split[0]);
			} else {
				node.setProcessId(split[1]);
			}
		}
		((Case) parser.getParent(Case.class)).addPlanItemDefinition(node);
		return node;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();
		CaseTask node = (CaseTask) parser.getCurrent();
		node.mapParameters();
		return parser.getCurrent();
	}

}
