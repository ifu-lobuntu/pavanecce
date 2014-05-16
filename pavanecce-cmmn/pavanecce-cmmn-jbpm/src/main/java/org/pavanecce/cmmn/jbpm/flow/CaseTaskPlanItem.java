package org.pavanecce.cmmn.jbpm.flow;

import java.util.List;
import java.util.Map.Entry;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.kie.api.definition.process.Connection;

public class CaseTaskPlanItem extends SubProcessNode implements PlanItem<CaseTask>,MultiInstancePlanItem {

	private static final long serialVersionUID = 76131417693392877L;
	private String elementId;
	private PlanItemInfo<CaseTask> info;
	private Work work;
	private PlanItemContainer planItemContainer;
	private String description;
	private PlanItemInstanceFactoryNode factoryNode;

	public CaseTaskPlanItem(PlanItemInfo<CaseTask> planItemInfo, PlanItemInstanceFactoryNode createFactoryNode) {
		this.factoryNode = createFactoryNode;
		this.info = planItemInfo;
	}

	public PlanItemInstanceFactoryNode getFactoryNode() {
		return factoryNode;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String s) {
		this.description = s;
	}

	@Override
	public String getProcessId() {
		return ((CaseTask) info.getDefinition()).getProcessId();
	}

	@Override
	public PlanItemInfo<CaseTask> getPlanInfo() {
		return info;
	}

	@Override
	public boolean isWaitForCompletion() {
		return ((CaseTask) getPlanInfo().getDefinition()).isBlocking();
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

	}

	public Work getWork() {
		if (work == null) {
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
			work.setParameter(PeopleAssignmentHelper.GROUP_ID, TableItem.getPlannerRoles(this));
			work.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, TableItem.getPlannerRoles(this));
		}
		return work;
	}

	@Override
	public String getElementId() {
		return elementId;
	}

	@Override
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public PlanItemContainer getPlanItemContainer() {
		return planItemContainer;
	}

	public void setPlanItemContainer(PlanItemContainer planItemContainer) {
		this.planItemContainer = planItemContainer;
	}

}
