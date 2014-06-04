package org.pavanecce.cmmn.jbpm.flow;

import java.util.Map.Entry;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;

public class CaseTaskPlanItem extends AbstractPlanItem<CaseTask> implements MultiInstancePlanItem, TaskItemWithDefinition<CaseTask> {

	private static final long serialVersionUID = 76131417693392877L;
	private Work work;
	private PlanItemInstanceFactoryNode factoryNode;

	public CaseTaskPlanItem() {
	}

	public CaseTaskPlanItem(PlanItemInfo<CaseTask> planItemInfo, PlanItemInstanceFactoryNode createFactoryNode) {
		super(planItemInfo);
		this.factoryNode = createFactoryNode;
	}

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
			work.setParameter(PeopleAssignmentHelper.GROUP_ID, TableItem.getPlannerRoles(this));
			work.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, TableItem.getPlannerRoles(this));
		}
		return work;
	}

}
