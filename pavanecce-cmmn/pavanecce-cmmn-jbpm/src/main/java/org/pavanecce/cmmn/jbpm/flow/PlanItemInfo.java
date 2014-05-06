package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.DataAssociation;

public class PlanItemInfo {
	private Map<String, Sentry> entryCriteria = new HashMap<String, Sentry>();
	private Map<String, Sentry> exitCriteria = new HashMap<String, Sentry>();
	private String definitionRef;
	private PlanItem planItem;
	private PlanItemDefinition definition;
	private String elementId;
	private String name;
	private long id;
	private NodeContainer nodeContainer;

	public PlanItemInfo() {
	}

	public Map<String, Sentry> getEntryCriteria() {
		return Collections.unmodifiableMap(entryCriteria);
	}

	public Map<String, Sentry> getExitCriteria() {
		return Collections.unmodifiableMap(exitCriteria);
	}

	public List<DataAssociation> getInAssociations() {
		return definition.getInAssociations();
	}

	public void addEntryCriterionRef(String s) {
		entryCriteria.put(s, null);
	}

	public void addExitCriterionRef(String s) {
		exitCriteria.put(s, null);
	}

	public PlanItem buildPlanItem() {
		if (definition instanceof HumanTask) {
			planItem = new HumanTaskPlanItem(this);
		} else if (definition instanceof Stage) {
			planItem = new StagePlanItem(this);
		}
		planItem.setElementId(getElementId());
		planItem.setName(getName());
		planItem.setId(id);
		nodeContainer.addNode(planItem);

		return planItem;
	}

	public void linkPlanItem() {
		Set<Entry<String, Sentry>> entrySet = entryCriteria.entrySet();
		for (Entry<String, Sentry> entry : entrySet) {
			new ConnectionImpl(entry.getValue(), NodeImpl.CONNECTION_DEFAULT_TYPE, planItem, NodeImpl.CONNECTION_DEFAULT_TYPE);
		}
		Set<Entry<String, Sentry>> exitSet = exitCriteria.entrySet();
		for (Entry<String, Sentry> entry : exitSet) {
			entry.getValue().addPlanItemExiting(this.planItem);
		}
	}

	public void putEntryCriterion(String s, Sentry c) {
		entryCriteria.put(s, c);
	}

	public void putExitCriterion(String s, Sentry c) {
		exitCriteria.put(s, c);
	}

	public String getDefinitionRef() {
		return definitionRef;
	}

	public void setDefinitionRef(String definitionRef) {
		this.definitionRef = definitionRef;
	}

	public void setDefinition(PlanItemDefinition findPlanItemDefinition) {
		this.definition = findPlanItemDefinition;
	}

	public PlanItemDefinition getDefinition() {
		return definition;
	}

	public void setElementId(String value) {
		this.elementId = value;
	}

	public String getElementId() {
		return elementId;
	}

	public void setName(String value) {
		this.name = value;
	}

	public String getName() {
		return name;
	}

	public void setId(long next) {
		this.id = next;
	}

	public void setContainer(PlanItemContainer nodeContainer) {
		this.nodeContainer = nodeContainer;
		nodeContainer.addPlanItemInfo(this);

	}
}
