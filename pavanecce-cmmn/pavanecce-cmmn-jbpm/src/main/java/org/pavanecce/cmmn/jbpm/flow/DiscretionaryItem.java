package org.pavanecce.cmmn.jbpm.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.ConnectionImpl;

public class DiscretionaryItem<T extends PlanItemDefinition> extends TableItem implements TaskItemWithDefinition<T> {
	private static final long serialVersionUID = 2371336993789669482L;
	private T definition;
	private String definitionRef;
	private PlanItemControl itemControl;
	private long id;
	private Work work;
	private Map<String, Sentry> entryCriteria = new HashMap<String, Sentry>();
	private Map<String, Sentry> exitCriteria = new HashMap<String, Sentry>();
	private PlanItemInstanceFactoryNode factoryNode;

	public Map<String, Sentry> getEntryCriteria() {
		return Collections.unmodifiableMap(entryCriteria);
	}

	public Map<String, Sentry> getExitCriteria() {
		return Collections.unmodifiableMap(exitCriteria);
	}

	private PlanItemInstanceFactoryNode createFactoryNode() {
		PlanItemInstanceFactoryNode result = new PlanItemInstanceFactoryNode();
		result.setId(id / 3334123);
		result.setName(getName() + "Factory");
		result.setItemToInstantiate(this);
		return result;
	}

	public void putEntryCriterion(String s, Sentry c) {
		entryCriteria.put(s, c);
	}

	public void putExitCriterion(String s, Sentry c) {
		exitCriteria.put(s, c);
	}

	@Override
	public T getDefinition() {
		return definition;
	}

	@Override
	public String getName() {
		return getDefinition().getName();
	}

	public void setDefinition(T definition) {
		this.definition = definition;
	}

	public String getDefinitionRef() {
		return definitionRef;
	}

	public void setDefinitionRef(String definitionRef) {
		this.definitionRef = definitionRef;
	}

	@Override
	public PlanItemControl getItemControl() {
		return itemControl;
	}

	public void setItemControl(PlanItemControl itemControl) {
		this.itemControl = itemControl;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public Work getWork() {
		if (work == null) {
			work = new WorkImpl();
			Work sourceWork = null;
			if (getDefinition() instanceof TaskDefinition) {
				sourceWork = ((TaskDefinition) getDefinition()).getWork();
			} else if (getDefinition() instanceof Stage) {
				sourceWork = ((Stage) getDefinition()).getWork();
			}
			work.setName(sourceWork.getName());
			for (ParameterDefinition pd : sourceWork.getParameterDefinitions()) {
				work.addParameterDefinition(new ParameterDefinitionImpl(pd.getName(), pd.getType()));
			}
			for (Entry<String, Object> entry : sourceWork.getParameters().entrySet()) {
				work.setParameter(entry.getKey(), entry.getValue());
			}
			work.setParameter("NodeName", getName());
			work.setParameter(PeopleAssignmentHelper.GROUP_ID, TableItem.getPlannerRoles(this));
			work.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, TableItem.getPlannerRoles(this));
		}
		return work;

	}

	public void copyFromDefinition() {
		long id = this.id;
		HashMap<Object, Object> copiedState = new HashMap<Object, Object>();
		T from = getDefinition();
		this.setNodeContainer(getParentTable().getFirstPlanItemContainer());
		copiedState.put(from, this);
		copy(copiedState, from, this);
		this.id = id;
	}

	@Override
	public PlanItemControl getEffectiveItemControl() {
		if (getItemControl() == null) {
			return getDefinition().getDefaultControl();
		} else {
			return getItemControl();
		}
	}

	@Override
	public PlanItemContainer getPlanItemContainer() {
		return getParentTable().getFirstPlanItemContainer();
	}

	@Override
	public String getPlanItemEventName() {
		return this.getElementId();
	}

	@Override
	public String getEffectiveName() {
		return getDefinition().getName();
	}

	public void linkItem() {
		Set<Entry<String, Sentry>> entrySet = entryCriteria.entrySet();
		for (Entry<String, Sentry> entry : entrySet) {
			entry.getValue().setPlanItemEntering(this);
			new ConnectionImpl(entry.getValue(), Node.CONNECTION_DEFAULT_TYPE, getFactoryNode(), Node.CONNECTION_DEFAULT_TYPE);
		}
		if (!entrySet.isEmpty()) {
			((NodeContainer) entrySet.iterator().next().getValue().getNodeContainer()).addNode(getFactoryNode());
		}
		Set<Entry<String, Sentry>> exitSet = exitCriteria.entrySet();
		for (Entry<String, Sentry> entry : exitSet) {
			entry.getValue().setPlanItemExiting(this);
		}
		// new ConnectionImpl(getFactoryNode(), Node.CONNECTION_DEFAULT_TYPE, this, Node.CONNECTION_DEFAULT_TYPE);
	}

	public PlanItemInstanceFactoryNode getFactoryNode() {
		if (factoryNode == null) {
			factoryNode = createFactoryNode();
		}
		return factoryNode;
	}

}
