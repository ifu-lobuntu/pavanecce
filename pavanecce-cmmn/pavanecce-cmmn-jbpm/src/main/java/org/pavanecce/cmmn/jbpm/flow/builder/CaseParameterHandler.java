package org.pavanecce.cmmn.jbpm.flow.builder;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.TaskNode;
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
				p.addInputParameter(cp);
			}else{
				p.addOutputParameter(cp);
			}
		}else if(xmlPackageReader.getParent() instanceof HumanTask){
			HumanTask ht = (HumanTask) xmlPackageReader.getParent();
			if(localName.equals("outputs")){
				ht.addInputParameter(cp);
			}else{
				ht.addOutputParameter(cp);
			}
		}
		return cp;
	}

	@Override
	public Class<?> generateNodeFor() {
		return CaseParameter.class;
	}

}
