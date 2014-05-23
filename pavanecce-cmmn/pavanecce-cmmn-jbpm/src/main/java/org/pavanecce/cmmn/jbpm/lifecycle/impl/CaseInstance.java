package org.pavanecce.cmmn.jbpm.lifecycle.impl;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.core.WorkItemHandlerNotFoundException;
import org.drools.core.process.core.Work;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.drools.core.spi.ProcessContext;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.services.task.wih.util.PeopleAssignmentHelper;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.runtime.process.EventListener;
import org.kie.api.runtime.process.NodeInstance;
import org.pavanecce.cmmn.jbpm.TaskParameters;
import org.pavanecce.cmmn.jbpm.event.SubscriptionManager;
import org.pavanecce.cmmn.jbpm.flow.Case;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItem;
import org.pavanecce.cmmn.jbpm.flow.CaseFileItemOnPart;
import org.pavanecce.cmmn.jbpm.flow.CaseParameter;
import org.pavanecce.cmmn.jbpm.flow.DiscretionaryItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItem;
import org.pavanecce.cmmn.jbpm.flow.PlanItemContainer;
import org.pavanecce.cmmn.jbpm.flow.PlanItemDefinition;
import org.pavanecce.cmmn.jbpm.flow.PlanItemTransition;
import org.pavanecce.cmmn.jbpm.flow.PlanningTable;
import org.pavanecce.cmmn.jbpm.flow.Role;
import org.pavanecce.cmmn.jbpm.flow.TableItem;
import org.pavanecce.cmmn.jbpm.flow.TaskDefinition;
import org.pavanecce.cmmn.jbpm.infra.OnPartInstanceSubscription;
import org.pavanecce.cmmn.jbpm.lifecycle.CaseInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.ControllableItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.ItemInstanceLifecycle;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementLifecycleWithTask;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementState;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanElementWithPlanningTable;
import org.pavanecce.cmmn.jbpm.lifecycle.PlanItemInstanceContainerLifecycle;
import org.pavanecce.common.ObjectPersistence;

public class CaseInstance extends RuleFlowProcessInstance implements CaseInstanceLifecycle {
	private static final long serialVersionUID = 8715128915363796623L;
	private boolean shouldUpdateSubscriptions;
	private transient int signalCount = 0;
	private PlanElementState planElementState = PlanElementState.ACTIVE;
	private long workItemId = -1;
	transient private WorkItem workItem;

	public Case getCase() {
		return (Case) getProcess();
	}

	public void addRoleAssignment(String role, String userId) {
		getRoleAssignments(role).add(userId);
	}

	@SuppressWarnings("unchecked")
	public Collection<String> getRoleAssignments(String role) {
		Collection<String> var = (Collection<String>) getVariable(role);
		if (var == null) {
			var = new HashSet<String>();
			setVariable(role, var);
		}
		return var;
	}

	@Override
	public void reconnect() {
		super.reconnect();

	}

	@Override
	public void disconnect() {
		super.disconnect();
	}

