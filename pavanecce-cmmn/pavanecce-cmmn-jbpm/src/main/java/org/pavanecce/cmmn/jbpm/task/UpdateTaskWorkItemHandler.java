package org.pavanecce.cmmn.jbpm.task;

import org.drools.core.process.instance.WorkItemHandler;
import org.jbpm.services.task.wih.LocalHTWorkItemHandler;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateTaskWorkItemHandler implements WorkItemHandler {
    private static final Logger logger = LoggerFactory.getLogger(UpdateTaskWorkItemHandler.class);
    private RuntimeManager runtimeManager;
    
    public RuntimeManager getRuntimeManager() {
        return runtimeManager;
    }

    public void setRuntimeManager(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }
   
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

}
