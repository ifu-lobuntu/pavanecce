package org.pavanecce.cmmn.jbpm.planning;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.jbpm.services.task.impl.model.TaskImpl;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.TaskData;
import org.kie.internal.task.api.model.Deadlines;
import org.kie.internal.task.api.model.Delegation;
import org.kie.internal.task.api.model.SubTasksStrategy;

@Entity
public class PlannedTaskImpl implements InternalPlannedTask {

	private static final long serialVersionUID = 11123315412L;
	@Id()
	Long id;
	@ManyToOne
	TaskImpl task;
	@Column(name = "discretionary_item_id")
	private String discretionaryItemId;
	@Enumerated
	PlanningStatus planningStatus;
	@Basic
	private String planItemName;
	@Transient
	Map<String, Object> parameterOverrides;

	public PlannedTaskImpl() {

	}

	public PlannedTaskImpl(TaskImpl task) {
		this.task = task;
		this.id = task.getId();
		planItemName = task.getNames().get(0).getText();
		planningStatus = PlanningStatus.PLANNING_IN_PROGRESS;

	}

	public TaskImpl getTask() {
		return task;
	}

	public String getDiscretionaryItemId() {
		return discretionaryItemId;
	}

	@Override
	public void setDiscretionaryItemId(String tableItemId) {
		this.discretionaryItemId = tableItemId;
	}

	public PlanningStatus getPlanningStatus() {
		return planningStatus;
	}

	@Override
	public void setPlanningStatus(PlanningStatus planningStatus) {
		this.planningStatus = planningStatus;
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		task.writeExternal(out);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		task.readExternal(in);
	}

	public Long getId() {
		return task.getId();
	}

	public void setId(long id) {
		task.setId(id);
	}

	public Boolean isArchived() {
		return task.isArchived();
	}

	public void setArchived(Boolean archived) {
		task.setArchived(archived);
	}

	public int getVersion() {
		return task.getVersion();
	}

	public int getPriority() {
		return task.getPriority();
	}

	public void setPriority(int priority) {
		task.setPriority(priority);
	}

	public List<I18NText> getNames() {
		return task.getNames();
	}

	public void setNames(List<I18NText> names) {
		task.setNames(names);
	}

	public List<I18NText> getSubjects() {
		return task.getSubjects();
	}

	public void setSubjects(List<I18NText> subjects) {
		task.setSubjects(subjects);
	}

	public List<I18NText> getDescriptions() {
		return task.getDescriptions();
	}

	public void setDescriptions(List<I18NText> descriptions) {
		task.setDescriptions(descriptions);
	}

	public PeopleAssignments getPeopleAssignments() {
		return task.getPeopleAssignments();
	}

	public void setPeopleAssignments(PeopleAssignments peopleAssignments) {
		task.setPeopleAssignments(peopleAssignments);
	}

	public Delegation getDelegation() {
		return task.getDelegation();
	}

	public void setDelegation(Delegation delegation) {
		task.setDelegation(delegation);
	}

	public TaskData getTaskData() {
		return task.getTaskData();
	}

	public void setTaskData(TaskData taskData) {
		task.setTaskData(taskData);
	}

	public Deadlines getDeadlines() {
		return task.getDeadlines();
	}

	public void setDeadlines(Deadlines deadlines) {
		task.setDeadlines(deadlines);
	}

	public String getTaskType() {
		return task.getTaskType();
	}

	public void setTaskType(String taskType) {
		task.setTaskType(taskType);
	}

	public String getFormName() {
		return task.getFormName();
	}

	public void setFormName(String formName) {
		task.setFormName(formName);
	}

	public Short getArchived() {
		return task.getArchived();
	}

	public int hashCode() {
		return task.hashCode();
	}

	public boolean equals(Object obj) {
		return task.equals(obj);
	}

	public String toString() {
		return task.toString();
	}

	public SubTasksStrategy getSubTaskStrategy() {
		return task.getSubTaskStrategy();
	}

	public void setSubTaskStrategy(SubTasksStrategy subTaskStrategy) {
		task.setSubTaskStrategy(subTaskStrategy);
	}

	@Override
	public String getPlanItemName() {
		return planItemName;
	}

	@Override
	public void setPlanItemName(String planItemName) {
		this.planItemName = planItemName;
	}

	@Override
	public Map<String, Object> getParameterOverrides() {
		if (parameterOverrides == null) {
			parameterOverrides = new HashMap<String, Object>();
		}
		return parameterOverrides;
	}
}
