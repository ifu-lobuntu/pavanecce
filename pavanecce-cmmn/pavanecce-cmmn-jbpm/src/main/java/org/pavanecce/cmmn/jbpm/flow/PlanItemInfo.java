package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.ConnectionImpl;

/**
 * This class represents a workaround for the parallel inheritance trees between PlanItems and PlanItemDefinitions Not
 * sure if we can circumvent this problem
 */
public class PlanItemInfo<T extends PlanItemDefinition> {
	private Map<String, Sentry> entryCriteria = new HashMap<String, Sentry>();
	private Map<String, Sentry> exitCriteria = new HashMap<String, Sentry>();
	private String definitionRef;
	private PlanItem<T> planItem;
	private T definition;
	private String elementId;
	private String name;
	private long id;
	private PlanItemContainer nodeContainer;
	private PlanItemControl itemControl;

	public PlanItemInfo() {
	}

	public Map<String, Sentry> getEntryCriteria() {
		return Collections.unmodifiableMap(entryCriteria);
	}

	public Map<String, Sentry> getExitCriteria() {
		return Collections.unmodifiableMap(exitCriteria);
	}

	public void putEntryCriterion(String s, Sentry c) {
		entryCriteria.put(s, c);
	}

	public void putExitCriterion(String s, Sentry c) {
		exitCriteria.put(s, c);
	}

	private PlanItemInstanceFactoryNode createFactoryNode() {
		PlanItemInstanceFactoryNode result = new PlanItemInstanceFactoryNode();
		result.setId(id / 34123);
		result.setName(getName() + "Factory");
		return result;
	}

	@SuppressWarnings("unchecked")
	public PlanItem<T> buildPlanItem() {
		if (definition instanceof HumanTask) {
			planItem = (PlanItem<T>) new HumanTaskPlanItem((PlanItemInfo<HumanTask>) this, createFactoryNode());
		} else if (definition instanceof Stage) {
			planItem = (PlanItem<T>) new StagePlanItem((PlanItemInfo<Stage>) this, createFactoryNode());
		} else if (definition instanceof UserEvent) {
			planItem = (PlanItem<T>) new UserEventPlanItem((PlanItemInfo<UserEvent>) this);
		} else if (definition instanceof TimerEvent) {
			planItem = (PlanItem<T>) new TimerEventPlanItem((PlanItemInfo<TimerEvent>) this);
		} else if (definition instanceof Milestone) {
			planItem = (PlanItem<T>) new MilestonePlanItem((PlanItemInfo<Milestone>) this, createFactoryNode());
		} else if (definition instanceof CaseTask) {
			planItem = (PlanItem<T>) new CaseTaskPlanItem((PlanItemInfo<CaseTask>) this, createFactoryNode());
		}
		planItem.setPlanItemContainer(nodeContainer); // possible duplication here of setNodeContainer
		planItem.setElementId(getElementId());
		planItem.setName(getName());
		planItem.setId(id);
		nodeContainer.addNode(planItem);
		if (planItem instanceof MultiInstancePlanItem) {
			nodeContainer.addNode(((MultiInstancePlanItem) planItem).getFactoryNode());
			((MultiInstancePlanItem) planItem).getFactoryNode().setItemToInstantiate(planItem);
		}

		return planItem;
	}

	public void linkPlanItem() {
		Set<Entry<String, Sentry>> entrySet = entryCriteria.entrySet();
		for (Entry<String, Sentry> entry : entrySet) {
			entry.getValue().setPlanItemEntering(this.planItem);
			if (planItem instanceof MultiInstancePlanItem) {
				new ConnectionImpl(entry.getValue(), Node.CONNECTION_DEFAULT_TYPE, ((MultiInstancePlanItem) planItem).getFactoryNode(),
						Node.CONNECTION_DEFAULT_TYPE);
			} else {
				new ConnectionImpl(entry.getValue(), Node.CONNECTION_DEFAULT_TYPE, planItem, Node.CONNECTION_DEFAULT_TYPE);
			}
		}
		Set<Entry<String, Sentry>> exitSet = exitCriteria.entrySet();
		for (Entry<String, Sentry> entry : exitSet) {
			entry.getValue().setPlanItemExiting(this.planItem);
		}
		if (planItem instanceof MultiInstancePlanItem) {
			new ConnectionImpl(((MultiInstancePlanItem) planItem).getFactoryNode(), Node.CONNECTION_DEFAULT_TYPE, planItem, Node.CONNECTION_DEFAULT_TYPE);
		}
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
