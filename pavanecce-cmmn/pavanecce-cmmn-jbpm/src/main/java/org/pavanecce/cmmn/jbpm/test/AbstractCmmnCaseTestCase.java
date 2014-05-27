package org.pavanecce.cmmn.jbpm.test;

import static org.kie.api.runtime.EnvironmentName.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.AnnotationMapperImpl;
import org.drools.core.audit.event.LogEvent;
import org.drools.core.audit.event.RuleFlowNodeLogEvent;
import org.drools.core.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.jbpm.marshalling.impl.ProcessInstanceResolverStrategy;
import org.jbpm.marshalling.impl.ProcessMarshallerRegistry;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.builder.ProcessNodeBuilderRegistry;
import org.jbpm.process.instance.ProcessInstanceFactoryRegistry;
import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.jbpm.services.task.identity.PropertyUserInfoImpl;
import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.shared.services.impl.JbpmServicesPersistenceManagerImpl;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.impl.NodeInstanceFactoryRegistry;
import org.jbpm.workflow.instance.impl.factory.CreateNewNodeFactory;
import org.jbpm.workflow.instance.impl.factory.ReuseNodeFactory;
import org.junit.After;
import org.kie.api.io.ResourceType;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.task.api.ContentMarshallerContext;
import org.kie.internal.task.api.InternalTaskService;
import org.pavanecce.cmmn.jbpm.event.AbstractPersistentSubscriptionManager;
import org.pavanecce.cmmn.jbpm.event.SubscriptionManager;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemDefinitionType;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemStartTrigger;
import org.pavanecce.cmmn.jbpm.flow.CaseTaskPlanItem;
import org.pavanecce.cmmn.jbpm.flow.DefaultJoin;
import org.pavanecce.cmmn.jbpm.flow.DefaultSplit;
import org.pavanecce.cmmn.jbpm.flow.DiscretionaryItem;
import org.pavanecce.cmmn.jbpm.flow.HumanTaskPlanItem;
import org.pavanecce.cmmn.jbpm.flow.MilestonePlanItem;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemInstanceFactoryNode;
import org.pavanecce.cmmn.jbpm.flow.PlanItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemStartTrigger;
import org.pavanecce.cmmn.jbpm.flow.Sentry;
import org.pavanecce.cmmn.jbpm.flow.StagePlanItem;
import org.pavanecce.cmmn.jbpm.flow.TimerEventPlanItem;
import org.pavanecce.cmmn.jbpm.flow.UserEventPlanItem;
import org.pavanecce.cmmn.jbpm.infra.CaseInstanceFactory;
import org.pavanecce.cmmn.jbpm.infra.CaseInstanceMarshaller;
import org.pavanecce.cmmn.jbpm.infra.CaseRegisterableItemsFactory;
import org.pavanecce.cmmn.jbpm.infra.DelegatingNodeFactory;
import org.pavanecce.cmmn.jbpm.infra.PlanItemBuilder;
import org.pavanecce.cmmn.jbpm.infra.SentryBuilder;
import org.pavanecce.cmmn.jbpm.jpa.CollectionPlaceHolderResolveStrategy;
import org.pavanecce.cmmn.jbpm.jpa.HibernateSubscriptionManager;
import org.pavanecce.cmmn.jbpm.jpa.JpaCasePersistence;
import org.pavanecce.cmmn.jbpm.jpa.JpaPlaceHolderResolverStrategy;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.CaseInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.DefaultJoinInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.DefaultSplitInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.OnPartInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.PlanItemInstanceFactoryNodeInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.SentryInstance;
import org.pavanecce.cmmn.jbpm.lifecycle.impl.StageInstance;
import org.pavanecce.cmmn.jbpm.ocm.OcmCasePersistence;
import org.pavanecce.cmmn.jbpm.ocm.OcmCollectionPlaceHolderResolveStrategy;
import org.pavanecce.cmmn.jbpm.ocm.OcmPlaceHolderResolveStrategy;
import org.pavanecce.cmmn.jbpm.ocm.OcmSubscriptionManager;
import org.pavanecce.cmmn.jbpm.xml.handler.CMMNBuilder;
import org.pavanecce.cmmn.jbpm.xml.handler.DefaultTypeMap;
import org.pavanecce.cmmn.jbpm.xml.handler.DefinitionsHandler;
import org.pavanecce.common.ObjectPersistence;
import org.pavanecce.common.jpa.JpaObjectPersistence;
import org.pavanecce.common.ocm.OcmFactory;
import org.pavanecce.common.ocm.OcmObjectPersistence;
import org.pavanecce.common.util.FileUtil;

