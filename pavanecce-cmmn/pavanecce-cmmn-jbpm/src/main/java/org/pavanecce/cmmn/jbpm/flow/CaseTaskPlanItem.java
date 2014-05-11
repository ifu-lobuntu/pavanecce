package org.pavanecce.cmmn.jbpm.flow;

import java.util.List;

import org.jbpm.workflow.core.node.SubProcessNode;
import org.kie.api.definition.process.Connection;

public class CaseTaskPlanItem extends SubProcessNode implements PlanItem {

	private static final long serialVersionUID = 76131417693392877L;
	private String elementId;
	private PlanItemInfo info;

	public CaseTaskPlanItem(PlanItemInfo info) {
		this.info = info;
	}

	@Override
	public String getProcessId() {
		return ((CaseTask)info.getDefinition()).getProcessId();
	}

	@Override
	public PlanItemInfo getPlanInfo() {
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

	}

	@Override
	public String getElementId() {
		return elementId;
	}

	@Override
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

}
