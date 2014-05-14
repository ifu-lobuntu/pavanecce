package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.ConnectionImpl;

public class PlanItemInfo<T extends PlanItemDefinition> {
	private Map<String, Sentry> entryCriteria = new HashMap<String, Sentry>();
	private Map<String, Sentry> exitCriteria = new HashMap<String, Sentry>();
	private String definitionRef;
	private PlanItem<T> planItem;
	private T definition;
	private String elementId;
	private String name;
	private long id;
	private NodeContainer nodeContainer;
	private PlanItemControl itemControl;

	public PlanItemInfo() {
	}

	public Map<String, Sentry> getEntryCriteria() {
		return Collections.unmodifiableMap(entryCriteria);
	}

	public Map<String, Sentry> getExitCriteria() {
		return Collections.unmodifiableMap(exitCriteria);
	}

	public void addEntryCriterionRef(String s) {
		entryCriteria.put(s, null);
	}

	public void addExitCriterionRef(String s) {
		exitCriteria.put(s, null);
	}

	@SuppressWarnings("unchecked")
	public PlanItem<T> buildPlanItem() {
		if (definition instanceof HumanTask) {
			planItem = (PlanItem<T>) new HumanTaskPlanItem((PlanItemInfo<HumanTask>) this);
		} else if (definition instanceof Stage) {
			planItem = (PlanItem<T>) new StagePlanItem((PlanItemInfo<Stage>) this);
		} else if (definition instanceof UserEventListener) {
			planItem = (PlanItem<T>) new UserEventPlanItem((PlanItemInfo<UserEventListener>) this);
		} else if (definition instanceof TimerEventListener) {
			planItem = (PlanItem<T>) new TimerEventPlanItem((PlanItemInfo<TimerEventListener>) this);
		} else if (definition instanceof Milestone) {
			planItem = (PlanItem<T>) new MilestonePlanItem((PlanItemInfo<Milestone>) this);
		} else if (definition instanceof CaseTask) {
			planItem = (PlanItem<T>) new CaseTaskPlanItem((PlanItemInfo<CaseTask>) this);
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
			entry.getValue().setPlanItemEntering(this.planItem);
			new ConnectionImpl(entry.getValue(), Node.CONNECTION_DEFAULT_TYPE, planItem, Node.CONNECTION_DEFAULT_TYPE);
		}
		Set<Entry<String, Sentry>> exitSet = exitCriteria.entrySet();
		for (Entry<String, Sentry> entry : exitSet) {
			entry.getValue().setPlanItemExiting(this.planItem);
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

	public void setDefinition(T findPlanItemDefinition) {
		this.definition = findPlanItemDefinition;
	}

	public T getDefinition() {
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

	public PlanItemControl getItemControl() {
		if (itemControl == null && definition != null) {
			return definition.getDefaultControl();
		}
		return itemControl;
	}

	public void setItemControl(PlanItemControl itemControl) {
		this.itemControl = itemControl;
	}
}
