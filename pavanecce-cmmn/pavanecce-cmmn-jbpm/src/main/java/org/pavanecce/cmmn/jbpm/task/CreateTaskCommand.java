package org.pavanecce.cmmn.jbpm.task;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.util.AnnotationLiteral;
import javax.xml.bind.annotation.XmlElement;

import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.events.AfterTaskAddedEvent;
import org.jbpm.services.task.events.BeforeTaskAddedEvent;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.model.InternalTaskData;

public class CreateTaskCommand extends AbstractTaskCommand<Long> {

	private static final long serialVersionUID = 5765007237796573932L;
	private Task task;
	@XmlElement(name = "parameter")
	private Map<String, Object> inputParameters;

	public CreateTaskCommand(JbpmServicesPersistenceManager pm, Task task, Map<String, Object> inputParmaeters) {
		super(pm);
		this.task = task;
		this.inputParameters = inputParmaeters;
	}

	@SuppressWarnings("serial")
	public Long execute(Context cntxt) {
		TaskContext context = (TaskContext) cntxt;
		init(context.getTaskService());
		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<BeforeTaskAddedEvent>() {
		}).fire(task);
		((InternalTaskData) task.getTaskData()).setStatus(Status.Created);
		((InternalTaskData) task.getTaskData()).setDocumentContentId(ensureContentPresent(task, -1, inputParameters, "Content"));
		((InternalTaskData) task.getTaskData()).setOutputContentId(ensureContentPresent(task, -1, new HashMap<String, Object>(), "Outpupt"));
		pm.persist(task);
		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<AfterTaskAddedEvent>() {
		}).fire(task);
		return (Long) task.getId();
	}
}