//import test.ConstructionCase;
//import test.House;
//import test.HousePlan;
//import test.RoofPlan;
//import test.RoomPlan;
//import test.Wall;
//import test.WallPlan;
import bitronix.tm.resource.jdbc.PoolingDataSource;

public abstract class AbstractCmmnCaseTestCase extends JbpmJUnitBaseTestCase {
	ObjectPersistence persistence;
	protected boolean isJpa = false;
	private OcmFactory ocmFactory;
	private RuntimeEngine runtimeEngine;
	private UserTransaction transaction;
	private RuntimeManager runtimeManager;
	public AbstractCmmnCaseTestCase() {
		super();
	}

	public AbstractCmmnCaseTestCase(boolean setupDataSource, boolean sessionPersistence) {
		super(setupDataSource, sessionPersistence);
	}

	public AbstractCmmnCaseTestCase(boolean setupDataSource, boolean sessionPersistence, String persistenceUnitName) {
		super(setupDataSource, sessionPersistence, persistenceUnitName);
	}

	@Override
	public void assertNodeActive(long processInstanceId, KieSession ksession, String... name) {
		super.assertNodeActive(processInstanceId, ksession, name);
	}

	protected CaseInstance reloadCaseInstance(CaseInstance caseInstance2) {
		return (CaseInstance) getRuntimeEngine().getKieSession().getProcessInstance(caseInstance2.getId());
	}

	protected void assertNodeNotTriggered(long processInstanceId, String... nodeNames) {
		getPersistence().start();
		List<String> names = removeNodesTriggered(processInstanceId, nodeNames);
		if (names.isEmpty()) {
			String s = names.get(0);
			for (int i = 1; i < names.size(); i++) {
				s += ", " + names.get(i);
			}
			fail("Node(s) executed: " + s);
		}
		getPersistence().commit();
	}

	protected InternalTaskService getTaskService() {
		return (InternalTaskService) getRuntimeEngine().getTaskService();
	}

	@Override
	public void assertNodeTriggered(long processInstanceId, String... nodeNames) {
		getPersistence().start();
		List<String> names = removeNodesTriggered(processInstanceId, nodeNames);
		if (!names.isEmpty()) {
			String s = names.get(0);
			for (int i = 1; i < names.size(); i++) {
				s += ", " + names.get(i);
			}
			fail("Node(s) not executed: " + s);
		}
		getPersistence().commit();
	}

	private List<String> removeNodesTriggered(long processInstanceId, String... nodeNames) {
		List<String> names = new ArrayList<String>();
		for (String nodeName : nodeNames) {
			names.add(nodeName);
		}
		if (sessionPersistence) {
			List<NodeInstanceLog> logs = getLogService().findNodeInstances(processInstanceId);
			if (logs != null) {
				for (NodeInstanceLog l : logs) {
					String nodeName = l.getNodeName();
					if ((l.getType() == NodeInstanceLog.TYPE_ENTER || l.getType() == NodeInstanceLog.TYPE_EXIT) && names.contains(nodeName)) {
						names.remove(nodeName);
					}
				}
			}
		} else {
			for (LogEvent event : getInMemoryLogger().getLogEvents()) {
				if (event instanceof RuleFlowNodeLogEvent) {
					String nodeName = ((RuleFlowNodeLogEvent) event).getNodeName();
					if (names.contains(nodeName)) {
						names.remove(nodeName);
					}
				}
			}
		}
		return names;
	}

	class StateResult {
		int count = 0;
		String foundState = "";
	}

	public void assertPlanItemInState(long processInstanceId, String planItemName, PlanElementState s, int... numberOfTimes) {
		getPersistence().start();
		NodeInstanceContainer ci = (NodeInstanceContainer) getRuntimeEngine().getKieSession().getProcessInstance(processInstanceId);

		StateResult sr = new StateResult();
		countItemInState(planItemName, s, ci, sr);
		getPersistence().commit();
		if (numberOfTimes.length == 0) {
			if (sr.count == 0) {
				assertTrue(planItemName + " should be in state " + s.name() + " but was in " + sr.foundState, sr.count > 0);
			}
		} else {
			assertEquals(planItemName + " should be in state " + s.name() + "  " + numberOfTimes[0] + " times, but was foudn in state " + sr.count + " times", numberOfTimes[0], sr.count);
		}
	}

