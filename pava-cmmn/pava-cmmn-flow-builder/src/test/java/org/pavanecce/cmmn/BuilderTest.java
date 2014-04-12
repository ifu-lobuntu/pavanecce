package org.pavanecce.cmmn;

import static org.kie.api.runtime.EnvironmentName.OBJECT_MARSHALLING_STRATEGIES;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.drools.core.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.jbpm.marshalling.impl.ProcessInstanceResolverStrategy;
import org.jbpm.marshalling.impl.ProcessMarshallerRegistry;
import org.jbpm.process.instance.ProcessInstanceFactory;
import org.jbpm.process.instance.ProcessInstanceFactoryRegistry;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.workflow.instance.impl.NodeInstanceFactoryRegistry;
import org.jbpm.workflow.instance.impl.factory.ReuseNodeFactory;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.pavanecce.cmmn.flow.Case;
import org.pavanecce.cmmn.flow.CaseFileItemDefinitionType;
import org.pavanecce.cmmn.flow.Sentry;
import org.pavanecce.cmmn.flow.builder.CMMNBuilder;
import org.pavanecce.cmmn.flow.builder.DefaultTypeMap;
import org.pavanecce.cmmn.flow.builder.DefinitionsHandler;
import org.pavanecce.cmmn.instance.CaseInstanceFactory;
import org.pavanecce.cmmn.instance.CaseInstanceMarshaller;
import org.pavanecce.cmmn.instance.SentryInstance;
import org.pavanecce.cmmn.instance.SubscriptionManager;
import org.pavanecce.cmmn.jpa.CollectionPlaceHolderResolveStrategy;
import org.pavanecce.cmmn.jpa.HibernateSubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class BuilderTest extends JbpmJUnitBaseTestCase {
	private static final Logger logger = LoggerFactory.getLogger(BuilderTest.class);

	public BuilderTest() {
		super(true, true, "org.jbpm.persistence.jpa");
	}
    protected PoolingDataSource setupPoolingDataSource() {
        PoolingDataSource pds = new PoolingDataSource();
        pds.setClassName("org.h2.jdbcx.JdbcDataSource");
        pds.setUniqueName("jdbc/jbpm-ds");
//        pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        pds.setMaxPoolSize(5);
        pds.setAllowLocalTransactions(true);
        pds.getDriverProperties().put("user", "sa");
        pds.getDriverProperties().put("password", "");
        pds.getDriverProperties().put("URL", "jdbc:h2:mem:jbpm-db;MVCC=true");
        pds.init();
        return pds;
    }
    
	@Test
	public void testBuild() throws Exception {
		DefinitionsHandler.registerTypeMap(CaseFileItemDefinitionType.UML_CLASS, new DefaultTypeMap());
		ProcessInstanceFactoryRegistry.INSTANCE.register(Case.class, new CaseInstanceFactory());
		CaseInstanceMarshaller m = new CaseInstanceMarshaller();
		ProcessMarshallerRegistry.INSTANCE.register(RuleFlowProcess.RULEFLOW_TYPE, m);
		createRuntimeManager("test/hello.cmmn");
		
		RuntimeEngine runtimeEngine = getRuntimeEngine();
		Environment env = runtimeEngine.getKieSession().getEnvironment();
		env.set(OBJECT_MARSHALLING_STRATEGIES, new ObjectMarshallingStrategy[] { new ProcessInstanceResolverStrategy(),
				new JPAPlaceholderResolverStrategy(env), new CollectionPlaceHolderResolveStrategy(env),
				new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT) });
		env.set(SubscriptionManager.ENV_NAME, new HibernateSubscriptionManager());
		NodeInstanceFactoryRegistry nodeInstanceFactoryRegistry = NodeInstanceFactoryRegistry.getInstance(env);
		nodeInstanceFactoryRegistry.register(Sentry.class, new ReuseNodeFactory(SentryInstance.class));
		KieSession ksession = runtimeEngine.getKieSession();
		TaskService taskService = runtimeEngine.getTaskService();
		Map<String, Object> params = new HashMap<String, Object>();
		UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
		ut.begin();
		EntityManager em = getEmf().createEntityManager();

		HousePlan housePlan = new HousePlan();
		em.persist(housePlan);
		em.flush();
		ut.commit();
		params.put("housePlan", Arrays.asList(housePlan));
		ut.begin();
		ProcessInstance processInstance = ksession.startProcess("hello", params);
		ut.commit();
		assertProcessInstanceActive(processInstance.getId(), ksession);
		assertNodeTriggered(processInstance.getId(), "defaultSplit");
		// Sentries:
		assertNodeTriggered(processInstance.getId(), "WaitingForWallPlanCreated");
		assertNodeTriggered(processInstance.getId(), "WaitingForFoundationLaid");
		addWallPlan(ut, housePlan);
		addWallPlan(ut, housePlan);
		assertNodeTriggered(processInstance.getId(), "LayFoundationPlanItem");
		//TODO:
		//Check two tasks for builder
		//Check WaitingForWallPlanCreated still active
		//When all planItems complete, check the process completes
		// // let john execute Task 1
		// List<TaskSummary> list =
		// taskService.getTasksAssignedAsPotentialOwner("Builder", "en-UK");
		// TaskSummary task = list.get(0);
		// logger.info("John is executing task {}", task.getName());
		// taskService.start(task.getId(), "john");
		// taskService.complete(task.getId(), "john", null);
		//
		// assertNodeTriggered(processInstance.getId(), "End");
		// assertProcessInstanceCompleted(processInstance.getId(), ksession);
	}

	private void addWallPlan(UserTransaction ut, HousePlan housePlan) throws NotSupportedException, SystemException, RollbackException,
			HeuristicMixedException, HeuristicRollbackException {
		EntityManager em;
		ut.begin();
		em = getEmf().createEntityManager();
		housePlan = em.find(HousePlan.class, housePlan.getId());
		housePlan.getWallPlans().add(new WallPlan());
		em.flush();
		ut.commit();
	}

	protected RuntimeManager createRuntimeManager(Strategy strategy, String identifier, String... process) {
		Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
		for (String p : process) {
			resources.put(p, CMMNBuilder.CMMN_RESOURCE_TYPE);
		}
		return createRuntimeManager(strategy, resources, identifier);
	}
}
