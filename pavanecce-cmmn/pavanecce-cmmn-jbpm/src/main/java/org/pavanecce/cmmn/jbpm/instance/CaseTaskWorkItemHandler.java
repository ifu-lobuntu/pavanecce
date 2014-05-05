package org.pavanecce.cmmn.jbpm.instance;

import java.util.HashMap;

import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.services.task.wih.LocalHTWorkItemHandler;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.task.api.model.ContentData;

public class CaseTaskWorkItemHandler extends LocalHTWorkItemHandler {
    protected ContentData createTaskContentBasedOnWorkItemParams(KieSession session, WorkItem workItem) {
        ContentData content = null;
        Object contentObject = workItem.getParameter("Content");
        if (contentObject == null) {
            contentObject = new HashMap<String, Object>(workItem.getParameters());
        }
        if (contentObject != null) {
            Environment env = null;
            if(session != null){
                env=session.getEnvironment();
            }
            content = ContentMarshallerHelper.marshal(contentObject, env);
        }
        return content;
    }
    
}
