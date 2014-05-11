package org.pavanecce.cmmn.jbpm.flow.builder;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseTask;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CaseTaskHandler extends AbstractPlanModelElementHandler implements Handler {
	public CaseTaskHandler() {

	}

	@Override
	public Class<?> generateNodeFor() {
		return CaseTask.class;
	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		xmlPackageReader.startElementBuilder(localName, attrs);
		CaseTask node = new CaseTask();
		node.setElementId(attrs.getValue("id"));
		node.setBlocking(!"false".equals(attrs.getValue("isBlocking")));
		node.setName(attrs.getValue("name"));
		node.setWaitForCompletion(node.isBlocking());
		String caseRef = attrs.getValue("caseRef");
		if (caseRef != null) {
			String[] split = caseRef.split("\\#");
			if (split.length == 1) {
				node.setProcessId(split[0]);
			} else {
				node.setProcessId(split[1]);
			}
		}
		((Case) xmlPackageReader.getParent(Case.class)).addPlanItemDefinition(node);
		return node;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		CaseTask node = (CaseTask) xmlPackageReader.getCurrent();
		node.mapParameters();
		return xmlPackageReader.getCurrent();
	}

}
