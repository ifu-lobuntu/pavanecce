package org.pavanecce.cmmn.jbpm.test;

import static org.kie.api.runtime.EnvironmentName.OBJECT_MARSHALLING_STRATEGIES;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.AnnotationMapperImpl;
import org.drools.core.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.jbpm.marshalling.impl.ProcessInstanceResolverStrategy;
import org.jbpm.marshalling.impl.ProcessMarshallerRegistry;
import org.jbpm.process.builder.ProcessNodeBuilderRegistry;
import org.jbpm.process.instance.ProcessInstanceFactoryRegistry;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.workflow.instance.impl.NodeInstanceFactoryRegistry;
import org.jbpm.workflow.instance.impl.factory.CreateNewNodeFactory;
import org.jbpm.workflow.instance.impl.factory.ReuseNodeFactory;
import org.kie.api.io.ResourceType;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.ContentMarshallerContext;
import org.kie.internal.task.api.InternalTaskService;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemDefinitionType;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.HumanTaskPlanItem;
import org.pavanecce.cmmn.jbpm.flow.JoiningSentry;
import org.pavanecce.cmmn.jbpm.flow.OnPart;
import org.pavanecce.cmmn.jbpm.flow.PlanItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.Sentry;
import org.pavanecce.cmmn.jbpm.flow.SimpleSentry;
import org.pavanecce.cmmn.jbpm.flow.StagePlanItem;
import org.pavanecce.cmmn.jbpm.flow.builder.CMMNBuilder;
import org.pavanecce.cmmn.jbpm.flow.builder.DefaultTypeMap;
import org.pavanecce.cmmn.jbpm.flow.builder.DefinitionsHandler;
import org.pavanecce.cmmn.jbpm.flow.builder.PlanItemBuilder;
import org.pavanecce.cmmn.jbpm.flow.builder.SentryBuilder;
import org.pavanecce.cmmn.jbpm.instance.AbstractSubscriptionManager;
import org.pavanecce.cmmn.jbpm.instance.CaseInstanceFactory;
import org.pavanecce.cmmn.jbpm.instance.CaseInstanceMarshaller;
import org.pavanecce.cmmn.jbpm.instance.HumanTaskPlanItemInstance;
import org.pavanecce.cmmn.jbpm.instance.OnPartInstance;
import org.pavanecce.cmmn.jbpm.instance.SentryInstance;
import org.pavanecce.cmmn.jbpm.instance.StagePlanItemInstance;
import org.pavanecce.cmmn.jbpm.instance.SubscriptionManager;
import org.pavanecce.cmmn.jbpm.jpa.CollectionPlaceHolderResolveStrategy;
import org.pavanecce.cmmn.jbpm.jpa.HibernateSubscriptionManager;
import org.pavanecce.cmmn.jbpm.ocm.OcmCaseFileItemSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.ocm.OcmCaseSubscriptionInfo;
import org.pavanecce.cmmn.jbpm.ocm.OcmCollectionPlaceHolderResolveStrategy;
import org.pavanecce.cmmn.jbpm.ocm.OcmPlaceHolderResolveStrategy;
import org.pavanecce.cmmn.jbpm.ocm.OcmSubscriptionManager;
import org.pavanecce.common.ObjectPersistence;
import org.pavanecce.common.jpa.JpaObjectPersistence;
import org.pavanecce.common.ocm.OcmFactory;
import org.pavanecce.common.ocm.OcmObjectPersistence;

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
	public void tearDown() throws Exception {
		super.tearDown();
		runtimeEngine = null;
		getPersistence().close();

	}

	protected void assertTaskTypeCreated(List<TaskSummary> list, String expected) {
		for (TaskSummary taskSummary : list) {
			if (taskSummary.getName().equals(expected)) {
				return;
			}
		}
		fail("Task not created: " + expected);
	}

	public ObjectPersistence getPersistence() {
		try {
			if (persistence == null) {
				if (isJpa) {
					persistence = new JpaObjectPersistence(getEmf());
				} else {
					OcmObjectPersistence ocmObjectPersistence = new OcmObjectPersistence(getOcmFactory());
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

	protected RuntimeManager createRuntimeManager(String... processFile) {
		DefinitionsHandler.registerTypeMap(CaseFileItemDefinitionType.UML_CLASS, new DefaultTypeMap());
		ProcessNodeBuilderRegistry.INSTANCE.register(StagePlanItem.class, new PlanItemBuilder());
		ProcessNodeBuilderRegistry.INSTANCE.register(HumanTaskPlanItem.class, new PlanItemBuilder());
		ProcessNodeBuilderRegistry.INSTANCE.register(SimpleSentry.class, new SentryBuilder());
		ProcessNodeBuilderRegistry.INSTANCE.register(JoiningSentry.class, new SentryBuilder());
		ProcessInstanceFactoryRegistry.INSTANCE.register(Case.class, new CaseInstanceFactory());
		CaseInstanceMarshaller m = new CaseInstanceMarshaller();
		ProcessMarshallerRegistry.INSTANCE.register(RuleFlowProcess.RULEFLOW_TYPE, m);
		RuntimeManager rm = super.createRuntimeManager(processFile);
		RuntimeEngine runtimeEngine = getRuntimeEngine();
		Environment env = runtimeEngine.getKieSession().getEnvironment();
		env.set(OBJECT_MARSHALLING_STRATEGIES, getPlaceholdStrategies(env));
		env.set(SubscriptionManager.ENV_NAME, getSubscriptionManager());
		if (!isJpa) {
			env.set(OcmFactory.OBJECT_CONTENT_MANAGER_FACTORY, getOcmFactory());
		}
		NodeInstanceFactoryRegistry nodeInstanceFactoryRegistry = NodeInstanceFactoryRegistry.getInstance(env);
		nodeInstanceFactoryRegistry.register(Sentry.class, new ReuseNodeFactory(SentryInstance.class));
		nodeInstanceFactoryRegistry.register(SimpleSentry.class, new ReuseNodeFactory(SentryInstance.class));
		nodeInstanceFactoryRegistry.register(JoiningSentry.class, new ReuseNodeFactory(SentryInstance.class));
		nodeInstanceFactoryRegistry.register(OnPart.class, new ReuseNodeFactory(OnPartInstance.class));
		nodeInstanceFactoryRegistry.register(CaseFileItemOnPart.class, new ReuseNodeFactory(OnPartInstance.class));
		nodeInstanceFactoryRegistry.register(PlanItemOnPart.class, new ReuseNodeFactory(OnPartInstance.class));
		nodeInstanceFactoryRegistry.register(StagePlanItem.class, new CreateNewNodeFactory(StagePlanItemInstance.class));
		nodeInstanceFactoryRegistry.register(HumanTaskPlanItem.class, new CreateNewNodeFactory(HumanTaskPlanItemInstance.class));
		TaskService ts = runtimeEngine.getTaskService();
		if (ts instanceof InternalTaskService) {
			((InternalTaskService) ts).addMarshallerContext(rm.getIdentifier(), new ContentMarshallerContext(env, getClass().getClassLoader()));
		}
		return rm;
	}

	protected ObjectMarshallingStrategy[] getPlaceholdStrategies(Environment env) {
		if (isJpa) {
			return new ObjectMarshallingStrategy[] { new ProcessInstanceResolverStrategy(), new JPAPlaceholderResolverStrategy(env), new CollectionPlaceHolderResolveStrategy(env),
					new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT) };
		} else {
			return new ObjectMarshallingStrategy[] { new ProcessInstanceResolverStrategy(), new OcmPlaceHolderResolveStrategy(env), new JPAPlaceholderResolverStrategy(env),
					new CollectionPlaceHolderResolveStrategy(env), new OcmCollectionPlaceHolderResolveStrategy(env),
					new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT) };

		}
	}

	protected AbstractSubscriptionManager<?, ?> getSubscriptionManager() {
		if (isJpa) {
			return new HibernateSubscriptionManager();
		} else {
			return (AbstractSubscriptionManager<?, ?>) getOcmFactory().getEventListener();
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
				ocmFactory = new OcmFactory(tr, "admin", "admin", new AnnotationMapperImpl(Arrays.<Class> asList(getClasses())), new OcmSubscriptionManager());
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

	protected abstract Class[] getClasses();
}