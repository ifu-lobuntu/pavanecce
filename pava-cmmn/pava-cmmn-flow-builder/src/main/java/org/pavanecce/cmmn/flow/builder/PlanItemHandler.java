package org.pavanecce.cmmn.flow.builder;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.workflow.core.NodeContainer;
import org.pavanecce.cmmn.flow.PlanItem;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PlanItemHandler extends AbstractPlanModelElementHandler implements Handler {

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		xmlPackageReader.startElementBuilder(localName, attrs);
		PlanItem planItem = new PlanItem();
		planItem.setId(IdGenerator.next(xmlPackageReader));
		((NodeContainer)xmlPackageReader.getParent()).addNode(planItem);
		planItem.setName(attrs.getValue("name"));
		String entry = attrs.getValue("entryCriteriaRefs");
		if(entry!=null){
			for (String string : entry.split("\\ ")) {
				planItem.putEntryCriterion(string, null);
			}
		}
		planItem.setElementId(attrs.getValue("id"));
		planItem.setDefinitionRef(attrs.getValue("definitionRef"));
		String exit = attrs.getValue("exitCriteriaRefs");
		if(exit!=null){
			for (String string : entry.split("\\ ")) {
				planItem.putExitCriterion(string, null);
			}
		}
		return planItem;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		return xmlPackageReader.getCurrent();
	}

	@Override
	public Class<?> generateNodeFor() {
		return PlanItem.class;
	}

}
