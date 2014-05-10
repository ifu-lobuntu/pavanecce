package org.pavanecce.cmmn.jbpm.flow.builder;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.TaskNode;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CaseParameterHandler extends AbstractCaseElementHandler implements Handler{
	public CaseParameterHandler() {
		this.validParents.add(TaskNode.class);
		this.validPeers.add(null);
	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		xmlPackageReader.startElementBuilder(localName, attrs);
		CaseParameter cp = new CaseParameter();
		cp.setBindingRef(attrs.getValue("bindingRef"));
		cp.setElementId(attrs.getValue("id"));
		cp.setName(attrs.getValue("name"));
		if(xmlPackageReader.getParent() instanceof Case){
			Case p = (Case) xmlPackageReader.getParent();
			if(localName.equals("output")){
				p.addOutputParameter(cp);
			}else{
				p.addInputParameter(cp);
			}
		}else if(xmlPackageReader.getParent() instanceof HumanTask){
			HumanTask ht = (HumanTask) xmlPackageReader.getParent();
			if(localName.equals("outputs")){
				ht.addOutputParameter(cp);
			}else{
				ht.addInputParameter(cp);
			}
		}
		return cp;
	}
	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser xmlPackageReader) throws SAXException {
		Element el = xmlPackageReader.endElementBuilder();
		CaseParameter caseParameter = (CaseParameter) xmlPackageReader.getCurrent();
		caseParameter.setBindingRefinement(ConstraintExtractor.extractExpression(el, "bindingRefinement"));
		return caseParameter;
	}
	

	@Override
	public Class<?> generateNodeFor() {
		return CaseParameter.class;
	}

}
