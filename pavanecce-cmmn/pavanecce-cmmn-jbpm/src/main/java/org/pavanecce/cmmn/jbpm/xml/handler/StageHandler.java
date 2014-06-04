package org.pavanecce.cmmn.jbpm.xml.handler;

import java.util.HashSet;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.process.core.context.variable.Variable;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.CaseTask;
import org.pavanecce.cmmn.jbpm.flow.DiscretionaryItem;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.Milestone;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInfo;
import org.pavanecce.cmmn.jbpm.flow.PlanningTable;
import org.pavanecce.cmmn.jbpm.flow.Role;
import org.pavanecce.cmmn.jbpm.flow.Sentry;
import org.pavanecce.cmmn.jbpm.flow.Stage;
import org.pavanecce.cmmn.jbpm.flow.TimerEvent;
import org.pavanecce.cmmn.jbpm.flow.UserEvent;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class StageHandler extends PlanItemContainerHandler implements Handler {
	public StageHandler() {
		this.validParents = new HashSet<Class<?>>();
		this.validParents.add(Case.class);
		this.validParents.add(Stage.class);

		this.validPeers = new HashSet<Class<?>>();
		this.validPeers.add(null);
		this.validPeers.add(Sentry.class);
		this.validPeers.add(PlanItemInfo.class);
		this.validPeers.add(PlanItem.class);
		this.validPeers.add(HumanTask.class);
		this.validPeers.add(Variable.class);
		this.validPeers.add(Role.class);
		this.validPeers.add(CaseTask.class);
		this.validPeers.add(Variable.class);
		this.validPeers.add(Stage.class);
		this.validPeers.add(Milestone.class);
		this.validPeers.add(CaseParameter.class);
		this.validPeers.add(UserEvent.class);
		this.validPeers.add(TimerEvent.class);
		this.validPeers.add(PlanItemInfo.class);
		this.validPeers.add(PlanItem.class);
		this.validPeers.add(DiscretionaryItem.class);
		this.validPeers.add(PlanningTable.class);
	}

	@Override
	public Class<?> generateNodeFor() {
		return Stage.class;
	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		Stage node = new Stage();
		node.setElementId(attrs.getValue("id"));
		node.setAutoComplete("true".equals(attrs.getValue("autoComplete")));
		node.setName(attrs.getValue("name"));
		node.setId(IdGenerator.getIdAsUniqueAsUuid(parser, node));
		Case theCase = (Case) parser.getParent(Case.class);
		theCase.addPlanItemDefinition(node);
		super.startNodeContainer(node, parser);
		return node;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();
		return parser.getCurrent();
	}

}
