package org.pavanecce.cmmn.jbpm.flow;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.StateNode;
import org.kie.api.definition.process.Node;

public class StagePlanItem extends StateNode implements PlanItem<Stage>, MultiInstancePlanItem, PlanItemContainer {
	private static final long serialVersionUID = -4998194330899363230L;
	private String elementId;
	private PlanItemInfo<Stage> info;
	private PlanItemContainer planItemContainer;
	private String description;
	private PlanItemInstanceFactoryNode factoryNode;

	private StartNode defaultStart;
	private DefaultSplit defaultSplit;
	private EndNode defaultEnd;
	private DefaultJoin defaultJoin;
	private PlanningTable planningTable;
	private boolean autoComplete;

	public StagePlanItem() {
	}

	public StagePlanItem(PlanItemInfo<Stage> info, PlanItemInstanceFactoryNode planItemInstanceFactoryNode) {
		this.info = info;
		this.factoryNode = planItemInstanceFactoryNode;
	}

	public StartNode getDefaultStart() {
		return defaultStart;
	}

	public void setDefaultStart(StartNode defaultStart) {
		this.defaultStart = defaultStart;
	}

	public DefaultSplit getDefaultSplit() {
		return defaultSplit;
	}

	public void setDefaultSplit(DefaultSplit defaultSplit) {
		this.defaultSplit = defaultSplit;
	}

	public EndNode getDefaultEnd() {
		return defaultEnd;
	}

	public void setDefaultEnd(EndNode defaultEnd) {
		this.defaultEnd = defaultEnd;
	}

	public DefaultJoin getDefaultJoin() {
		return defaultJoin;
	}

	public void setDefaultJoin(DefaultJoin defaultJoin) {
		this.defaultJoin = defaultJoin;
	}

	public PlanningTable getPlanningTable() {
		return planningTable;
	}

	public void setPlanningTable(PlanningTable planningTable) {
		this.planningTable = planningTable;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String s) {
		this.description = s;
	}

	public Stage getStage() {
		return (Stage) info.getDefinition();
	}

	@Override
	public PlanItemInfo<Stage> getPlanInfo() {
		return info;
	}

	@Override
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public String getElementId() {
		return elementId;
	}

	public void copyFromStage() {
		HashMap<Object, Object> copiedState = new HashMap<Object, Object>();
		Stage stage = getStage();
		copiedState.put(stage, this);
		copy(copiedState, stage, this);
		this.autoComplete = stage.isAutoComplete();
		this.defaultStart = copy(copiedState, stage.getDefaultStart());
		this.defaultSplit = copy(copiedState, stage.getDefaultSplit());
		this.defaultJoin = copy(copiedState, stage.getDefaultJoin());
		this.defaultEnd = copy(copiedState, stage.getDefaultEnd());
		this.planningTable=copy(copiedState,stage.getPlanningTable());
	}

	public void copy(Map<Object, Object> copiedState, Object from, Object to) {
		Class<?> class1 = from.getClass();
		while (class1 != Object.class) {
			if (class1.isInstance(to)) {
				copy(copiedState, from, to, class1);
			}
			class1 = class1.getSuperclass();
		}
	}

	public void copy(Map<Object, Object> copiedState, Object from, Object to, Class<?> class1) {
		for (Field field : class1.getDeclaredFields()) {
			if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
				field.setAccessible(true);
				try {
					if (!isIgnored(to, field)) {
						Object fromFieldValue = field.get(from);
						if (fromFieldValue != null) {
							Object toValue = copy(copiedState, fromFieldValue);
							field.set(to, toValue);
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public boolean isIgnored(Object target, Field field) {
		if(field.getDeclaringClass()==NodeImpl.class && target==this){
			return true;
		}
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T copy(Map<Object, Object> copiedState, T fromFieldValue) {
		try {
			if (fromFieldValue instanceof String || fromFieldValue instanceof Number || fromFieldValue instanceof Boolean) {
				return fromFieldValue;
			} else if (copiedState.containsKey(fromFieldValue)) {
				return (T) copiedState.get(fromFieldValue);
			} else if (fromFieldValue instanceof PlanItemDefinition) {
				return fromFieldValue;
			} else if (fromFieldValue instanceof Enum<?>) {
				return fromFieldValue;
			}
			if (fromFieldValue instanceof Collection) {
				Collection fromCollection = (Collection) fromFieldValue;
				Collection toCollection = fromCollection.getClass().newInstance();
				for (Object object : fromCollection) {
					toCollection.add(copy(copiedState, object));
				}
				return (T) toCollection;
			} else if (fromFieldValue instanceof Map) {
				Map fromCollection = (Map) fromFieldValue;
				Map toCollection = fromCollection.getClass().newInstance();
				Set<Map.Entry> entrySet = fromCollection.entrySet();
				for (Map.Entry object : entrySet) {
					toCollection.put(copy(copiedState, object.getKey()), copy(copiedState, object.getValue()));
				}
				return (T) toCollection;
			} else {
				Object newInstance = fromFieldValue.getClass().newInstance();
				copiedState.put(fromFieldValue, newInstance);
				copy(copiedState, fromFieldValue, newInstance);
				return (T) newInstance;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Work getWork() {
		WorkImpl work = new WorkImpl();
		Work sourceWork = getStage().getWork();
		work.setName(sourceWork.getName());
		for (ParameterDefinition pd : sourceWork.getParameterDefinitions()) {
			work.addParameterDefinition(new ParameterDefinitionImpl(pd.getName(), pd.getType()));
		}
		for (Entry<String, Object> entry : sourceWork.getParameters().entrySet()) {
			work.setParameter(entry.getKey(), entry.getValue());
		}
		work.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, TableItem.getPlannerRoles(this));
		work.setParameter(PeopleAssignmentHelper.GROUP_ID, TableItem.getPlannerRoles(this));
		work.setParameter("NodeName", getName());
		return work;
	}

	public PlanItemContainer getPlanItemContainer() {
		return planItemContainer;
	}

	public void setPlanItemContainer(PlanItemContainer planItemContainer) {
		this.planItemContainer = planItemContainer;
	}

	@Override
	public PlanItemInstanceFactoryNode getFactoryNode() {
		return factoryNode;
	}

	@Override
	public void addPlanItemInfo(PlanItemInfo<?> d) {
	}

	@Override
	public Collection<PlanItemInfo<?>> getPlanItemInfo() {
		return this.getStage().getPlanItemInfo();
	}

	@Override
	public Case getCase() {
		return this.getStage().getCase();
	}

	@Override
	public boolean isAutoComplete() {
		return autoComplete;
	}
}