	public void countItemInState(String planItemName, PlanElementState s, NodeInstanceContainer ci, StateResult sr) {
		for (NodeInstance ni : ci.getNodeInstances()) {
			if (ni instanceof PlanItemInstanceFactoryNodeInstance) {
				PlanItemInstanceFactoryNode node = (PlanItemInstanceFactoryNode) ni.getNode();
				if (node.getItemToInstantiate().getName().equals(planItemName)) {
					PlanItemInstanceFactoryNodeInstance<?> piil = (PlanItemInstanceFactoryNodeInstance<?>) ni;
					if (piil.isPlanItemInstanceStillRequired() && s == PlanElementState.AVAILABLE) {
						sr.count++;
					} else if (piil.getPlanElementState() == s) {
						sr.count++;
					} else {
						sr.foundState = piil.getPlanElementState().name();
					}
				}
			} else if (ni instanceof StageInstance) {
				countItemInState(planItemName, s, (StageInstance) ni, sr);
			}
		}
		if (sr.count == 0) {
			for (NodeInstance ni : ci.getNodeInstances()) {
				if (ni instanceof PlanItemInstance && ni.getNodeName().equals(planItemName)) {
					if (((PlanItemInstance<?>) ni).getPlanElementState() == s) {
						sr.count++;
					} else {
						sr.foundState = ((PlanItemInstance<?>) ni).getPlanElementState().name();
					}
				} else if (ni instanceof StageInstance) {
					countItemInState(planItemName, s, (StageInstance) ni, sr);
				}

			}
		}
	}

	public UserTransaction getTransaction() throws NamingException {
		if (transaction == null) {
			transaction = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
		}
		return transaction;
	}

	@After
	public void tearDown() throws Exception {
		try {
			getTransaction().rollback();
		} catch (Exception e) {

		}
		transaction = null;
		super.tearDown();
		runtimeEngine = null;

		getPersistence().close();
		persistence = null;

		Field fl = JbpmServicesPersistenceManagerImpl.class.getDeclaredField("noScopeEmLocal");
		fl.setAccessible(true);
		ThreadLocal<?> l = (ThreadLocal<?>) fl.get(null);
		l.set(null);
		FileUtil.deleteRoot(new File("jbpm-db.h2.db"));
		FileUtil.deleteRoot(new File("jbpm-db.trace.db"));
		// FileUtil.deleteRoot(new File("btm1.tlog"));
		// FileUtil.deleteRoot(new File("btm2.tlog"));

	}

	protected void assertTaskTypeCreated(List<TaskSummary> list, String expected, int... numberOfTimes) {
		int count = 0;
		for (TaskSummary taskSummary : list) {
			if (taskSummary.getName().equals(expected)) {
				count++;
			}
		}
		if (numberOfTimes.length == 1) {
			assertEquals("Task not created the correct number of times", numberOfTimes[0], count);
		} else if (count == 0) {
			fail("Task not created: " + expected);
		}
	}

