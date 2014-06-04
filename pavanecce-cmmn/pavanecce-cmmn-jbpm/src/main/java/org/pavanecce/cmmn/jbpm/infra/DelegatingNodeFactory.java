package org.pavanecce.cmmn.jbpm.infra;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.impl.NodeInstanceFactory;
import org.jbpm.workflow.instance.impl.factory.CreateNewNodeFactory;
import org.jbpm.workflow.instance.impl.factory.ReuseNodeFactory;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.flow.CaseTask;
import org.pavanecce.cmmn.jbpm.flow.HumanTask;
import org.pavanecce.cmmn.jbpm.flow.ItemWithDefinition;
import org.pavanecce.cmmn.jbpm.flow.Milestone;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.Stage;
import org.pavanecce.cmmn.jbpm.flow.TimerEvent;
import org.pavanecce.cmmn.jbpm.flow.UserEvent;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseTaskInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.HumanTaskInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.MilestoneInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.StageInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.TimerEventInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.UserEventInstance;

public final class DelegatingNodeFactory implements NodeInstanceFactory {
	Map<Class<?>, NodeInstanceFactory> registry = new HashMap<Class<?>, NodeInstanceFactory>();

	public DelegatingNodeFactory() {
		registry.put(UserEvent.class, new ReuseNodeFactory(UserEventInstance.class));
		registry.put(TimerEvent.class, new ReuseNodeFactory(TimerEventInstance.class));
		registry.put(Milestone.class, new CreateNewNodeFactory(MilestoneInstance.class));
		registry.put(Stage.class, new CreateNewNodeFactory(StageInstance.class));
		registry.put(HumanTask.class, new CreateNewNodeFactory(HumanTaskInstance.class));
		registry.put(CaseTask.class, new CreateNewNodeFactory(CaseTaskInstance.class));
	}

	@Override
	public NodeInstance getNodeInstance(Node node, WorkflowProcessInstance processInstance,
			org.kie.api.runtime.process.NodeInstanceContainer nodeInstanceContainer) {
		ItemWithDefinition<?> di = (ItemWithDefinition<?>) node;
		return registry.get(di.getDefinition().getClass()).getNodeInstance(node, processInstance, nodeInstanceContainer);
	}

	public void addDelegate(Class<? extends PlanItemDefinition> d, NodeInstanceFactory f) {
		registry.put(d, f);
	}
}