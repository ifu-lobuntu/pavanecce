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
import org.pavanecce.cmmn.jbpm.flow.TimerEventListener;
import org.pavanecce.cmmn.jbpm.flow.UserEventListener;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseTaskPlanItemInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.HumanTaskPlanItemInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.MilestonePlanItemInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.StagePlanItemInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.TimerEventPlanItemInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.UserEventPlanItemInstance;

public final class DelegatingNodeFactory implements NodeInstanceFactory {
	Map<Class<?>, NodeInstanceFactory> registry = new HashMap<Class<?>, NodeInstanceFactory>();
	public DelegatingNodeFactory(){
		registry.put(UserEventListener.class, new ReuseNodeFactory(UserEventPlanItemInstance.class));
		registry.put(TimerEventListener.class, new ReuseNodeFactory(TimerEventPlanItemInstance.class));
		registry.put(Milestone.class, new CreateNewNodeFactory(MilestonePlanItemInstance.class));
		registry.put(Stage.class, new CreateNewNodeFactory(StagePlanItemInstance.class));
		registry.put(HumanTask.class, new CreateNewNodeFactory(HumanTaskPlanItemInstance.class));
		registry.put(CaseTask.class, new CreateNewNodeFactory(CaseTaskPlanItemInstance.class));
	}

	@Override
	public NodeInstance getNodeInstance(Node node, WorkflowProcessInstance processInstance, org.kie.api.runtime.process.NodeInstanceContainer nodeInstanceContainer) {
		ItemWithDefinition<?> di = (ItemWithDefinition<?>) node;
		return registry.get(di.getDefinition().getClass()).getNodeInstance(node, processInstance, nodeInstanceContainer);
	}
	public void addDelegate(Class<? extends PlanItemDefinition> d, NodeInstanceFactory f){
		registry.put(d, f);
	}
}