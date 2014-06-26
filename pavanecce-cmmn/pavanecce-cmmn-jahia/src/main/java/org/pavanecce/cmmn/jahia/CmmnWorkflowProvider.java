package org.pavanecce.cmmn.jahia;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.impl.EnvironmentFactory;
import org.drools.core.process.instance.WorkItemHandler;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.TransactionManager;
import org.jahia.pipelines.Pipeline;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.HistoryWorkflow;
import org.jahia.services.workflow.HistoryWorkflowTask;
import org.jahia.services.workflow.Workflow;
import org.jahia.services.workflow.WorkflowAction;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowObservationManager;
import org.jahia.services.workflow.WorkflowTask;
import org.jahia.services.workflow.jbpm.AbstractPeopleAssignmentValve;
import org.jahia.services.workflow.jbpm.BaseCommand;
import org.jahia.services.workflow.jbpm.JBPM6WorkflowProvider;
import org.jahia.services.workflow.jbpm.JBPMListener;
import org.jahia.services.workflow.jbpm.JahiaKModuleRegisterableItemsFactory;
import org.jahia.services.workflow.jbpm.JahiaRuntimeManagerFactoryImpl;
import org.jahia.services.workflow.jbpm.JahiaUserGroupCallback;
import org.jahia.services.workflow.jbpm.command.AbortProcessCommand;
import org.jahia.services.workflow.jbpm.command.AddCommentCommand;
import org.jahia.services.workflow.jbpm.command.AssignTaskCommand;
import org.jahia.services.workflow.jbpm.command.CompleteTaskCommand;
import org.jahia.services.workflow.jbpm.command.GetActiveWorkflowsInformationsCommand;
import org.jahia.services.workflow.jbpm.command.GetAvailableActionsCommand;
import org.jahia.services.workflow.jbpm.command.GetAvailableWorkflowsCommand;
import org.jahia.services.workflow.jbpm.command.GetHistoryWorkflowCommand;
import org.jahia.services.workflow.jbpm.command.GetHistoryWorkflowTasksCommand;
import org.jahia.services.workflow.jbpm.command.GetHistoryWorkflowsForNodeCommand;
import org.jahia.services.workflow.jbpm.command.GetHistoryWorkflowsForPathCommand;
import org.jahia.services.workflow.jbpm.command.GetTasksForUserCommand;
import org.jahia.services.workflow.jbpm.command.GetWorkflowCommand;
import org.jahia.services.workflow.jbpm.command.GetWorkflowDefinitionCommand;
import org.jahia.services.workflow.jbpm.command.GetWorkflowTaskCommand;
import org.jahia.services.workflow.jbpm.command.GetWorkflowsForDefinitionCommand;
import org.jahia.services.workflow.jbpm.command.GetWorkflowsForUserCommand;
import org.jahia.services.workflow.jbpm.command.StartProcessCommand;
import org.jahia.services.workflow.jbpm.custom.AbstractTaskLifeCycleEventListener;
import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
import org.jbpm.shared.services.impl.JbpmServicesPersistenceManagerImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.task.api.EventService;
import org.kie.spring.persistence.KieSpringJpaManager;
import org.kie.spring.persistence.KieSpringTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

public class CmmnWorkflowProvider extends JBPM6WorkflowProvider {
	private transient static Logger logger = LoggerFactory.getLogger(CmmnWorkflowProvider.class);
	private transient static CmmnWorkflowProvider instance = new CmmnWorkflowProvider();

	private WorkflowObservationManager observationManager;
	private JahiaUserManagerService userManager;
	private JahiaGroupManagerService groupManager;
	private KieRepository kieRepository;
	private KieServices kieServices;
	private KieFileSystem kieFileSystem;
	private JBPMListener listener = new JBPMListener(this);
	private RuntimeManager runtimeManager;
	private RuntimeEngine runtimeEngine;
	private AbstractPlatformTransactionManager platformTransactionManager;
	private EntityManagerFactory emf;
	private EntityManager sharedEm;
	private JbpmServicesPersistenceManagerImpl jbpmServicesPersistenceManager;
	private Map<String, WorkItemHandler> workItemHandlers = new TreeMap<String, WorkItemHandler>();
	private Map<String, AbstractTaskLifeCycleEventListener> taskLifeCycleEventListeners = new TreeMap<String, AbstractTaskLifeCycleEventListener>();
	private Pipeline peopleAssignmentPipeline;
	private JahiaUserGroupCallback jahiaUserGroupCallback;
	private KieContainer kieContainer;
	private TransactionManager transactionManager;
	private ThreadLocal<Boolean> loop = new ThreadLocal<Boolean>();

