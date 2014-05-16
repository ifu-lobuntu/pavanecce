package org.pavanecce.cmmn.jbpm.flow;

import org.jbpm.workflow.core.node.EventNodeInterface;
import org.jbpm.workflow.core.node.Join;

public class DefaultJoin extends Join implements EventNodeInterface {

	private static final long serialVersionUID = 4769541084056401100L;
	public static final String CLOSE = "Close";
	public static final String COMPLETE = "Complete";

	@Override
	public boolean acceptsEvent(String type, Object event) {
		return type.equals(DefaultJoin.CLOSE) || type.equals(DefaultJoin.COMPLETE);
	}

}
