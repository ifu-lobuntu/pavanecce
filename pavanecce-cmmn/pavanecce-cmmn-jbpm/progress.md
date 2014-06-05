# 1. Done and Tested

JCR/OCM persistent subscriptions

- scope of subscriptions
- bindingRefined
- throwing CREATE,DELETE,ADD_CHILD,REMOVE_CHILD,ADD_REFERENCE,REMOVE_REFERENCE and UPDATE events

JCR/OCM demarcated subscriptions

- throwing CREATE,DELETE,ADD_CHILD,REMOVE_CHILD,ADD_REFERENCE,REMOVE_REFERENCE and UPDATE events

Pure JCR persistent subscriptions

- scope of subscriptions
- bindingRefined
- throwing CREATE,DELETE,ADD_CHILD,REMOVE_CHILD,ADD_REFERENCE,REMOVE_REFERENCE and UPDATE events

Pure JCR demarcated subscriptions

- throwing CREATE,DELETE,ADD_CHILD,REMOVE_CHILD,ADD_REFERENCE,REMOVE_REFERENCE and UPDATE events

JPA/Hibernate persistent subscriptions

- scope of subscriptions 
- bindingRefined
- throwing CREATE,DELETE,ADD_CHILD,REMOVE_CHILD,ADD_REFERENCE,REMOVE_REFERENCE and UPDATE events

JPA/Hibernate demarcated subscriptions

- throwing CREATE,DELETE,ADD_CHILD,REMOVE_CHILD,ADD_REFERENCE,REMOVE_REFERENCE and UPDATE events

Timer Event

- OCCUR,CREATE,SUSPEND,RESUME,TERMINATE,PARENT_TERMINATE
- timerStart CaseFileItem
- timerStart PlanItem

Milestone

- entry criteria achieved
- requiredRule
- repetitionRule
- OCCUR,CREATE,SUSPEND,RESUME,TERMINATE,PARENT_TERMINATE

CaseTask

- basic call
- entry criteria achieved
- automaticActivationRule
- exit criteria achieved
- parameterMapping
- parameter transformation
- writing result to bindingRef/bindingRefinement
- EXIT,DISABLE,ENABLE,MANUAL_START,START,COMPLETE,TERMINATE,FAULT,SUSPEND,RESUME,REENABLE,REACTIVATE,CREATE

Human Task

- automaticActivationRule
- repetitionRule
- entry criteria achieved
- exit criteria achieved
- writing result to bindingRef/bindingRefinement
- EXIT,DISABLE,ENABLE,MANUAL_START,START,COMPLETE,TERMINATE,FAULT,SUSPEND,RESUME,REENABLE,REACTIVATE,CREATE

UserEvent

- OCCUR, CREATE,SUSPEND,RESUME,TERMINATE,PARENT_TERMINATE

Stage:

- automaticActivationRule
- exit criteria achieved
- EXIT,DISABLE,ENABLE,MANUAL_START,START,COMPLETE,TERMINATE,FAULT,SUSPEND,RESUME,CREATE,REENABLE,REACTIVATE
- auto completion

CaseModel

- auto completion
- manual completion
- direct exit Criteria
- CLOSE, COMPLETE,CREATE, SUSPEND, REACTIVATE (from suspended),TERMINATE,FAULT
- Write Output Parameters from Processes to Task.result (CaseTask, ProcessTask, Standalone Case) 

PlanningTable

- authorizedRoles - pass the Case roles that the user is fulfilling 

Planning Service

- startPlanning (include current parameters, authorizedRole)
- prepareDiscretionaryItem 
- submitPlan 
-- update parameters (taskContent AND workItem ),
-- role (do role-assignment in process too) and
- makeDiscretionaryItemAvailable
 
DiscretionaryItem

- entryCriteria 
- itemControl.automaticActivationRule
- ApplicabilityRule
- itemControl.requiredRule
 
Generate OCM and JPA test Java classes from UML along with CaseFileItemDefinition includes

Implement OCLProcessDialect that extends JavaProcessDialect

Output ParameterMapping, bindingRefinement and scoped subscriptions

Recursive subscriptions, parameterizable


# 2. To test more directly

- *.no sentry
- *.repetitionRule
- *.parentSuspend
- *.parentResume
- Storage of event variables inside StageInstances
- CaseTask.reactivated - check that process is restarted - it failed previously
- Planning Tables contained by HumanTasks and Stages

# 3. In Progress

XPath processDialect on JCR

# 4. To implement

Planning Service - HumanTask, Stage AND CasePlanModel

- startPlanning (include applicabilityRule) *
- prepareDiscretionaryItem *
-- recheck applicabilityRule*
-- recheck authorizedRole*
- submitPlan *
-- status/transition, *
-- (due date?)*

PlanItemOnPart.sentryRef *

Sentry.condition without onParts *

DiscretionaryItem - need input from OMG

- exitCriteria
- itemControl.repititionRule

# 5. Current potential problems
 
AbstractPersistentSubscriptionManager
- Find a way to get to the commandScopedEntity manager - the reflection won't work in CDI 

PlanningService (look at TaskServiceFactory interface)

- find a way to get to the persistence context - the reflection won't work in CDI
- find a way to get to the InternalTaskService

# 6. Difficult

For the validation of authorization for planning - get the possible roles from somewhere, perhaps the UserInfo service, but group != role

 
Sentry.condition without onParts - when to trigger the evaluation of the condition.


With JCR Property.CHANGE events, try to get the original value and only fire events for ADD_/REMOVE_REFERENCE when they applicable - JCR limitation


Optimize caching of SubscriptionInfo and perhaps even other OCM objects IN OCMSubscriptioinManager


# 7. Low priority


Plan fragments
- For Plan Items
- For Discretionary Items


ContextRefs for rules


Internationalization


CaseModel:

- REACTIVATE (from Failed,Terminated, Completed? need input from OMG) 