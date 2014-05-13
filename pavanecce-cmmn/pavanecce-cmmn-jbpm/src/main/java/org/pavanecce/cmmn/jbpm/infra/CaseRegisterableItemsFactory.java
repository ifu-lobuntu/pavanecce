package org.pavanecce.cmmn.jbpm.infra;

import org.jbpm.runtime.manager.impl.DefaultRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.jbpm.services.task.wih.ExternalTaskEventListener;
import org.jbpm.services.task.wih.LocalHTWorkItemHandler;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.internal.runtime.manager.Disposable;
import org.kie.internal.runtime.manager.DisposeListener;
import org.kie.internal.task.api.EventService;

public class CaseRegisterableItemsFactory extends DefaultRegisterableItemsFactory {
	@SuppressWarnings({"unchecked","rawtypes"})
	@Override
	protected WorkItemHandler getHTWorkItemHandler(RuntimeEngine runtime) {
		ExternalTaskEventListener listener = new CaseTaskLifecycleListener();
		listener.setRuntimeManager(((RuntimeEngineImpl) runtime).getManager());

		LocalHTWorkItemHandler humanTaskHandler = new CaseTaskWorkItemHandler();
		humanTaskHandler.setRuntimeManager(((RuntimeEngineImpl) runtime).getManager());
		if (runtime.getTaskService() instanceof EventService) {
			((EventService) runtime.getTaskService()).registerTaskLifecycleEventListener(listener);
		}

		if (runtime instanceof Disposable) {
			((Disposable) runtime).addDisposeListener(new DisposeListener() {

				@Override
				public void onDispose(RuntimeEngine runtime) {
					if (runtime.getTaskService() instanceof EventService) {
						((EventService) runtime.getTaskService()).clearTaskLifecycleEventListeners();
						((EventService) runtime.getTaskService()).clearTasknotificationEventListeners();
					}
				}
			});
		}
		return humanTaskHandler;
	}
}
