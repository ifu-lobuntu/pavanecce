package org.pavanecce.cmmn.jbpm.xml.handler;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.workflow.core.impl.ConstraintImpl;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.CaseTask;
import org.pavanecce.cmmn.jbpm.flow.DiscretionaryItem;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.Milestone;
import org.pavanecce.cmmn.jbpm.flow.ParameterMapping;
import org.pavanecce.cmmn.jbpm.flow.PlanItemControl;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInfo;
import org.pavanecce.cmmn.jbpm.flow.TimerEvent;
import org.pavanecce.cmmn.jbpm.flow.UserEvent;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PlanItemControlHandler extends AbstractCaseElementHandler implements Handler {
	public PlanItemControlHandler() {
		super();
		super.validParents.add(PlanItemInfo.class);
		super.validParents.add(HumanTask.class);
		super.validParents.add(CaseTask.class);
		super.validParents.add(UserEvent.class);
		super.validParents.add(TimerEvent.class);
		super.validParents.add(Milestone.class);
		super.validParents.add(DiscretionaryItem.class);
		super.validPeers.add(null);
		this.validPeers.add(CaseParameter.class);
		this.validPeers.add(ParameterMapping.class);
		this.validPeers.add(PlanItemControl.class);

	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		PlanItemControl planItemControl = new PlanItemControl();
		planItemControl.setElementId(attrs.getValue("id"));
		Object parent = parser.getParent();
		if (parent instanceof PlanItemInfo) {
			((PlanItemInfo<?>) parent).setItemControl(planItemControl);
		} else if (parent instanceof PlanItemDefinition) {
			((PlanItemDefinition) parent).setDefaultControl(planItemControl);
		} else if (parent instanceof DiscretionaryItem) {
			((DiscretionaryItem<?>) parent).setItemControl(planItemControl);
		}
		return planItemControl;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		Element el = parser.endElementBuilder();
		PlanItemControl planItemControl = (PlanItemControl) parser.getCurrent();
		planItemControl.setManualActivationRule(extractRule(el, "manualActivationRule"));
		planItemControl.setRepetitionRule(extractRule(el, "repetitionRule"));
		planItemControl.setRequiredRule(extractRule(el, "requiredRule"));
		return parser.getCurrent();
	}

	protected ConstraintImpl extractRule(Element el, String epxressionElementName3) {
		NodeList elems = el.getElementsByTagName(epxressionElementName3);
		if (elems.getLength() == 1) {
			Element rule = (Element) elems.item(0);
			return ConstraintExtractor.extractExpression(rule, "condition");
		}
		return null;
	}

	@Override
	public Class<?> generateNodeFor() {
		return PlanItemControl.class;
	}

}
