package org.pavanecce.cmmn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.marshalling.impl.ProcessMarshallerFactory;
import org.jbpm.marshalling.impl.ProcessMarshallerRegistry;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.test.JbpmJUnitBaseTestCase.Strategy;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.pavanecce.cmmn.flow.builder.CMMNBuilder;
import org.pavanecce.cmmn.instance.CaseInstanceMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuilderTest extends JbpmJUnitBaseTestCase {
	private static final Logger logger = LoggerFactory.getLogger(BuilderTest.class);

	public BuilderTest() {
		super(true, true);
	}

	@Test
	public void testBuild() throws Exception {
		ProcessMarshallerRegistry.INSTANCE.register(RuleFlowProcess.RULEFLOW_TYPE, new CaseInstanceMarshaller());
		createRuntimeManager("test/hello.cmmn");
		RuntimeEngine runtimeEngine = getRuntimeEngine();
		KieSession ksession = runtimeEngine.getKieSession();
		TaskService taskService = runtimeEngine.getTaskService();

		ProcessInstance processInstance = ksession.startProcess("hello");

		assertProcessInstanceActive(processInstance.getId(), ksession);
		assertNodeTriggered(processInstance.getId(), "Start", "Task 1");

		// let john execute Task 1
		List<TaskSummary> list = taskService.getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		TaskSummary task = list.get(0);
		logger.info("John is executing task {}", task.getName());
		taskService.start(task.getId(), "john");
		taskService.complete(task.getId(), "john", null);

		assertNodeTriggered(processInstance.getId(), "End");
		assertProcessInstanceCompleted(processInstance.getId(), ksession);
	}

	protected RuntimeManager createRuntimeManager(Strategy strategy, String identifier, String... process) {
		Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
		for (String p : process) {
			resources.put(p, CMMNBuilder.CMMN_RESOURCE_TYPE);
		}
		return createRuntimeManager(strategy, resources, identifier);
	}
}
