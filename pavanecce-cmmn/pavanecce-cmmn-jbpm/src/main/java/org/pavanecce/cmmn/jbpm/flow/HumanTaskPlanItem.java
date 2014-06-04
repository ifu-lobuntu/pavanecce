package org.pavanecce.cmmn.jbpm.flow;

import java.util.Map.Entry;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;

public class HumanTaskPlanItem extends AbstractPlanItem<HumanTask> implements MultiInstancePlanItem, TaskItemWithDefinition<HumanTask> {

	private static final long serialVersionUID = 7613141769339402877L;
	private Work work;
	private PlanItemInstanceFactoryNode factoryNode;

	public HumanTaskPlanItem() {
	}

	public HumanTaskPlanItem(PlanItemInfo<HumanTask> info, PlanItemInstanceFactoryNode factorNode) {
		super(info);
		this.factoryNode = factorNode;
	}

	@Override
	public PlanItemInstanceFactoryNode getFactoryNode() {
		return factoryNode;
	}

	public Work getWork() {
		if (work == null) {
			work = new WorkImpl();
			Work sourceWork = getDefinition().getWork();
			work.setName(sourceWork.getName());
			for (ParameterDefinition pd : sourceWork.getParameterDefinitions()) {
				work.addParameterDefinition(new ParameterDefinitionImpl(pd.getName(), pd.getType()));
			}
			for (Entry<String, Object> entry : sourceWork.getParameters().entrySet()) {
				work.setParameter(entry.getKey(), entry.getValue());
			}
			work.setParameter("NodeName", getName());
			work.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, TableItem.getPlannerRoles(this));
		}
		return work;
	}

}
