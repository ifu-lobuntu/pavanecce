package org.pavanecce.cmmn.jbpm.xml.handler;

import java.util.HashSet;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.pavanecce.cmmn.jbpm.flow.ApplicabilityRule;
import org.pavanecce.cmmn.jbpm.flow.DiscretionaryItem;
import org.pavanecce.cmmn.jbpm.flow.PlanningTable;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ApplicabilityRuleHandler extends AbstractTableItemHandler implements Handler {
	public ApplicabilityRuleHandler() {
		super();
		validParents = new HashSet<Class<?>>();
		validPeers = new HashSet<Class<?>>();
		super.validParents.add(null);
		super.validParents.add(PlanningTable.class);
		super.validPeers.add(null);
		super.validPeers.add(PlanningTable.class);
		super.validPeers.add(DiscretionaryItem.class);
		super.validPeers.add(ApplicabilityRule.class);
	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		ApplicabilityRule rule = new ApplicabilityRule();
		rule.setElementId(attrs.getValue("id"));
		rule.setContextRef(attrs.getValue("contextRef"));
		PlanningTable parent = (PlanningTable) parser.getParent();
		parent.addOwnedApplicabilityRule(rule);
		return rule;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		Element el = parser.endElementBuilder();
		ApplicabilityRule rule = (ApplicabilityRule) parser.getCurrent();
		rule.setCondition(ConstraintExtractor.extractExpression(el, "condition"));
		return rule;
	}

	@Override
	public Class<?> generateNodeFor() {
		return ApplicabilityRule.class;
	}

}
