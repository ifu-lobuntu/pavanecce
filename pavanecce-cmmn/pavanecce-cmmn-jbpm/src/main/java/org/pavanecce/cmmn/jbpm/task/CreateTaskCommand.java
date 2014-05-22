package org.pavanecce.cmmn.jbpm.task;

import javax.enterprise.util.AnnotationLiteral;
import javax.xml.bind.annotation.XmlElement;

import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.events.AfterTaskAddedEvent;
import org.jbpm.services.task.events.BeforeTaskAddedEvent;
import org.jbpm.services.task.impl.model.ContentImpl;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.model.ContentData;
import org.kie.internal.task.api.model.InternalTaskData;

public class CreateTaskCommand extends AbstractTaskCommand<Long> {

	private static final long serialVersionUID = 5765007237796573932L;
	private Task task;
	@XmlElement(name = "parameter")
	private ContentData params;

	public CreateTaskCommand(JbpmServicesPersistenceManager pm, Task task, ContentData content) {
		super(pm);
		this.task = task;
		this.params = content;
	}

	@SuppressWarnings("serial")
	public Long execute(Context cntxt) {
		TaskContext context = (TaskContext) cntxt;
		init(context.getTaskService());
		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<BeforeTaskAddedEvent>() {
		}).fire(task);
		ContentImpl content = new ContentImpl(params.getContent());
		pm.persist(content);
		((InternalTaskData) task.getTaskData()).setDocument(content.getId(), params);
		((InternalTaskData) task.getTaskData()).setStatus(Status.Created);
		pm.persist(task);
		ts.getTaskLifecycleEventListeners().select(new AnnotationLiteral<AfterTaskAddedEvent>() {
		}).fire(task);
		return (Long) task.getId();
	}
}
