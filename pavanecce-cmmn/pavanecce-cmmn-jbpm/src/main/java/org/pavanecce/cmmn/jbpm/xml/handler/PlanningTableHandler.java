package org.pavanecce.cmmn.jbpm.xml.handler;

import java.util.Collection;
import java.util.Map.Entry;

import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.process.core.context.variable.Variable;
import org.pavanecce.cmmn.jbpm.flow.ApplicabilityRule;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.CaseTask;
import org.pavanecce.cmmn.jbpm.flow.DiscretionaryItem;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.Milestone;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemContainer;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInfo;
import org.pavanecce.cmmn.jbpm.flow.PlanningTable;
import org.pavanecce.cmmn.jbpm.flow.Role;
import org.pavanecce.cmmn.jbpm.flow.Sentry;
import org.pavanecce.cmmn.jbpm.flow.Stage;
import org.pavanecce.cmmn.jbpm.flow.TableItem;
import org.pavanecce.cmmn.jbpm.flow.TimerEvent;
import org.pavanecce.cmmn.jbpm.flow.UserEvent;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PlanningTableHandler extends AbstractTableItemHandler implements Handler {
	public PlanningTableHandler() {
		super();
		this.validParents.add(null);
		this.validParents.add(Case.class);
		this.validParents.add(HumanTask.class);
		this.validParents.add(PlanningTable.class);
		this.validParents.add(Stage.class);
		this.validPeers.add(null);
		this.validPeers.add(PlanningTable.class);
		this.validPeers.add(DiscretionaryItem.class);
		this.validPeers.add(ApplicabilityRule.class);
		this.validPeers.add(Sentry.class);
		this.validPeers.add(PlanItem.class);
		this.validPeers.add(HumanTask.class);
		this.validPeers.add(CaseTask.class);
		this.validPeers.add(Variable.class);
		this.validPeers.add(Role.class);
		this.validPeers.add(Stage.class);
		this.validPeers.add(Milestone.class);
		this.validPeers.add(CaseParameter.class);
		this.validPeers.add(UserEvent.class);
		this.validPeers.add(TimerEvent.class);
		this.validPeers.add(PlanItemInfo.class);
		this.validPeers.add(PlanItem.class);
	}

	@Override
	public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);
		PlanningTable table = new PlanningTable();
		super.populateCommonItems(attrs, table);
		table.setId(IdGenerator.getIdAsUniqueAsUuid(parser, table));
		Object parent = parser.getParent();
		if (parent instanceof PlanningTable) {
			((PlanningTable) parent).addTableItem(table);
		} else if (parent instanceof PlanItemContainer) {
			((PlanItemContainer) parent).setPlanningTable(table);
		} else if (parent instanceof HumanTask) {
			((HumanTask) parent).setPlanningTable(table);
		}
		return table;
	}

	@Override
	public Object end(String uri, String localName, ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();
		PlanningTable table = (PlanningTable) parser.getCurrent();
		Collection<ApplicabilityRule> applicabilityRules = table.getOwnedApplicabilityRules();
		for (TableItem ti : table.getTableItems()) {
			// TODO recursively? Check with OMG
			for (Entry<String, ApplicabilityRule> entry : ti.getApplicabilityRules().entrySet()) {
				for (ApplicabilityRule rule : applicabilityRules) {
					if (entry.getValue() == null && entry.getKey().equals(rule.getElementId())) {
						entry.setValue(rule);
						break;
					}
				}
			}
		}
		return table;
	}

	@Override
	public Class<?> generateNodeFor() {
		return PlanningTable.class;
	}

}
