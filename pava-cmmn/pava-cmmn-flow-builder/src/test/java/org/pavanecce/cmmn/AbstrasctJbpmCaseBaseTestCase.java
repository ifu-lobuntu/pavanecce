package org.pavanecce.cmmn;

import static org.kie.api.runtime.EnvironmentName.OBJECT_MARSHALLING_STRATEGIES;

import org.drools.core.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.jbpm.marshalling.impl.ProcessInstanceResolverStrategy;
import org.jbpm.marshalling.impl.ProcessMarshallerRegistry;
import org.jbpm.process.instance.ProcessInstanceFactoryRegistry;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.jbpm.workflow.instance.impl.NodeInstanceFactoryRegistry;
import org.jbpm.workflow.instance.impl.factory.CreateNewNodeFactory;
import org.jbpm.workflow.instance.impl.factory.ReuseNodeFactory;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.pavanecce.cmmn.flow.Case;
import org.pavanecce.cmmn.flow.CaseFileItemDefinitionType;
import org.pavanecce.cmmn.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.flow.JoiningSentry;
import org.pavanecce.cmmn.flow.OnPart;
import org.pavanecce.cmmn.flow.PlanItem;
import org.pavanecce.cmmn.flow.PlanItemOnPart;
import org.pavanecce.cmmn.flow.Sentry;
import org.pavanecce.cmmn.flow.SimpleSentry;
import org.pavanecce.cmmn.flow.builder.DefaultTypeMap;
import org.pavanecce.cmmn.flow.builder.DefinitionsHandler;
import org.pavanecce.cmmn.instance.CaseInstanceFactory;
import org.pavanecce.cmmn.instance.CaseInstanceMarshaller;
import org.pavanecce.cmmn.instance.OnPartInstance;
import org.pavanecce.cmmn.instance.PlanItemInstance;
import org.pavanecce.cmmn.instance.SentryInstance;
import org.pavanecce.cmmn.instance.SubscriptionManager;
import org.pavanecce.cmmn.jpa.CollectionPlaceHolderResolveStrategy;
import org.pavanecce.cmmn.jpa.HibernateSubscriptionManager;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public abstract class AbstrasctJbpmCaseBaseTestCase extends JbpmJUnitBaseTestCase {

	public AbstrasctJbpmCaseBaseTestCase() {
		super();
	}

	public AbstrasctJbpmCaseBaseTestCase(boolean setupDataSource, boolean sessionPersistence) {
		super(setupDataSource, sessionPersistence);
	}

	public AbstrasctJbpmCaseBaseTestCase(boolean setupDataSource, boolean sessionPersistence, String persistenceUnitName) {
		super(setupDataSource, sessionPersistence, persistenceUnitName);
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

	protected RuntimeManager createRuntimeManager(String ...processFile ) {
		DefinitionsHandler.registerTypeMap(CaseFileItemDefinitionType.UML_CLASS, new DefaultTypeMap());
		ProcessInstanceFactoryRegistry.INSTANCE.register(Case.class, new CaseInstanceFactory());
		CaseInstanceMarshaller m = new CaseInstanceMarshaller();
		ProcessMarshallerRegistry.INSTANCE.register(RuleFlowProcess.RULEFLOW_TYPE, m);
		RuntimeManager createRuntimeManager = super.createRuntimeManager(processFile);
		RuntimeEngine runtimeEngine = getRuntimeEngine();
		Environment env = runtimeEngine.getKieSession().getEnvironment();
		env.set(OBJECT_MARSHALLING_STRATEGIES, new ObjectMarshallingStrategy[] { new ProcessInstanceResolverStrategy(),
				new JPAPlaceholderResolverStrategy(env), new CollectionPlaceHolderResolveStrategy(env),
				new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT) });
		env.set(SubscriptionManager.ENV_NAME, new HibernateSubscriptionManager());
		NodeInstanceFactoryRegistry nodeInstanceFactoryRegistry = NodeInstanceFactoryRegistry.getInstance(env);
		nodeInstanceFactoryRegistry.register(Sentry.class, new ReuseNodeFactory(SentryInstance.class));
		nodeInstanceFactoryRegistry.register(SimpleSentry.class, new ReuseNodeFactory(SentryInstance.class));
		nodeInstanceFactoryRegistry.register(JoiningSentry.class, new ReuseNodeFactory(SentryInstance.class));
		nodeInstanceFactoryRegistry.register(OnPart.class, new ReuseNodeFactory(OnPartInstance.class));
		nodeInstanceFactoryRegistry.register(CaseFileItemOnPart.class, new ReuseNodeFactory(OnPartInstance.class));
		nodeInstanceFactoryRegistry.register(PlanItemOnPart.class, new ReuseNodeFactory(OnPartInstance.class));
		nodeInstanceFactoryRegistry.register(PlanItem.class, new  CreateNewNodeFactory(PlanItemInstance.class));
		return createRuntimeManager;
	}

}