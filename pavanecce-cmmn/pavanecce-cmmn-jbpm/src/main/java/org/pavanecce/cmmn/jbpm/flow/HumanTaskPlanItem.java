package org.pavanecce.cmmn.jbpm.flow;

import java.util.List;
import java.util.Map.Entry;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.kie.api.definition.process.Connection;

public class HumanTaskPlanItem extends AbstractPlanItem<HumanTask> implements MultiInstancePlanItem,TaskItemWithDefinition<HumanTask> {

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

	@Override
	public Connection getFrom() {
		final List<Connection> list = getIncomingConnections(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE);
		if (list.size() == 1) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public void validateAddIncomingConnection(final String type, final Connection connection) {
		if (type == null) {
			throw new IllegalArgumentException("Connection type cannot be null");
		}
		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}
		if (!org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE.equals(type)) {
			throw new IllegalArgumentException("This type of node only accepts default incoming connection type!");
		}
		// if (getFrom() != null &&
		// !"true".equals(System.getProperty("jbpm.enable.multi.con"))) {
		// throw new IllegalArgumentException(
		// "This type of node cannot have more than one incoming connection!");
		// }
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