	public WorkItem createPlannedItem(long containerWorkItemId, String tableItemId) {
		org.jbpm.workflow.instance.NodeInstance contextNodeInstance = null;
		PlanElementWithPlanningTable pewpt = findPlanElementWithPlanningTable(containerWorkItemId);
		if (pewpt.getPlanningTable() != null) {
			if (pewpt == this) {
				contextNodeInstance = getNodeInstance(getCase().getDefaultJoin());
			} else {
				contextNodeInstance = (org.jbpm.workflow.instance.NodeInstance) pewpt;
			}
			DiscretionaryItem<? extends PlanItemDefinition> di = pewpt.getPlanningTable().getDiscretionaryItemById(tableItemId);
			Work work = di.getWork();
			PlanItemDefinition definition = di.getDefinition();
			WorkItemImpl workItem = new WorkItemImpl();
			workItem.setName(work.getName());
			workItem.setProcessInstanceId(contextNodeInstance.getProcessInstance().getId());
			workItem.setParameters(new HashMap<String, Object>(work.getParameters()));
			if (definition instanceof TaskDefinition) {
				workItem.getParameters().putAll(PlanItemInstanceUtil.buildInputParameters(work, contextNodeInstance, (TaskDefinition) definition));
			}
			CaseInstance caseInstance = (CaseInstance) contextNodeInstance.getProcessInstance();
			String deploymentId = (String) caseInstance.getKnowledgeRuntime().getEnvironment().get("deploymentId");
			workItem.setDeploymentId(deploymentId);
			workItem.setParameter(TaskParameters.COMMENT, definition.getDescription());
			if (contextNodeInstance.getNodeInstanceContainer() instanceof PlanElementLifecycleWithTask) {
				long parentWorkItemId = ((PlanElementLifecycleWithTask) contextNodeInstance.getNodeInstanceContainer()).getWorkItemId();
				if (parentWorkItemId >= 0) {
					workItem.setParameter(TaskParameters.PARENT_WORK_ITEM_ID, parentWorkItemId);
				}
			}
			workItem.setParameter(TaskParameters.PLANNED, Boolean.TRUE);
			workItem.setParameter(TaskParameters.DISCRETIONARY_ITEM_ID, tableItemId);
			return executeWorkItem(workItem);
		}
		return null;
	}

	public PlanElementWithPlanningTable findPlanElementWithPlanningTable(long containerWorkItemId) {
		PlanElementWithPlanningTable pewpt = null;
		if (containerWorkItemId == getWorkItemId()) {
			pewpt = this;
		} else {
			for (NodeInstance ni : getNodeInstances(true)) {
				if (ni instanceof PlanElementWithPlanningTable) {
					pewpt = (PlanElementWithPlanningTable) ni;
					if (pewpt.getWorkItemId() == containerWorkItemId) {
						break;
					}
				}
			}
		}
		return pewpt;
	}

	@Override
	public PlanningTable getPlanningTable() {
		return getCase().getPlanningTable();

	}

	public void markSubscriptionsForUpdate() {
		this.shouldUpdateSubscriptions = true;
	}

