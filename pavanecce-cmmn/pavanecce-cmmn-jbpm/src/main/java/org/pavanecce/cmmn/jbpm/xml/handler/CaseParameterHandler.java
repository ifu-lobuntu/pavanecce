package org.pavanecce.cmmn.jbpm.xml.handler;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.BindingRefinement;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.CaseTask;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.ParameterMapping;
import org.pavanecce.cmmn.jbpm.flow.PlanItemControl;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CaseParameterHandler extends AbstractCaseElementHandler implements Handler {
	public CaseParameterHandler() {
		this.validParents.add(HumanTask.class);
		this.validParents.add(CaseTask.class);
		this.validPeers.add(null);
		this.validPeers.add(CaseParameter.class);
		this.validPeers.add(ParameterMapping.class);
		this.validPeers.add(PlanItemControl.class);
	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		CaseParameter cp = new CaseParameter();
		cp.setBindingRef(attrs.getValue("bindingRef"));
		cp.setElementId(attrs.getValue("id"));
		cp.setName(attrs.getValue("name"));
		if (parser.getParent() instanceof Case) {
			Case p = (Case) parser.getParent();
			if (localName.equals("output")) {
				p.addOutputParameter(cp);
			} else {
				p.addInputParameter(cp);
			}
		} else if (parser.getParent() instanceof TaskDefinition) {
			TaskDefinition ht = (TaskDefinition) parser.getParent();
			if (localName.equals("outputs")) {
				ht.addOutputParameter(cp);
			} else {
				ht.addInputParameter(cp);
			}
		}
		return cp;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		Element el = parser.endElementBuilder();
		CaseParameter caseParameter = (CaseParameter) parser.getCurrent();
		caseParameter.setBindingRefinement(new BindingRefinement(ConstraintExtractor.extractExpression(el, "bindingRefinement")));
		return caseParameter;
	}

	@Override
	public Class<?> generateNodeFor() {
		return CaseParameter.class;
	}

}
