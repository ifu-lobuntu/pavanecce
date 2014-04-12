package org.pavanecce.cmmn.flow;

import java.io.Serializable;

import org.jbpm.process.core.event.EventFilter;
import org.jbpm.process.core.event.EventTypeFilter;

public abstract class OnPart extends EventTypeFilter implements Serializable, EventFilter {
	@Override
	public abstract String getType();

	
	public static String getType(String fileItemName, Enum<?> transition) {
		return "On" + transition.name() + "Of" + fileItemName;
	}


}
