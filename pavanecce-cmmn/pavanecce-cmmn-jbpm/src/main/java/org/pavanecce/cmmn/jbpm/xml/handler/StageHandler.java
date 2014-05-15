package org.pavanecce.cmmn.jbpm.xml.handler;

import java.util.HashSet;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInfo;
import org.pavanecce.cmmn.jbpm.flow.Role;
import org.pavanecce.cmmn.jbpm.flow.Sentry;
import org.pavanecce.cmmn.jbpm.flow.Stage;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class StageHandler extends PlanItemContainerHandler implements Handler {
	public StageHandler() {
		this.validParents = new HashSet<Class<?>>();
		this.validParents.add(RuleFlowProcess.class);

		this.validPeers = new HashSet<Class<?>>();
		this.validPeers.add(null);
		this.validPeers.add(Sentry.class);
		this.validPeers.add(PlanItemInfo.class);
		this.validPeers.add(PlanItem.class);
		this.validPeers.add(HumanTask.class);
		this.validPeers.add(Variable.class);
		this.validPeers.add(Role.class);
	}

	@Override
	public Class<?> generateNodeFor() {
		return Stage.class;
	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		Stage node = new Stage();
		node.setId(IdGenerator.next(parser));
		node.setElementId(attrs.getValue("id"));
		node.setAutoComplete("true".equals(attrs.getValue("autoComplete")));
		node.setName(attrs.getValue("name"));
		Case theCase = (Case) parser.getParent(Case.class);
		theCase.addPlanItemDefinition(node);
		super.startNodeContainer(node,parser);
		return node;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();
		return parser.getCurrent();
	}

}
