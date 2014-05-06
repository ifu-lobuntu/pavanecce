package org.pavanecce.cmmn.jbpm.flow.builder;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.PlanItemContainer;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInfo;
import org.pavanecce.cmmn.jbpm.flow.Stage;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PlanItemHandler extends AbstractPlanModelElementHandler implements Handler {
	public PlanItemHandler(){
		super();
		super.validParents.add(Stage.class);
	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		xmlPackageReader.startElementBuilder(localName, attrs);
		PlanItemInfo planItem = new PlanItemInfo();
		planItem.setId(IdGenerator.next(xmlPackageReader));
		planItem.setContainer(((PlanItemContainer) xmlPackageReader.getParent()));
		planItem.setName(attrs.getValue("name"));
		String entry = attrs.getValue("entryCriteriaRefs");
		if (entry != null) {
			for (String string : entry.split("\\ ")) {
				planItem.putEntryCriterion(string, null);
			}
		}
		planItem.setElementId(attrs.getValue("id"));
		planItem.setDefinitionRef(attrs.getValue("definitionRef"));
		String exit = attrs.getValue("exitCriteriaRefs");
		if (exit != null) {
			for (String string : exit.split("\\ ")) {
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
		return PlanItemInfo.class;
	}

}