	public void checkSubs() {
		for (NodeInstance nodeInstance : getNodeInstances()) {
			if (nodeInstance instanceof CaseTaskPlanItemInstance) {
				try {
					Field elf = WorkflowProcessInstanceImpl.class.getDeclaredField("eventListeners");
					elf.setAccessible(true);
					Map<String, List<EventListener>> eventListeners = (Map<String, List<EventListener>>) elf.get(this);
					if (!eventListeners.get(TaskParameters.WORK_ITEM_UPDATED).contains(nodeInstance)) {
						System.err.println();
					}
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void signalEvent(String type, Object event) {
		checkSubs();
		signalCount++;
		if (type.equals("workItemUpdated") && isMyWorkItem((WorkItem) event)) {
			this.workItem = (WorkItem) event;
			PlanItemTransition transition = (PlanItemTransition) workItem.getResult(TaskParameters.TRANSITION);
			if (getPlanElementState().isTerminalState() && transition == PlanItemTransition.TERMINATE) {
				System.out.println("ignore - called from task service: " + TaskParameters.TRANSITION);
			} else if (transition == PlanItemTransition.COMPLETE) {
				if (canComplete()) {
					transition.invokeOn(this);
				} else {
					// TODO what now?
				}
			} else {
				transition.invokeOn(this);
			}
		} else {
			super.signalEvent(type, event);
		}
		signalCount--;
		if (shouldUpdateSubscriptions && signalCount == 0) {
			updateSubscriptions();
		}
	}

	public org.jbpm.workflow.instance.NodeInstance getFirstNodeInstance(final long nodeId) {
		// level logic not relevant.
		for (NodeInstance ni : this.getNodeInstances()) {
			if (ni.getNodeId() == nodeId) {
				return (org.jbpm.workflow.instance.NodeInstance) ni;
			}
		}
		return null;
	}

	protected boolean isMyWorkItem(WorkItem event) {
		return event.getId() == getWorkItemId() || (getWorkItem() != null && getWorkItem().getId() == (event.getId()));
	}

	protected WorkItem createWorkItem() {
		// if we are in here, it means it is a standalone case, not called from a CaseTask
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setName("Human Task");
		workItem.setProcessInstanceId(getId());
		workItem.setParameters(new HashMap<String, Object>());
		workItem.setParameter("NodeName", getCase().getName());
		String initiator = getInitiator();
		String caseOwner = getCaseOwner();
		if (initiator != null) {
			workItem.setParameter(TaskParameters.INITIATOR, initiator);
		}
		if (caseOwner != null) {
			workItem.setParameter(PeopleAssignmentHelper.ACTOR_ID, caseOwner);
		} else if (initiator != null) {
			workItem.setParameter(PeopleAssignmentHelper.ACTOR_ID, initiator);
		} else {
			throw new IllegalStateException("A Case Instance must have either an initiator, an owner or both");
		}
		workItem.setParameter(TaskParameters.CLAIM_IMMEDIATELY, true);
		workItem.setParameter(PeopleAssignmentHelper.GROUP_ID, TableItem.getPlannerRoles(this.getCase()));
		workItem.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, TableItem.getPlannerRoles(this.getCase()));
		return workItem;
	}

	public String getCaseOwner() {
		return (String) getVariable(TaskParameters.CASE_OWNER);
	}

	public String getInitiator() {
		return (String) getVariable(TaskParameters.INITIATOR);
	}

	@Override
	public void start() {
		super.start();
		updateSubscriptions();
		maybeExecuteWorkItem();
	}

	private void maybeExecuteWorkItem() {
		if (workItemId < 0) {
			this.workItem = createWorkItem();
			executeWorkItem(workItem);
			this.workItemId = workItem.getId();
		}
	}

	private WorkItem executeWorkItem(WorkItem wi) {
		String deploymentId = (String) getKnowledgeRuntime().getEnvironment().get("deploymentId");
		wi.setDeploymentId(deploymentId);
		try {
			((WorkItemManager) getKnowledgeRuntime().getWorkItemManager()).internalExecuteWorkItem((org.drools.core.process.instance.WorkItem) wi);
			return wi;
		} catch (WorkItemHandlerNotFoundException wihnfe) {
			setState(ProcessInstance.STATE_ABORTED);
			throw wihnfe;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void updateSubscriptions() {
		SubscriptionManager subscriptionManager = (SubscriptionManager) getKnowledgeRuntime().getEnvironment().get(SubscriptionManager.ENV_NAME);
		if (subscriptionManager != null) {
			CaseInstance caseInstance = this;
			ObjectPersistence persistence = subscriptionManager.getObjectPersistence(caseInstance);
			Map<CaseFileItem, Collection<Object>> parentSubscriptions = new HashMap<CaseFileItem, Collection<Object>>();
			Collection<Object> subscriptions = new HashSet<Object>();
			populateSubscriptionsActivatedByParameters(parentSubscriptions, subscriptions, getCase().getInputParameters());
			populateSubscriptionsActivatedByParameters(caseInstance, parentSubscriptions, subscriptions);
			subscriptionManager.updateSubscriptions(caseInstance, subscriptions, parentSubscriptions, persistence);
			subscriptionManager.flush(persistence);
		}
		shouldUpdateSubscriptions = false;

	}

	protected void populateSubscriptionsActivatedByParameters(NodeInstanceContainer caseInstance, Map<CaseFileItem, Collection<Object>> parentSubscriptions, Collection<Object> subscriptions) {
		Collection<NodeInstance> nodeInstances = caseInstance.getNodeInstances();
		for (NodeInstance ni : nodeInstances) {
			if (ni.getNode() instanceof PlanItem) {
				PlanItem<?> pi = (PlanItem<?>) ni.getNode();
				if (pi.getPlanInfo().getDefinition() instanceof TaskDefinition && ni instanceof ControllableItemInstanceLifecycle && isSubscribing((ControllableItemInstanceLifecycle<?>) ni)) {
					TaskDefinition td = (TaskDefinition) pi.getPlanInfo().getDefinition();
					populateSubscriptionsActivatedByParameters(parentSubscriptions, subscriptions, td.getOutputs());
				}
			}
			if (ni instanceof StagePlanItemInstance) {
				populateSubscriptionsActivatedByParameters((NodeInstanceContainer) ni, parentSubscriptions, subscriptions);
			}
		}
	}

	protected boolean isSubscribing(ControllableItemInstanceLifecycle<?> ni) {
		return ni.getPlanElementState() == PlanElementState.ACTIVE || ni.getPlanElementState() == PlanElementState.ENABLED;
	}

	@SuppressWarnings("unchecked")
	protected void populateSubscriptionsActivatedByParameters(Map<CaseFileItem, Collection<Object>> parentSubscriptions, Collection<Object> subscriptions,
			Collection<CaseParameter> subscribingParameters) {
		for (CaseParameter caseParameter : subscribingParameters) {
			if (caseParameter.getBindingRefinementEvaluator() == null) {
				Object var = getVariable(caseParameter.getBoundVariable().getName());
				if (var instanceof Collection) {
					subscriptions.addAll((Collection<? extends Object>) var);
				} else if (var != null) {
					subscriptions.add(var);
				}
			} else {
				ProcessContext ctx = new ProcessContext(getKnowledgeRuntime());
				ctx.setProcessInstance(this);
				try {
					Object subscribeTo = caseParameter.getBindingRefinementEvaluator().evaluate(ctx);
					if ((subscribeTo instanceof Collection && ((Collection<?>) subscribeTo).isEmpty()) || subscribeTo == null) {
						// Nothing to subscribe to - subscribe to parent for CREATE and DELETE events
						Object parentToSubscribeTo = caseParameter.getBindingRefinementParentEvaluator().evaluate(ctx);
						if (parentToSubscribeTo != null) {
							Collection<Object> collection = parentSubscriptions.get(caseParameter.getBoundVariable());
							if (collection == null) {
								parentSubscriptions.put(caseParameter.getBoundVariable(), collection = new HashSet<Object>());
							}
							if (parentToSubscribeTo instanceof Collection) {
								collection.addAll((Collection<? extends Object>) parentToSubscribeTo);
							} else {
								collection.add(parentToSubscribeTo);
							}
						}
					} else if (subscribeTo instanceof Collection) {
						subscriptions.addAll((Collection<?>) subscribeTo);
					} else if (subscribeTo != null) {
						subscriptions.add(subscribeTo);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public Set<OnPartInstanceSubscription> findOnPartInstanceSubscriptions() {
		Set<CaseParameter> params = new HashSet<CaseParameter>();
		params.addAll(getCase().getInputParameters());
		addSubscribingCaseParameters(params, this);
		Collection<NodeInstance> nodes = getNodeInstances();
		Map<OnPartInstance, OnPartInstanceSubscription> onCaseFileItemParts = new HashMap<OnPartInstance, OnPartInstanceSubscription>();
		for (CaseParameter item : params) {
			findCaseFileItemOnPartsFor(item, nodes, onCaseFileItemParts);
		}
		return new HashSet<OnPartInstanceSubscription>(onCaseFileItemParts.values());

	}

	private void addSubscribingCaseParameters(Set<CaseParameter> params, NodeInstanceContainer caseInstance) {
		for (NodeInstance ni : caseInstance.getNodeInstances()) {
			if (ni.getNode() instanceof PlanItem) {
				PlanItem<?> pi = (PlanItem<?>) ni.getNode();
				PlanItemDefinition def = pi.getPlanInfo().getDefinition();
				if (def instanceof TaskDefinition) {
					params.addAll(((TaskDefinition) def).getOutputs());
				}
			} else if (ni instanceof StagePlanItemInstance) {
				addSubscribingCaseParameters(params, (NodeInstanceContainer) ni);
			}
		}
	}

	private void findCaseFileItemOnPartsFor(CaseParameter parameter, Collection<NodeInstance> nodes, Map<OnPartInstance, OnPartInstanceSubscription> target) {
		for (NodeInstance node : nodes) {
			if (node instanceof OnPartInstance) {
				OnPartInstance onPartInstance = (OnPartInstance) node;
				if (onPartInstance.getOnPart() instanceof CaseFileItemOnPart) {
					CaseFileItemOnPart onPart = (CaseFileItemOnPart) onPartInstance.getOnPart();
					if (onPart.getSourceCaseFileItem().getElementId().equals(parameter.getBoundVariable().getElementId())) {
						OnPartInstanceSubscription subscription = target.get(onPartInstance);
						if (subscription == null) {
							target.put(onPartInstance, new OnPartInstanceSubscription(getCase().getCaseKey(), getId(), onPart, parameter));
						} else {
							subscription.addParameter(parameter);
						}
					}
				}
			} else if (node instanceof StagePlanItemInstance) {
				findCaseFileItemOnPartsFor(parameter, ((StagePlanItemInstance) node).getNodeInstances(), target);
			}
		}
	}

	@Override
	public void setPlanElementState(PlanElementState s) {
		this.planElementState = s;
	}

	@Override
	public void reactivate() {
		planElementState.reactivate(this);
	}

	@Override
	public void suspend() {
		planElementState.suspend(this);
	}

	@Override
	public void terminate() {
		planElementState.terminate(this);
	}

	@Override
	public void complete() {
		planElementState.complete(this);
	}

	@Override
	public void triggerTransitionOnTask(PlanItemTransition transition) {
		WorkItemImpl wi = new WorkItemImpl();
		wi.setName(TaskParameters.UPDATE_TASK_STATUS);
		wi.setParameter(TaskParameters.TASK_TRANSITION, transition);
		wi.setParameter(TaskParameters.WORK_ITEM_ID, getWorkItemId());
		if (transition == PlanItemTransition.COMPLETE) {
			if (isCalledFromCaseTask()) {
				executeWorkItem(wi);
				// Let the caseTaskInstance know
				getKnowledgeRuntime().signalEvent("processInstanceCompleted:" + getId(), this, getWorkItem().getProcessInstanceId());
				// Because only the Task will be completed in the callback, not this caseInstance:
				complete();
			} else {
				// Standalone, so write this caseInstance's result to the tasks result
				wi.getParameters().putAll(getResult());
				executeWorkItem(wi);
			}
		} else {
			executeWorkItem(wi);
		}
	}

	private boolean isCalledFromCaseTask() {
		return getWorkItem().getProcessInstanceId() != getId();
	}

	@Override
	public void create() {
		planElementState.create(this);
	}

	@Override
	public void fault() {
		planElementState.fault(this);
	}

	@Override
	public void close() {
		planElementState.close(this);
	}

	@Override
	public PlanElementState getPlanElementState() {
		return planElementState;
	}

	@Override
	public Collection<? extends ItemInstanceLifecycle<?>> getChildren() {
		Set<ItemInstanceLifecycle<?>> result = new HashSet<ItemInstanceLifecycle<?>>();
		for (NodeInstance nodeInstance : getNodeInstances()) {
			if (nodeInstance instanceof ItemInstanceLifecycle) {
				result.add((ItemInstanceLifecycle<?>) nodeInstance);
			}
		}
		return result;
	}

	@Override
	public CaseInstance getCaseInstance() {
		return this;
	}

	public WorkItem getWorkItem() {
		if (workItem == null && workItemId >= 0) {
			workItem = ((WorkItemManager) getKnowledgeRuntime().getWorkItemManager()).getWorkItem(workItemId);
		}
		return workItem;
	}

	@Override
	public Object getTask() {
		if (getWorkItem() != null) {
			return getWorkItem().getResult(TaskParameters.TASK);
		}
		return null;
	}

	@Override
	public long getWorkItemId() {
		if (workItem != null) {
			return workItem.getId();
		}
		return workItemId;
	}

	@Override
	public String getHumanTaskName() {
		return getCase().getName();
	}

	@Override
	public boolean canComplete() {
		return PlanItemInstanceUtil.canComplete(this);
	}

	@Override
	public PlanItemContainer getPlanItemContainer() {
		return getCase();
	}

	public void inernalSetWorkItemId(long readLong) {
		this.workItemId = readLong;
	}

	public void setWorkItem(WorkItem w) {
		this.workItem = w;
		this.workItemId = w.getId();
	}

	public ControllableItemInstanceLifecycle<?> findNodeForWorkItem(long id) {
		for (NodeInstance ni : getNodeInstances(true)) {
			if (ni instanceof ControllableItemInstanceLifecycle && ((ControllableItemInstanceLifecycle<?>) ni).getWorkItemId() == id) {
				return (ControllableItemInstanceLifecycle<?>) ni;
			}
		}
		return null;
	}

	@Override
	public void setState(int state) {
		super.setState(state);
		if (state == STATE_SUSPENDED) {
			suspend();
		} else if (state == STATE_ACTIVE && (getPlanElementState().isSemiTerminalState(this) || getPlanElementState() == PlanElementState.SUSPENDED)) {
			reactivate();
		}
	}

	public ControllableItemInstanceLifecycle<?> ensurePlanItemCreated(long parentWorkItemId, String discretionaryItemId, WorkItem wi) {
		ControllableItemInstanceLifecycle<?> found = findNodeForWorkItem(wi.getId());
		if (found != null) {
			return found;
		} else {
			PlanElementWithPlanningTable e = findPlanElementWithPlanningTable(parentWorkItemId);
			DiscretionaryItem<?> item = e.getPlanningTable().getDiscretionaryItemById(discretionaryItemId);
			PlanItemInstanceContainerLifecycle piic = null;
			if (e instanceof PlanItemInstanceContainerLifecycle) {
				piic = (PlanItemInstanceContainerLifecycle) e;
			} else {
				piic = (PlanItemInstanceContainerLifecycle) ((NodeInstance) e).getNodeInstanceContainer();
			}
			found = (ControllableItemInstanceLifecycle<?>) piic.getNodeInstance(item);
			found.internalTriggerWithoutInstantiation(piic.getNodeInstance(piic.getPlanItemContainer().getDefaultSplit()), NodeImpl.CONNECTION_DEFAULT_TYPE, wi);
			if (e.getPlanElementState() == PlanElementState.ACTIVE) {
				found.create();
				found.noteInstantiation();
			} else {
				found.setPlanElementState(PlanElementState.INITIAL);
			}
			return found;
		}
	}

	public Map<String, String> getApplicableDiscretionaryItems(long wi, String user) {
		Map<String, String> result = new HashMap<String, String>();
		NodeInstance ni = null;
		PlanningTable pt = null;
		if (getWorkItemId() == wi) {
			pt = getCase().getPlanningTable();
			ni = getNodeInstance(getCase().getDefaultSplit());
		} else {
			ControllableItemInstanceLifecycle<?> ce = findNodeForWorkItem(wi);
			if (ce instanceof PlanElementWithPlanningTable) {
				pt = ((PlanElementWithPlanningTable) ce).getPlanningTable();
				ni = (NodeInstance) ce;
			}
		}
		if (pt != null) {
			putApplicableItems(user, result, ni, pt);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void putApplicableItems(String user, Map<String, String> result, NodeInstance ni, PlanningTable pt) {
		boolean authorized = false || true;
		// TODO we still need to associate the roles somewhere - perhaps from planning
		for (Role role : pt.getAuthorizedRoles().values()) {
			Collection<String> variable = (Collection<String>) getVariable(role.getName());
			if (variable != null && variable.contains(user)) {
				authorized = true;
			}
		}
		if (authorized) {
			for (TableItem ti : pt.getTableItems()) {
				if (PlanItemInstanceUtil.isApplicable(ti, ni)) {
					if (ti instanceof DiscretionaryItem<?>) {
						result.put(ti.getElementId(), ((DiscretionaryItem<?>) ti).getDefinition().getName());
					} else {
						putApplicableItems(user, result, ni, (PlanningTable) ti);
					}
				}
			}
		}
	}

	public Map<String, Object> getResult() {
		Map<String, Object> result = new HashMap<String, Object>();
		for (CaseParameter cp : getCase().getOutputParameters()) {
			Object variable = PlanItemInstanceUtil.getRefinedValue(cp, this, null);
			result.put(cp.getName(), variable);
		}
		return result;
	}
}