	public static JBPM6WorkflowProvider getInstance() {
		logger.info("CmmnWorkflowProvider.getInstance()");
		return instance;
	}

	public void setWorkflowObservationManager(WorkflowObservationManager observationManager) {
		this.observationManager = observationManager;
		listener.setObservationManager(observationManager);
	}

	public void setGroupManager(JahiaGroupManagerService groupManager) {
		this.groupManager = groupManager;
	}

	public void setUserManager(JahiaUserManagerService userManager) {
		this.userManager = userManager;
	}

	public void setPeopleAssignmentPipeline(Pipeline peopleAssignmentPipeline) {
		this.peopleAssignmentPipeline = peopleAssignmentPipeline;
	}

	public void setJahiaUserGroupCallback(JahiaUserGroupCallback jahiaUserGroupCallback) {
		this.jahiaUserGroupCallback = jahiaUserGroupCallback;
	}

	public KieRepository getKieRepository() {
		return kieRepository;
	}

	public void setPlatformTransactionManager(AbstractPlatformTransactionManager platformTransactionManager) {
		this.platformTransactionManager = platformTransactionManager;
	}

	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.emf = emf;
	}

	public void setSharedEntityManager(EntityManager em) {
		this.sharedEm = em;
	}

	public void setJbpmServicesPersistenceManager(JbpmServicesPersistenceManagerImpl jbpmServicesPersistenceManager) {
		this.jbpmServicesPersistenceManager = jbpmServicesPersistenceManager;
	}

	public synchronized void registerWorkItemHandler(String name, WorkItemHandler workItemHandler) {
		logger.info("CmmnWorkflowProvider.registerWorkItemHandler():" + name + "=" + workItemHandler.getClass().getName());
		synchronized (getWorkflowService()) {
			workItemHandlers.put(name, workItemHandler);
			if (runtimeEngine != null) {
				runtimeEngine.getKieSession().getWorkItemManager().registerWorkItemHandler(name, workItemHandler);
			}
		}
	}

	public WorkItemHandler unregisterWorkItemHandler(String name) {
		synchronized (getWorkflowService()) {
			if (runtimeEngine != null) {
				runtimeEngine.getKieSession().getWorkItemManager().registerWorkItemHandler(name, null);
			}
			return workItemHandlers.remove(name);
		}
	}

	public void registerTaskLifeCycleEventListener(String name, AbstractTaskLifeCycleEventListener taskAssignmentListener) {
		taskLifeCycleEventListeners.put(name, taskAssignmentListener);
	}

	public AbstractTaskLifeCycleEventListener unregisterTaskLifeCycleEventListener(String name) {
		return taskLifeCycleEventListeners.remove(name);
	}

	public void start() {
		kieServices = KieServices.Factory.get();
		kieRepository = kieServices.getRepository();
		kieFileSystem = kieServices.newKieFileSystem();
		recompilePackages();
		getWorkflowService().addProvider(this);
	}

	public void stop() {
		getWorkflowService().removeProvider(this);
		runtimeManager.close();
	}

	@Override
	public List<WorkflowDefinition> getAvailableWorkflows(final Locale uiLocale) {
		logger.info("CmmnWorkflowProvider.getAvailableWorkflows()");
		return executeCommand(new GetAvailableWorkflowsCommand(uiLocale));
	}

	@Override
	public WorkflowDefinition getWorkflowDefinitionByKey(final String key, final Locale uiLocale) {
		logger.info("CmmnWorkflowProvider.getWorkflowDefinitionByKey()");
		return executeCommand(new GetWorkflowDefinitionCommand(key, uiLocale));
	}

	@Override
	public List<Workflow> getActiveWorkflowsInformations(final List<String> processIds, final Locale uiLocale) {
		logger.info("CmmnWorkflowProvider.getActiveWorkflowsInformations()");
		return executeCommand(new GetActiveWorkflowsInformationsCommand(processIds, uiLocale));
	}

	@Override
	public String startProcess(final String processKey, final Map<String, Object> args) {
		logger.info("CmmnWorkflowProvider.startProcess()");
		return executeCommand(new StartProcessCommand(processKey, args));
	}

	@Override
	public void abortProcess(final String processId) {
		logger.info("CmmnWorkflowProvider.abortProcess()");
		executeCommand(new AbortProcessCommand(processId));
	}

	@Override
	public Workflow getWorkflow(final String processId, final Locale uiLocale) {
		return executeCommand(new GetWorkflowCommand(processId, uiLocale));
	}

	@Override
	public Set<WorkflowAction> getAvailableActions(final String processId, final Locale uiLocale) {
		return executeCommand(new GetAvailableActionsCommand(processId, uiLocale));
	}

	@Override
	public List<WorkflowTask> getTasksForUser(final JahiaUser user, final Locale uiLocale) {
		logger.info("CmmnWorkflowProvider.getTasksForUser()");
		return executeCommand(new GetTasksForUserCommand(user, uiLocale));
	}

	@Override
	public List<Workflow> getWorkflowsForDefinition(final String definition, final Locale uiLocale) {
		return executeCommand(new GetWorkflowsForDefinitionCommand(definition, uiLocale));
	}

	@Override
	public List<Workflow> getWorkflowsForUser(final JahiaUser user, final Locale uiLocale) {
		return executeCommand(new GetWorkflowsForUserCommand(user, uiLocale));
	}

	@Override
	public void assignTask(final String taskId, final JahiaUser user) {
		if (loop.get() != null) {
			return;
		}
		try {
			loop.set(Boolean.TRUE);
			executeCommand(new AssignTaskCommand(taskId, user));
		} finally {
			loop.set(null);
		}
	}

	@Override
	public void completeTask(final String taskId, final JahiaUser jahiaUser, final String outcome, final Map<String, Object> args) {

		if (loop.get() != null) {
			return;
		}
		try {
			loop.set(Boolean.TRUE);
			executeCommand(new CompleteTaskCommand(taskId, outcome, args, jahiaUser, observationManager));
		} finally {
			loop.set(null);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void addComment(final String processId, final String comment, final String user) {
		executeCommand(new AddCommentCommand(processId, comment, user));
	}

	@Override
	public WorkflowTask getWorkflowTask(final String taskId, final Locale uiLocale) {
		return executeCommand(new GetWorkflowTaskCommand(taskId, uiLocale));
	}

	@Override
	public List<HistoryWorkflow> getHistoryWorkflowsForNode(final String nodeId, final Locale uiLocale) {
		return executeCommand(new GetHistoryWorkflowsForNodeCommand(nodeId, uiLocale));
	}

	@Override
	public List<HistoryWorkflow> getHistoryWorkflowsForPath(final String path, final Locale uiLocale) {
		return executeCommand(new GetHistoryWorkflowsForPathCommand(path, uiLocale));
	}

	@Override
	public List<HistoryWorkflow> getHistoryWorkflows(final List<String> processIds, final Locale uiLocale) {
		return executeCommand(new GetHistoryWorkflowCommand(processIds, uiLocale));
	}

	@Override
	public List<HistoryWorkflowTask> getHistoryWorkflowTasks(final String processId, final Locale uiLocale) {
		return executeCommand(new GetHistoryWorkflowTasksCommand(processId, uiLocale));
	}

	public void addResource(Resource kieResource) throws IOException {
		synchronized (getWorkflowService()) {
			kieFileSystem.write(kieServices.getResources().newUrlResource(kieResource.getURL()));
		}
	}

	public void removeResource(Resource kieResource) throws IOException {
		synchronized (getWorkflowService()) {
			kieFileSystem.delete(kieResource.getURL().getPath());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void recompilePackages() {
		synchronized (getWorkflowService()) {
			long timer = System.currentTimeMillis();
			KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
			kieBuilder.buildAll();

			kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());

			transactionManager = new KieSpringTransactionManager(platformTransactionManager);

			PersistenceContextManager persistenceContextManager = createKieSpringContextManager(transactionManager);
			RuntimeEnvironment runtimeEnvironment = RuntimeEnvironmentBuilder.getDefault().entityManagerFactory(emf)
					.addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, transactionManager)
					.addEnvironmentEntry(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, persistenceContextManager).knowledgeBase(kieContainer.getKieBase())
					.classLoader(kieContainer.getClassLoader())
					.registerableItemsFactory(new JahiaKModuleRegisterableItemsFactory(kieContainer, null, peopleAssignmentPipeline))
					.userGroupCallback(jahiaUserGroupCallback).get();
			if (runtimeManager != null) {
				runtimeManager.close();
			}

			final JahiaRuntimeManagerFactoryImpl runtimeFactory = JahiaRuntimeManagerFactoryImpl.getInstance();
			runtimeFactory.setJbpmServicesPersistenceManager(jbpmServicesPersistenceManager);

			// Use singleton runtime manager - one manager/session/taskservice for all requests
			runtimeManager = runtimeFactory.newSingletonRuntimeManager(runtimeEnvironment);
			runtimeEngine = runtimeManager.getRuntimeEngine(EmptyContext.get());

			KieSession kieSession = runtimeEngine.getKieSession();

			for (Map.Entry<String, WorkItemHandler> workItemHandlerEntry : workItemHandlers.entrySet()) {
				kieSession.getWorkItemManager().registerWorkItemHandler(workItemHandlerEntry.getKey(), workItemHandlerEntry.getValue());
			}

			for (Map.Entry<String, AbstractTaskLifeCycleEventListener> taskLifeCycleEventListenerEntry : taskLifeCycleEventListeners.entrySet()) {
				AbstractTaskLifeCycleEventListener taskLifeCycleEventListener = taskLifeCycleEventListenerEntry.getValue();
				taskLifeCycleEventListener.setEnvironment(kieSession.getEnvironment());
				taskLifeCycleEventListener.setObservationManager(observationManager);
				taskLifeCycleEventListener.setTaskService(runtimeEngine.getTaskService());
				if (runtimeEngine.getTaskService() instanceof EventService) {
					((EventService) runtimeEngine.getTaskService()).registerTaskLifecycleEventListener(taskLifeCycleEventListener);
				}
			}

			Map<String, Object> pipelineEnvironment = new HashMap<String, Object>();
			pipelineEnvironment.put(AbstractPeopleAssignmentValve.ENV_JBPM_WORKFLOW_PROVIDER, this);
			peopleAssignmentPipeline.setEnvironment(pipelineEnvironment);

			kieSession.addEventListener(new JBPMListener(this));

			logger.info("Rebuilding KIE base took {} ms", System.currentTimeMillis() - timer);
		}
	}

	private PersistenceContextManager createKieSpringContextManager(TransactionManager transactionManager) {
		Environment env = EnvironmentFactory.newEnvironment();
		env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
		env.set(EnvironmentName.APP_SCOPED_ENTITY_MANAGER, sharedEm);

		/** Put app EM as cmd-shared **/
		env.set(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, sharedEm);
		env.set("IS_SHARED_ENTITY_MANAGER", true);
		/*****/

		env.set("IS_JTA_TRANSACTION", false);

		env.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
		PersistenceContextManager persistenceContextManager = new KieSpringJpaManager(env);
		env.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, persistenceContextManager);
		return persistenceContextManager;
	}

	private <T> T executeCommand(BaseCommand<T> t) {
		t.setRuntimeEngine(runtimeEngine);
		t.setEm(sharedEm);
		t.setPersistenceManager(jbpmServicesPersistenceManager);
		t.setGroupManager(groupManager);
		t.setUserManager(userManager);
		t.setWorkflowService(getWorkflowService());
		t.setKey(getKey());
		CommandBasedStatefulKnowledgeSession s = (CommandBasedStatefulKnowledgeSession) runtimeEngine.getKieSession();
		return s.execute(t);
	}
}
