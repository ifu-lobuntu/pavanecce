package org.pavanecce.cmmn.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jbpm.process.core.event.EventFilter;
import org.jbpm.workflow.core.node.EventNode;

public class Sentry extends EventNode {
	private static final long serialVersionUID = -3568385090236274366L;
	private List<OnPart> onParts = new ArrayList<OnPart>();
	private String elementId;

	public Sentry() {
	}

	public void addOnPart(OnPart onPart) {
		this.onParts.add(onPart);
	}

	@Override
	public String getType() {
		return "too-many-events";
	}

	public List<OnPart> getOnParts() {
		return onParts;
	}

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}
	@Override
	public List<EventFilter> getEventFilters() {
		return Collections.<EventFilter>unmodifiableList(onParts);
	};

}
