package org.pavanecce.cmmn.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.kie.api.definition.process.Connection;

public class PlanItem extends WorkItemNode implements CMMNElement {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7613141769339402877L;
	private static final Work NO_WORK = new WorkImpl();
	private static final String DEFAULT = CONNECTION_DEFAULT_TYPE;
	static {
		NO_WORK.setName("NoWork");
	}
	private Map<String, Sentry> entryCriteria = new HashMap<String, Sentry>();
	private Map<String, Sentry> exitCriteria = new HashMap<String, Sentry>();
	private String definitionRef;
	private PlanItemDefinition definition;
	private String elementId;
	private Work work;

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

	public void putEntryCriterion(String s, Sentry c) {
		entryCriteria.put(s, c);
		if (c != null) {
			new ConnectionImpl(c, DEFAULT, this, DEFAULT);
		}
	}

	@Override
	public Connection getFrom() {
		final List<Connection> list = getIncomingConnections(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE);
		if (list.size() == 1) {
			return list.get(0);
		}
		return null;
	}

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

	public void putExitCriterion(String s, Sentry c) {
		exitCriteria.put(s, c);
		if (c != null) {
			c.addPlanItemExiting(this);
		}
	}

	public String getDefinitionRef() {
		return definitionRef;
	}

	public void setDefinitionRef(String definitionRef) {
		this.definitionRef = definitionRef;
	}

	public void setDefinition(PlanItemDefinition findPlanItemDefinition) {
		this.definition = findPlanItemDefinition;
	}

	public PlanItemDefinition getDefinition() {
		return definition;
	}

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	public Work getWork() {
		if (work == null) {
			if (definition instanceof WorkItemNode) {
				work = new WorkImpl();
				Work sourceWork = ((WorkItemNode) definition).getWork();
				work.setName(sourceWork.getName());
				for (ParameterDefinition pd : sourceWork.getParameterDefinitions()) {
					work.addParameterDefinition(new ParameterDefinitionImpl(pd.getName(), pd.getType()));
				}
				for (Entry<String, Object> entry : sourceWork.getParameters().entrySet()) {
					work.setParameter(entry.getKey(), entry.getValue());
				}
				work.setParameter("NodeName", getName());
			} else {
				// TODO rethink this
				return NO_WORK;
			}
		}
		return work;
	}

}
