package org.pavanecce.cmmn.jbpm.task;

import java.util.Map;

import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.impl.model.ContentDataImpl;
import org.jbpm.services.task.impl.model.ContentImpl;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.task.model.Task;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.ContentMarshallerContext;
import org.kie.internal.task.api.model.InternalTaskData;

public class SetTaskOutputCommand extends AbstractTaskCommand<Long> {

	private static final long serialVersionUID = 5765007237796573932L;
	private Map<String, Object> outputAsMap;

	public SetTaskOutputCommand(JbpmServicesPersistenceManager pm, long taskId, Map<String, Object> params) {
		super(pm);
		this.taskId = taskId;
		this.outputAsMap = params;
	}

	public Long execute(Context cntxt) {
		TaskContext context = (TaskContext) cntxt;
		init(context.getTaskService());
		Task task = ts.getTaskById(taskId);
		ContentMarshallerContext mc = ts.getMarshallerContext(task);
		InternalTaskData itd = (InternalTaskData) task.getTaskData();
		ContentImpl existingOutput = pm.find(ContentImpl.class, itd.getOutputContentId());
		if (existingOutput == null) {
			ContentDataImpl outputContentData = ContentMarshallerHelper.marshal(outputAsMap, mc.getEnvironment());
			ContentImpl content = new ContentImpl(outputContentData.getContent());
			pm.persist(content);
			itd.setOutputContentId(content.getId());
		} else {
			Map<String,Object> existingOutputAsMap= (Map<String, Object>) ContentMarshallerHelper.unmarshall(existingOutput.getContent(), mc.getEnvironment());
			existingOutputAsMap.putAll(outputAsMap);
			ContentDataImpl outputContentData = ContentMarshallerHelper.marshal(outputAsMap, mc.getEnvironment());
			existingOutput.setContent(outputContentData.getContent());
		}
		return (Long) task.getId();
	}
}