	public ObjectPersistence getPersistence() {
		try {
			if (persistence == null) {
				if (isJpa) {
					persistence = new JpaCasePersistence(getEmf(),runtimeManager);
				} else {
					OcmObjectPersistence ocmObjectPersistence = new OcmCasePersistence(getOcmFactory(),runtimeManager);
					persistence = ocmObjectPersistence;
				}
			}
			return persistence;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected PoolingDataSource setupPoolingDataSource() {
		PoolingDataSource pds = new PoolingDataSource();
		pds.setClassName("org.h2.jdbcx.JdbcDataSource");
		pds.setUniqueName("jdbc/jbpm-ds");
		// pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
		pds.setMaxPoolSize(5);
		pds.setAllowLocalTransactions(true);
		pds.getDriverProperties().put("user", "sa");
		pds.setApplyTransactionTimeout(false);
		pds.getDriverProperties().put("password", "");
		pds.getDriverProperties().put("URL", "jdbc:h2:mem:jbpm-db;MVCC=true");
		pds.init();
		return pds;
	}

	@Override
	protected RuntimeEngine getRuntimeEngine() {
		if (this.runtimeEngine == null) {
			this.runtimeEngine = super.getRuntimeEngine();
		}
		return this.runtimeEngine;
	}

	@Override
	protected RuntimeManager createRuntimeManager(Strategy strategy, String identifier, String... process) {
		Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
		for (String p : process) {
			if (p.endsWith(".cmmn")) {
				resources.put(p, CMMNBuilder.CMMN_RESOURCE_TYPE);
			} else if (p.endsWith(".bpmn")) {
				resources.put(p, ResourceType.BPMN2);
			}
		}
		return createRuntimeManager(strategy, resources, identifier);
	}

	protected RuntimeManager createRuntimeManager(Strategy strategy, Map<String, ResourceType> resources, String identifier) {
		if (manager != null) {
			throw new IllegalStateException("There is already one RuntimeManager active");
		}

		RuntimeEnvironmentBuilder builder = null;
		if (!setupDataSource) {
			builder = RuntimeEnvironmentBuilder.Factory.get().newEmptyBuilder().registerableItemsFactory(new CaseRegisterableItemsFactory())
					.addConfiguration("drools.processSignalManagerFactory", DefaultSignalManagerFactory.class.getName())
					.addConfiguration("drools.processInstanceManagerFactory", DefaultProcessInstanceManagerFactory.class.getName());
		} else if (sessionPersistence) {
			builder = RuntimeEnvironmentBuilder.Factory.get().newDefaultBuilder().registerableItemsFactory(new CaseRegisterableItemsFactory()).entityManagerFactory(getEmf());
		} else {
			builder = RuntimeEnvironmentBuilder.Factory.get().newDefaultInMemoryBuilder();
		}
		builder.userGroupCallback(new JBossUserGroupCallbackImpl("classpath:/usergroups.properties"));

		for (Map.Entry<String, ResourceType> entry : resources.entrySet()) {
			builder.addAsset(ResourceFactory.newClassPathResource(entry.getKey()), entry.getValue());
		}

		return createRuntimeManager(strategy, resources, builder.get(), identifier);
	}

	@Override
	protected RuntimeManager createRuntimeManager(String... processFile) {
		DefinitionsHandler.registerTypeMap(CaseFileItemDefinitionType.UML_CLASS, new DefaultTypeMap());
		ProcessNodeBuilderRegistry.INSTANCE.register(UserEventPlanItem.class, new PlanItemBuilder());
		ProcessNodeBuilderRegistry.INSTANCE.register(TimerEventPlanItem.class, new PlanItemBuilder());
		ProcessNodeBuilderRegistry.INSTANCE.register(StagePlanItem.class, new PlanItemBuilder());
		ProcessNodeBuilderRegistry.INSTANCE.register(CaseTaskPlanItem.class, new PlanItemBuilder());
		ProcessNodeBuilderRegistry.INSTANCE.register(MilestonePlanItem.class, new PlanItemBuilder());
		ProcessNodeBuilderRegistry.INSTANCE.register(HumanTaskPlanItem.class, new PlanItemBuilder());
		ProcessNodeBuilderRegistry.INSTANCE.register(Sentry.class, new SentryBuilder());
		ProcessInstanceFactoryRegistry.INSTANCE.register(Case.class, new CaseInstanceFactory());
		CaseInstanceMarshaller m = new CaseInstanceMarshaller();
		ProcessMarshallerRegistry.INSTANCE.register(RuleFlowProcess.RULEFLOW_TYPE, m);
		RuntimeManager rm = super.createRuntimeManager(processFile);
		this.runtimeManager=rm;
		RuntimeEngine runtimeEngine = getRuntimeEngine();
		Environment env = runtimeEngine.getKieSession().getEnvironment();
		env.set(OBJECT_MARSHALLING_STRATEGIES, getPlaceholdStrategies(env));
		AbstractPersistentSubscriptionManager<?, ?> subscriptionManager = getSubscriptionManager();
		if (subscriptionManager != null) {
			env.set(SubscriptionManager.ENV_NAME, subscriptionManager);
		}
		if (isJpa) {
			env.set(JpaObjectPersistence.ENV_NAME, getPersistence());

		} else {
			env.set(OcmFactory.OBJECT_CONTENT_MANAGER_FACTORY, getOcmFactory());
		}
		NodeInstanceFactoryRegistry nodeInstanceFactoryRegistry = NodeInstanceFactoryRegistry.getInstance(env);
		nodeInstanceFactoryRegistry.register(DefaultJoin.class, new ReuseNodeFactory(DefaultJoinInstance.class));
		nodeInstanceFactoryRegistry.register(Sentry.class, new ReuseNodeFactory(SentryInstance.class));
		nodeInstanceFactoryRegistry.register(PlanItemInstanceFactoryNode.class, new ReuseNodeFactory(PlanItemInstanceFactoryNodeInstance.class));
		nodeInstanceFactoryRegistry.register(OnPart.class, new ReuseNodeFactory(OnPartInstance.class));
		nodeInstanceFactoryRegistry.register(CaseFileItemOnPart.class, new ReuseNodeFactory(OnPartInstance.class));
		nodeInstanceFactoryRegistry.register(CaseFileItemStartTrigger.class, new ReuseNodeFactory(OnPartInstance.class));
		nodeInstanceFactoryRegistry.register(PlanItemStartTrigger.class, new ReuseNodeFactory(OnPartInstance.class));
		nodeInstanceFactoryRegistry.register(PlanItemOnPart.class, new ReuseNodeFactory(OnPartInstance.class));
		nodeInstanceFactoryRegistry.register(StagePlanItem.class, new DelegatingNodeFactory());
		nodeInstanceFactoryRegistry.register(DefaultSplit.class, new CreateNewNodeFactory(DefaultSplitInstance.class));
		nodeInstanceFactoryRegistry.register(HumanTaskPlanItem.class, new DelegatingNodeFactory());
		nodeInstanceFactoryRegistry.register(CaseTaskPlanItem.class, new DelegatingNodeFactory());
		nodeInstanceFactoryRegistry.register(DiscretionaryItem.class, new DelegatingNodeFactory());
		nodeInstanceFactoryRegistry.register(UserEventPlanItem.class, new DelegatingNodeFactory());
		nodeInstanceFactoryRegistry.register(TimerEventPlanItem.class, new DelegatingNodeFactory());
		nodeInstanceFactoryRegistry.register(MilestonePlanItem.class, new DelegatingNodeFactory());
		TaskService ts = runtimeEngine.getTaskService();
		if (ts instanceof InternalTaskService) {
			InternalTaskService its = (InternalTaskService) ts;
			its.addMarshallerContext(rm.getIdentifier(), new ContentMarshallerContext(env, getClass().getClassLoader()));
			its.setUserInfo(new PropertyUserInfoImpl());
		}
		// for some reason the task service does not persist the users and groups ???
		populateUsers();
		return rm;
	}

	protected void populateUsers() {
		JBossUserGroupCallbackImpl users = new JBossUserGroupCallbackImpl("classpath:/usergroups.properties");
		Properties props = buildUserGroupProperties();
		for (Object userId : props.keySet()) {
			getPersistence().start();
			EntityManager em = getEmf().createEntityManager();
			GroupImpl group = em.find(GroupImpl.class, userId);
			if (group == null) {
				UserImpl builder = em.find(UserImpl.class, userId);
				if (builder == null) {
					em.persist(new UserImpl((String) userId));
					em.flush();
				}
				for (String g : users.getGroupsForUser((String) userId, null, null)) {
					group = em.find(GroupImpl.class, g);
					if (group == null) {
						em.persist(new GroupImpl(g));
						em.flush();
					}
				}
			}
			getPersistence().commit();
		}
	}

	private Properties buildUserGroupProperties() {
		Properties props = new Properties();
		try {
			props.load(getClass().getClassLoader().getResourceAsStream("usergroups.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}

	protected ObjectMarshallingStrategy[] getPlaceholdStrategies(Environment env) {
		if (isJpa) {
			return new ObjectMarshallingStrategy[] { new ProcessInstanceResolverStrategy(), new JpaPlaceHolderResolverStrategy(env), new CollectionPlaceHolderResolveStrategy(env),
					new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT) };
		} else {
			return new ObjectMarshallingStrategy[] { new ProcessInstanceResolverStrategy(), new OcmPlaceHolderResolveStrategy(env), new JpaPlaceHolderResolverStrategy(env),
					new OcmCollectionPlaceHolderResolveStrategy(env),new CollectionPlaceHolderResolveStrategy(env),
					new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT) };

		}
	}

	protected AbstractPersistentSubscriptionManager<?, ?> getSubscriptionManager() {
		if (isJpa) {
			return new HibernateSubscriptionManager();
		} else {
			return (AbstractPersistentSubscriptionManager<?, ?>) getOcmFactory().getEventListener();
		}
	}

	@SuppressWarnings("rawtypes")
	protected OcmFactory getOcmFactory() {
		if (this.ocmFactory == null) {
			try {
				TransientRepository tr = new TransientRepository();
				Session session;
				session = tr.login(new SimpleCredentials("admin", "admin".toCharArray()));
				session.getRootNode().addNode("cases");
				session.getRootNode().addNode("subscriptions");
				CndImporter.registerNodeTypes(new InputStreamReader(AbstractCmmnCaseTestCase.class.getResourceAsStream("/META-INF/definitions.cnd")), session);
				CndImporter.registerNodeTypes(new InputStreamReader(AbstractCmmnCaseTestCase.class.getResourceAsStream("/test.cnd")), session);
				session.save();
				session.logout();
				ocmFactory = new OcmFactory(tr, "admin", "admin", new AnnotationMapperImpl(Arrays.<Class> asList(getClasses())), new OcmSubscriptionManager(runtimeManager));
				OcmSubscriptionManager eventListener = (OcmSubscriptionManager) ocmFactory.getEventListener();
				eventListener.setOcmFactory(ocmFactory);
				return ocmFactory;
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return ocmFactory;
	}

	@SuppressWarnings("rawtypes")
	protected abstract Class[] getClasses();
}