package org.pavanecce.cmmn.jbpm.task;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.impl.TaskServiceEntryPointImpl;
import org.jbpm.services.task.impl.model.ContentDataImpl;
import org.jbpm.services.task.impl.model.ContentImpl;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.shared.services.api.JbpmServicesPersistenceManager;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.ContentMarshallerContext;

public abstract class AbstractTaskCommand<T> extends TaskCommand<T> {

	private static final long serialVersionUID = -6267126920414609188L;
	protected JbpmServicesPersistenceManager pm;
	protected TaskServiceEntryPointImpl ts;

	public AbstractTaskCommand(JbpmServicesPersistenceManager pm) {
		super();
		this.pm = pm;
	}

	public void init(TaskServiceEntryPointImpl ts) {
		// TODO Won't work in CDI
		try {
			this.ts = ts;
			if (pm == null) {
				Field pmField = ts.getTaskAdminService().getClass().getDeclaredField("pm");
				pmField.setAccessible(true);
				pm = (JbpmServicesPersistenceManager) pmField.get(ts.getTaskAdminService());
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Object getCorrectContentObject(Map<String, Object> parameters, String contentParamName) {
		Object contentObject = parameters.get(contentParamName);
		if (contentObject == null) {
			contentObject = new HashMap<String, Object>(parameters);
		}
		return contentObject;
	}

	protected long ensureContentPresent(Task task, long existingId, Map<String, Object> newContentAsMap, String contentNameInMap) {
		ContentImpl existingContent = null;
		ContentMarshallerContext mc = ts.getMarshallerContext(task);
		if (existingId < 0 || (existingContent = pm.find(ContentImpl.class, existingId)) == null) {
			Object correctContentObject = getCorrectContentObject(newContentAsMap, contentNameInMap);
			ContentDataImpl contentData = ContentMarshallerHelper.marshal(correctContentObject, mc.getEnvironment());
			ContentImpl newlyCreatedContent = new ContentImpl(contentData.getContent());
			pm.persist(newlyCreatedContent);
			return newlyCreatedContent.getId();
		} else {
			Object contentObjectToUpdateTaskWith = mergeContentObjectIfPossible(newContentAsMap, contentNameInMap, existingContent, mc);
			ContentDataImpl contentData = ContentMarshallerHelper.marshal(contentObjectToUpdateTaskWith, mc.getEnvironment());
			existingContent.setContent(contentData.getContent());
			return existingContent.getId();
		}
	}

	@SuppressWarnings("unchecked")
	private Object mergeContentObjectIfPossible(Map<String, Object> newContentAsMap, String contentNameInMap, ContentImpl existingContent,
			ContentMarshallerContext mc) {
		Object contentObjectToUpdateTaskWith = ContentMarshallerHelper.unmarshall(existingContent.getContent(), mc.getEnvironment());
		if (contentObjectToUpdateTaskWith instanceof Map) {
			Map<String, Object> existingOutputAsMap = (Map<String, Object>) contentObjectToUpdateTaskWith;
			Object newContentObject = getCorrectContentObject(newContentAsMap, contentNameInMap);
			if (newContentObject instanceof Map) {
				// merge
				existingOutputAsMap.putAll((Map<String, Object>) newContentObject);
			} else if (newContentObject != null) {
				// replace
				contentObjectToUpdateTaskWith = newContentObject;
			}
		} else {
			contentObjectToUpdateTaskWith = getCorrectContentObject(newContentAsMap, contentNameInMap);
		}
		return contentObjectToUpdateTaskWith;
	}

}