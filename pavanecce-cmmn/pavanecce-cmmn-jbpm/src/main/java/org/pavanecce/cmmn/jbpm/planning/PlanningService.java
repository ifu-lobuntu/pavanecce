package org.pavanecce.cmmn.jbpm.planning;

import java.util.Collection;
import java.util.Collections;

import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.PlannedWork;

public class PlanningService {
	public Collection<PlannedItem> getPlannedItemsForParentTask(long taskId){
		return Collections.emptySet();
	}
	public PlannedWork buildPlannedItemInfo(long processId, long parentWorkItemId, String discretionaryItemId){
		CaseInstance ci=null;
		return ci.createPlannedItem(parentWorkItemId, discretionaryItemId);
	}
}
