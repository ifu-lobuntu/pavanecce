package org.pavanecce.cmmn.jbpm.planning;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import java.util.List;

import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.model.InternalTaskSummary;
import org.kie.internal.task.api.model.SubTasksStrategy;

public class PlannedTaskSummaryImpl implements InternalPlannedTaskSummary {

	private static final long serialVersionUID = 11123315412L;
	private Long id;
	private InternalTaskSummary taskSummary;
	private String discretionaryItemId;
	private PlanningStatus planningStatus;
	private String planItemName;

	public PlannedTaskSummaryImpl() {

	}

	public PlannedTaskSummaryImpl(PlannedTaskImpl pt) {
		this.taskSummary = new TaskSummaryImpl(pt.getId(), pt.getTaskData().getProcessInstanceId(), pt.getNames().get(0).getText(), pt.getSubjects().get(0)
				.getText(), pt.getDescriptions().get(0).getText(), pt.getTaskData().getStatus(), pt.getPriority(), pt.getTaskData().isSkipable(), pt
				.getTaskData().getActualOwner(), pt.getTaskData().getCreatedBy(), pt.getTaskData().getCreatedOn(), pt.getTaskData().getActivationTime(), pt
				.getTaskData().getExpirationTime(), pt.getTaskData().getProcessId(), pt.getTaskData().getProcessSessionId(), pt.getSubTaskStrategy(), pt
				.getTaskData().getParentId());
		this.id = pt.getId();
		this.discretionaryItemId = pt.getDiscretionaryItemId();
		this.planItemName = pt.getPlanItemName();
		this.planningStatus = pt.getPlanningStatus();
	}

	public String getDiscretionaryItemId() {
		return discretionaryItemId;
	}

	public void setDiscretionaryItemId(String tableItemId) {
		this.discretionaryItemId = tableItemId;
	}

	public PlanningStatus getPlanningStatus() {
		return planningStatus;
	}

	@Override
	public String getPlanItemName() {
		return planItemName;
	}

	@Override
	public void setPlanItemName(String name) {
		this.planItemName = name;
	}

	public void setPlanningStatus(PlanningStatus planningStatus) {
		this.planningStatus = planningStatus;
	}

	public long getId() {
		return taskSummary.getId();
	}

	public long getProcessInstanceId() {
		return taskSummary.getProcessInstanceId();
	}

	public String getName() {
		return taskSummary.getName();
	}

	public String getSubject() {
		return taskSummary.getSubject();
	}

	public String getDescription() {
		return taskSummary.getDescription();
	}

	public Status getStatus() {
		return taskSummary.getStatus();
	}

	public int getPriority() {
		return taskSummary.getPriority();
	}

	public boolean isSkipable() {
		return taskSummary.isSkipable();
	}

	public User getActualOwner() {
		return taskSummary.getActualOwner();
	}

	public User getCreatedBy() {
		return taskSummary.getCreatedBy();
	}

	public Date getCreatedOn() {
		return taskSummary.getCreatedOn();
	}

	public Date getActivationTime() {
		return taskSummary.getActivationTime();
	}

	public Date getExpirationTime() {
		return taskSummary.getExpirationTime();
	}

	public String getProcessId() {
		return taskSummary.getProcessId();
	}

	public int getProcessSessionId() {
		return taskSummary.getProcessSessionId();
	}

	public List<String> getPotentialOwners() {
		return taskSummary.getPotentialOwners();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeObject(planningStatus);
		out.writeUTF(discretionaryItemId);
		out.writeUTF(planItemName);
		taskSummary.writeExternal(out);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id = in.readLong();
		planningStatus = (PlanningStatus) in.readObject();
		discretionaryItemId = in.readUTF();
		planItemName = in.readUTF();
		taskSummary = new TaskSummaryImpl();
		taskSummary.readExternal(in);
	}

	public void setId(long id) {
		taskSummary.setId(id);
	}

	public void setProcessInstanceId(long processInstanceId) {
		taskSummary.setProcessInstanceId(processInstanceId);
	}

	public void setName(String name) {
		taskSummary.setName(name);
	}

	public void setSubject(String subject) {
		taskSummary.setSubject(subject);
	}

	public void setDescription(String description) {
		taskSummary.setDescription(description);
	}

	public void setStatus(Status status) {
		taskSummary.setStatus(status);
	}

	public void setPriority(int priority) {
		taskSummary.setPriority(priority);
	}

	public void setSkipable(boolean skipable) {
		taskSummary.setSkipable(skipable);
	}

	public void setActualOwner(User actualOwner) {
		taskSummary.setActualOwner(actualOwner);
	}

	public void setCreatedBy(User createdBy) {
		taskSummary.setCreatedBy(createdBy);
	}

	public void setCreatedOn(Date createdOn) {
		taskSummary.setCreatedOn(createdOn);
	}

	public void setActivationTime(Date activationTime) {
		taskSummary.setActivationTime(activationTime);
	}

	public void setExpirationTime(Date expirationTime) {
		taskSummary.setExpirationTime(expirationTime);
	}

	public void setProcessId(String processId) {
		taskSummary.setProcessId(processId);
	}

	public void setProcessSessionId(int processSessionId) {
		taskSummary.setProcessSessionId(processSessionId);
	}

	public SubTasksStrategy getSubTaskStrategy() {
		return taskSummary.getSubTaskStrategy();
	}

	public void setSubTaskStrategy(SubTasksStrategy subTaskStrategy) {
		taskSummary.setSubTaskStrategy(subTaskStrategy);
	}

	public long getParentId() {
		return taskSummary.getParentId();
	}

	public void setParentId(long parentId) {
		taskSummary.setParentId(parentId);
	}

	public void setPotentialOwners(List<String> potentialOwners) {
		taskSummary.setPotentialOwners(potentialOwners);
	}

}
