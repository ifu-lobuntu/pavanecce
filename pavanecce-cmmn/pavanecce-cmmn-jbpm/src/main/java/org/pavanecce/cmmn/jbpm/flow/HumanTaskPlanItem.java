package org.pavanecce.cmmn.jbpm.flow;

import java.util.List;
import java.util.Map.Entry;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.kie.api.definition.process.Connection;

public class HumanTaskPlanItem extends WorkItemNode implements PlanItem<HumanTask>,MultiInstancePlanItem {

	private static final long serialVersionUID = 7613141769339402877L;
	private static final Work NO_WORK = new WorkImpl();
	static {
		NO_WORK.setName("NoWork");
	}
	private Work work;
	private String elementId;
	private PlanItemInfo<HumanTask> info;
	private PlanItemContainer planItemContainer;
	private String description;
	private PlanItemInstanceFactoryNode factoryNode;
	public HumanTaskPlanItem() {
	}
	public HumanTaskPlanItem(PlanItemInfo<HumanTask> info, PlanItemInstanceFactoryNode factorNode){
		this.info=info;
		this.factoryNode=factorNode;
	}
	@Override
	public PlanItemInstanceFactoryNode getFactoryNode() {
		return factoryNode;
	}
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String s){
		this.description=s;
	}
	@Override
	public boolean isWaitForCompletion() {
		return ((HumanTask)getPlanInfo().getDefinition()).isBlocking();
	}
	@Override
	public PlanItemInfo<HumanTask> getPlanInfo() {
		return info;
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



	@Override
	public String getElementId() {
		return elementId;
	}

	@Override
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public Work getWork() {
		if (work == null) {
			if (info.getDefinition() instanceof WorkItemNode) {
				work = new WorkImpl();
				Work sourceWork = info.getDefinition().getWork();
				work.setName(sourceWork.getName());
				for (ParameterDefinition pd : sourceWork.getParameterDefinitions()) {
					work.addParameterDefinition(new ParameterDefinitionImpl(pd.getName(), pd.getType()));
				}
				for (Entry<String, Object> entry : sourceWork.getParameters().entrySet()) {
					work.setParameter(entry.getKey(), entry.getValue());
				}
				work.setParameter("NodeName", getName());
				work.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, TableItem.getPlannerRoles(this));
			} else {
				// TODO rethink this
				return NO_WORK;
			}
		}
		return work;
	}
	public PlanItemContainer getPlanItemContainer() {
		return planItemContainer;
	}
	public void setPlanItemContainer(PlanItemContainer planItemContainer) {
		this.planItemContainer = planItemContainer;
	}

}
