package org.pavanecce.cmmn.jbpm.planning;

import java.io.Serializable;
import java.util.Collection;

import org.pavanecce.cmmn.jbpm.ApplicableDiscretionaryItem;

public class PlanningTableInstance implements Serializable {
	private static final long serialVersionUID = 7379712745917913470L;
	private Collection<PlannedTaskSummary> plannedTasks;
	private Collection<ApplicableDiscretionaryItem> applicableDiscretionaryItems;

	public PlanningTableInstance() {
		super();
	}

	public PlanningTableInstance(Collection<PlannedTaskSummary> plannedTasks, Collection<ApplicableDiscretionaryItem> applicableDiscretionaryItems) {
		super();
		this.plannedTasks = plannedTasks;
		this.applicableDiscretionaryItems = applicableDiscretionaryItems;
	}

	public Collection<PlannedTaskSummary> getPlannedTasks() {
		return plannedTasks;
	}

	public void setPlannedTasks(Collection<PlannedTaskSummary> plannedTasks) {
		this.plannedTasks = plannedTasks;
	}

	public Collection<ApplicableDiscretionaryItem> getApplicableDiscretionaryItems() {
		return applicableDiscretionaryItems;
	}

	public void setApplicableDiscretionaryItems(Collection<ApplicableDiscretionaryItem> applicableDiscretionaryItems) {
		this.applicableDiscretionaryItems = applicableDiscretionaryItems;
	}

}
