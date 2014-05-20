package org.pavanecce.cmmn.jbpm.planning;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.jbpm.services.task.impl.model.ContentImpl;
import org.jbpm.services.task.impl.model.OrganizationalEntityImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;

@Entity
public class PlannedItemImpl implements Serializable, PlannedItem {
	private static final long serialVersionUID = 11123315412L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "taskPlanSeq")
	@Column(name = "id")
	Long id;
	@Basic
	@Column(name = "case_instance_id")
	long caseInstanceId;
	@ManyToOne
	@Column(name = "plan_container_task_id")
	TaskImpl planContainerTask;
	@ManyToOne(optional = true)
	@Column(name = "resulting_task_id")
	TaskImpl resultingTaskId;
	@Basic
	@Column(name = "table_item_id")
	String tableItemId;
	@Basic
	String assignedActorId;
	@ManyToOne()
	OrganizationalEntityImpl actor;
	@Enumerated
	PlanningStatus planningStatus;
	@ManyToOne()
	ContentImpl content;
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public long getCaseInstanceId() {
		return caseInstanceId;
	}

	@Override
	public void setCaseInstanceId(long caseInstanceId) {
		this.caseInstanceId = caseInstanceId;
	}

	@Override
	public TaskImpl getPlanContainerTask() {
		return planContainerTask;
	}

	@Override
	public void setPlanContainerTask(Task planContainerTask) {
		this.planContainerTask = (TaskImpl) planContainerTask;
	}

	@Override
	public TaskImpl getResultingTask() {
		return resultingTaskId;
	}

	@Override
	public void setResultingTask(Task resultingTaskId) {
		this.resultingTaskId = (TaskImpl) resultingTaskId;
	}

	@Override
	public String getTableItemId() {
		return tableItemId;
	}

	@Override
	public void setTableItemId(String tableItemId) {
		this.tableItemId = tableItemId;
	}

	@Override
	public OrganizationalEntityImpl getActor() {
		return actor;
	}

	@Override
	public void setActor(OrganizationalEntity  actor) {
		this.actor = (OrganizationalEntityImpl) actor;
	}

	@Override
	public PlanningStatus getPlanningStatus() {
		return planningStatus;
	}

	@Override
	public void setPlanningStatus(PlanningStatus planningStatus) {
		this.planningStatus = planningStatus;
	}

	@Override
	public Content getContent() {
		return content;
	}

	@Override
	public void setContent(Content content) {
		this.content = (ContentImpl) content;
	}

}
