package org.pavanecce.cmmn.jbpm.planning;

import org.kie.api.task.model.Content;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;

public interface PlannedItem {

	Long getId();

	void setId(Long id);

	long getCaseInstanceId();

	void setCaseInstanceId(long caseInstanceId);

	Task getPlanContainerTask();

	void setPlanContainerTask(Task planContainerTask);

	void setResultingTask(Task resultingTaskId);

	String getTableItemId();

	void setTableItemId(String tableItemId);


	OrganizationalEntity getActor();

	void setActor(OrganizationalEntity actor);

	PlanningStatus getPlanningStatus();

	void setPlanningStatus(PlanningStatus planningStatus);

	Content getContent();

	void setContent(Content content);

	Task  getResultingTask();

}