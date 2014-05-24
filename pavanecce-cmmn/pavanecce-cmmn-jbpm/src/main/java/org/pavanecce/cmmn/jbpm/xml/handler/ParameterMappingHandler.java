package org.pavanecce.cmmn.jbpm.xml.handler;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.CaseTask;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.ParameterMapping;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ParameterMappingHandler extends AbstractCaseElementHandler implements Handler {
	public ParameterMappingHandler() {
		this.validParents.add(HumanTask.class);
		this.validParents.add(CaseTask.class);
		this.validPeers.add(null);
		this.validPeers.add(CaseParameter.class);
		this.validPeers.add(ParameterMapping.class);

	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		ParameterMapping cp = new ParameterMapping();
		cp.setSourceRef(attrs.getValue("sourceRef"));
		cp.setTargetRef(attrs.getValue("targetRef"));
		cp.setElementId(attrs.getValue("id"));
		if (parser.getParent() instanceof CaseTask) {
			CaseTask ht = (CaseTask) parser.getParent();
			ht.addParameterMapping(cp);
		}
		return cp;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		Element el = parser.endElementBuilder();
		ParameterMapping pm = (ParameterMapping) parser.getCurrent();
		pm.setTransformation(ConstraintExtractor.extractExpression(el, "transformation"));
		return pm;
	}

	@Override
	public Class<?> generateNodeFor() {
		return ParameterMapping.class;
	}

}
