# 1. Overview

This document is intended for developers that wish to understand how this CMMN implementation fits on top of jBPM. It discusses the current state of developments, and the approach followed implementing the CMMN specification. It also discusses a couple of important design designs that were made and the reasoning behind them. Lastly, it highlights some problem areas that still need to be addressed. It assumes a basic understanding of the jBPM code base. It also assumes a basic understanding of the various CMMN constructs. I you are entirely new to CMMN, please read this [introduction](../cmmn-intro.md)

# 2. High level project phases.
This CMMN implementation is being built specifically to contribute usable functionality towards the [Pavanecce](http://www.ifu-lobuntu.org/home/projects/pavanecce.html) project. Our priority is getting online 
collaborative communities up and running, hence our initial focus was on the runtime rather than the development tooling. We have broken the project up into the following high-level consecutive phases:

- **Phase 1.** Implement Runtime Semantics.
- **Phase 2.** Implement Task Management and Planning User Interface.
- **Phase 3.** Implement Diagram tooling.

# 3. Implement runtime semantics.
This phase is about 80% complete. The vast majority of the CMMN specification's semantics has been implemented, but there are still a couple of grey areas that need to be fleshed out. At this point, the CMMN runtime
contains enough functionality to start using in the other projects at Ifu Lobuntu. 

## 3.1. Approach followed.
The plan initially was to follow the same approach as the BPMN extension of jBPM. jBPM's BPMN extension introduces a couple of new Node classes, but does not introduce any new NodeInstance classes. It also provides a separate SemanticModule implementation which handles the BPMN xml documents. The Pavanecce CMMN implementation started off along similar lines.  

However, CMMN defines very specific life-cycle requirements for PlanItems (Tasks, CaseTasks, Stages, Events etc.) in the process instance, as they react to events originating from the task engine and persistence engine. As a result, the need arose to implement custom NodeInstances for the CMMN elements, and hence a CaseInstanceMarshaller class that supports these NodeInstances. The current and previous state of PlanItemInstances are marshaled as well, which allows the life-cycle to be managed across multiple interactions with the CaseInstance. As was expected, we also had to implement different Node classes as well, and eventually also a new NodeFactory class.

## 3.2. Separation of the 'Definition' of a PlanItem from its 'Inclusion' in a Case. 
One important aspect of CMMN to keep in mind is the separation of the DEFINITION of a PlanItem from its INCLUSION in a Stage or a Case. This is similar to the distinction that certain BPM engines have between 
stand-alone tasks and embedded tasks. PlanItemDefinitions are like  stand-alone tasks that need to be included into a flow. A PlanItemDefinition is included in a Stage or Case by either using a 
user-controlled DiscretionaryItem, or a more 'automated' PlanItem. However, the underlying PlanItemDefinition still needs to execute the same way no matter which context it is being used from.

In order to address this requirement, PlanItemDefinitions, DiscretionaryItems and PlanItems are all just subclasses of the standard jBPM StateNode. However, they are carried as isolated state in the NodeContainer (Case/Stage) and they have no incoming or outgoing connections. After the CMMN file has been read, the state of the PlanItemDefinition is copied into each DiscretionaryItem or PlanItem StateNode that is used to include the PlanItemDefinition into the containing Case or Stage. This part of the parsing process uses reflection to copy the state across recursively. This approach works currently and it ensures that the multiple uses of the same PlanItemDefinition don't interfere with each other, but it is not ideal. We're considering some other options here.

## 3.3. State Machines/Life-cycles
Like the WS Human Tasks specification, CMMN provides well-defined state-machines for the life-cycle of the various elements. In order to implement the CMMN specification's specific life-cycle requirements, 
this implementation uses a slightly modified version of the 'State pattern' to manage the life-cycle of Plan elements. A polymorphic enum class, PlanElementState, implements all the validations and transitions. 
The enum literal representing a Plan element's current state gets stored in the process against the NodeInstance whose state it represents, and it directly affects the flow of the ProcessInstance.  There are two different PlanItemInstance life-cycles: one for Tasks and Stages and another for Milestones and EventListeners.

### 3.3.1 Milestones and EventListeners: OccurrablePlanItemInstance 
Milestones, UserEvents and TimerEvents have simple life-cycles. Generally they are controlled indirectly from the parent Stage or CaseInstance. The PlanElementState class takes care of the relationship
between the state of parent Stages and CaseInstances and child Milestones and Events.

### 3.3.2 Tasks and Stages: ControllablePlanItemInstance 
This life-cycle is quite complex, and the majority of the transitions are typically triggered by the user. Each of these PlanItems have a task in the jBPM's TaskService representing its current state, and all of the
user induced triggers are therefore triggered from the TaskService. The CaseTaskLifecycleListener class forwards all task transitions to the process engine, where they get interpreted within the context of the 
CaseInstance's current state by the various PlanItemInstances' current PlanElementState.

### 3.3.3 WS Human Tasks / CMMN Life-cycle  mapping
There is an encouraging overlap between the life-cycle of the WS Human Task specification implemented by jBPM, and the life-cycle of a Task/Stage PlanItem of the CMMN specification. The states defined by WS Human
Tasks generally form a superset of the states defined by CMMN. However, when it comes to the transitions in these two life-cycles there are some extra transitions defined in the CMMN specification that are not
available in WS Human Tasks, and vice versa. We had to implement a couple of extra commands and event listener annotations  to support these transitions. The CaseTaskLifecycleListener class is an example 
of how these extra annotations can be used.

The following table lists the mapping of WS Human Tasks states to CMMN Task/Stage States

|WS Human Task state	|CMMN Task/Stage state|
|-----------------------|---------------------|
|Created				|N/A                  |				
|Ready					|Enabled              |
|Reserved				|Enabled              |
|Completed				|Completed            |
|Completed				|Closed               |
|Suspended				|Suspended            |
|Exited					|Terminated           |
|Failed					|Failed               |
|Error					|Failed               |
|Obsolete				|Disabled             |
|InProgress				|Active               |

The CMMN Created and Available states are not applicable to Task instances as the corresponding Task instance does not exist in the jBPM TaskService at that given point.

The following table lists the mapping of WS Human Task transitions to CMMN Task/Stage transitions

|WS Human Task transition|CMMN Task/Stage transition      |
|------------------------|--------------------------------|
|Activate				 |Enable                          |
|Suspend				 |Suspend                         |
|Resume					 |Resume                          |
|Skip					 |Disable                         |
|Start					 |Manual Start                    |
|Exit					 |Terminate                       |
|Fault					 |Fault                           |
|Complete				 |Complete                        |
|N/A					 |Exit (Exit criteria become true)|
|N/A					 |ParentSuspend                   |
|N/A					 |ParentResume                    |
|N/A					 |Start (automatically)           |
|N/A					 |Reenable                        |
|Forward				 |N/A                             |
|Delegate				 |N/A                             |
|Claim					 |N/A                             |

### 3.3.4. PlanItemInstanceFactoryNode
One of the fuzzy areas in the CMMN specification is the transition from the Available state to the Enabled or Active states. When reading the CMMN specification, it appears that a NEW instance of the PlanItem may
be created on this transition. From the perspective of a traditional Object Oriented programming language this poses a challenge - implementing a single state machine across multiple instances. In order to implement
this requirement, two extra class were introduced - the PlanItemInstanceFactoryNode and associated PlanItemInstanceFactoryNodeInstance. When a PlanItem or Discretionary item's entry criteria Sentry becomes 
true, this node gets to decide whether the corresponding PlanItem is required, whether it can repeat, and even whether it is included by the discretion of the user. This adds some complexity to the implementation, 
but it does seem to address the CMMN requirement.   

## 3.4. Updating task statuses from the process
CMMN requires much tighter integration between the TaskService and the Process runtime than what is be the case with standard BPMN. Every instance of a Case, Stage, Human Task, Process Task or Case Task creates an
associated Task in the jBPM TaskService on instantiation, that is, when their entry criteria are met. However, on occasion, a condition is reached within the context of the process instance that requires the 
associated Task to be updated. This is done by sending a 'UpdateTaskStatus' WorkItem to the WorkItemManager using ControllableItemInstanceImpl.triggerTransitionOnTask(PlanItemTransition) method. In the 
WorkItemManager, the UpdateTaskStatusWorkItemHandler class picks up the WorkItem and does interesting things to the associated jBPM Task. The user of this triggerTransitionOnTask method should assume that the 
WorkItem will be delivered asynchronously to the WorkItemManager. Currently, all these queued workItems are delivered in the ObjectPersistence.commit() method, but his is just a temporary 'workaround' 
to emulate asynchronous behavior for tests. 

## 3.5. Events
Events are central to CMMN - events generated by the persistence engine and the process engine. CMMN also requires very tight integration between the task engine and process engine, which is mediated by WorkItems and
the WorkItemManager. Generally it would seem that this CMMN implementation may benefit from a more asynchronous approach to events and WorkItems, and this will be implemented over time. However, for testing purposes,
WorkItems and events are delivered synchronously in the ObjectPersistence.commit() method. This is not ideal for a production environment, and truly asynchronous behavior will have to be implemented. However, the
particulars of the implementation of this asynchronous delivery depends on whether this CMMN implementation will run in a non-JTA, non-CDI, non-JMS Jahia/Tomcat environment, or in a KIE Workbench, full JEE JBoss/Wildfly environment. At least the event delivery is pretty well isolated in the AbstractPersistentSubscriptionManager class, and it should not be overly complicated to support different event architectures. 

## 3.6. Planning
Planning introduces a new level of control over the processes. Again, these activities are mediated by the TaskService, and are initiated from the PlanningService class which co-ordinates the TaskService and 
the ProcessRuntime. During planning, new 'discretionary' Tasks may be created, but it is essential for them to be initialized from the CaseInstance into which they will be created, as their input parameters 
may very well depend on the current state of the CaseInstance. This communication between the TaskService and ProcessRuntime thus needed to be direct and synchronous, which means that the PlanningService does not
work via the WorkItemManager, but interacts directly with the CaseInstance in question, but it is well worth breaking the encapsulation strategies there. 

## 3.7. Persistence
Along with the tight integration with the Task engine, CMMN also requires tight integration with the persistence engine. CMMN requires a single persistence mechanism for both Variables (CaseFileItems) in 
CaseInstances as well as for CaseParameters on Tasks. CMMN also comes more of a document-oriented, pass-by-reference paradigm rather than the XML-oriented, pass-by-value paradigm of BPMN. As a result, this implementation had to introduce a whole lot of new of ObjectMarshallingStrategies. A problem we encountered was that the jBPM TaskService did not consistently use an Environment object when doing parameter marshaling, which means it did not pick up our new ObjectMarshallingStrategies. We had to implement a couple of custom commands to ensure the parameters are marshaled correctly. During subscription demarcation, we also found that we needed to make sure that the ObjectMarshallingStrategies used the same instance of the underlying persistence engine to ensure consistent behavior in the TaskService and the process runtime. This was achieved by letting  these marshaling strategies use implementations of the ObjectPersistence class from the Environment, which is simple a wrapper around a ThreadLocal persistence engine such as an EntityManager or Jackrabbit ObjectContentManager. In the tests, the ObjectPersistence implementation is also responsible for ensure that all events are dispatched correctly.

### 3.7.1. JPA
For JPA, we found the fine grained command scoped EntityManager to be problematic for persistence of custom JPA entities. In fact, it does not seem like a good idea in general to have the same persistenceUnit
for custom entities and the core jBPM entities. The JpaCasePersistence also relies on Hibernate event listeners (Flush-entity, post-insert, post-delete) to queue the persistence events, which it then dispatches
just after flushing the underlying EntityManager. Signaling events this way has a slight performance impact. Under certain circumstances, specifically with the CasefFileItem.ADD_XXX and CaseFileItem.REMOVE_XXX transitions, unininitalized lazy loaded collections need to be initialized to determine membership. Because of this, combined with the current synchronous delivery of events, one can expect a performance impact on Hibernate, which may be alleviated to some extent when events are delivered asynchronously.  

### 3.7.2. Jackrabbit's ObjectContentManager (OCM)
For Jackrabbit's OCM, we followed a similar approach, and the OcmCasePersistence also requires a JCR EventListener to queue persistence events. We implemented support for OCM Events that are generated by the JCR 
implementation from a standard JCR EventListener, but assume that the nodes that generate the events are mapped to OCM annotated Java domain classes. Interestingly, the default behavior for JCR is that these events should be delivered asynchronously, but thanks to a Jackrabbit, one has the option of delivering JCR persistence events synchronously. A problem we faces is that JCR events do not contain enough information to reconstruct deleted objects or removed references. Deleted objects passed to the CaseInstance therefore only contain the UUID, but not data. OCM does not have a session based cache for object as JPA does. It is therefore substantially slower than JPA which is a point of concern. It is also not very clear how well supported and active the OCM project is.

### 3.7.3. 'Pure' JCR. 
It is also possible to associated standard JCR nodes with processes and tasks. We are currently working on this feature. Whereas Java and MVEL can be used for the JPA and OCM classes, JCR nodes 
would allow us to support XPath as another process dialect. 

## 3.8. Other challenges
Currently, there are a few challenges that still stands in the way of using this implementation in all environments. 

1. The JbpmServicesPersistenceManager is not available to the new commands that were implemented. All these new commands define an input parameter for this class, but the class from which they are 
instantiated can only get hold of the JbpmServicesPerstenceManager via CDI. Since it is very likely that this CMMN implementation will be used from a non-CDI environment, we had to resort to reflection to
get hold of the  JbpmServicesPerstenceManager  in the TaskServiceEndpoint. This is not ideal.
2. We faced a similar problem with the command scoped EntityManager when submitting events, and had to use reflection to get hold of this object.
3. Resolving the correct RuntimeEngine from the RuntimeManager, is problematic from classes such as the Hibernate event listener, whose instantiation we do not control. 

# 4. Implement task management user interface.
This is our next phase and we are about to embark on this phase, and it will focus on implementing a task management user interface that leverages the full richness of task management in CMMN. CMMN, especially the Planning activity in CMMN, requires more user interface functionality than traditional BPMN task management engines. One specific activity associated with planning is that the planning user can manually override task input parameters. Task parameters always represent documents from the case file, called CaseFileItems. Since these documents are passed by reference, it is possible for a planner to override and select other parameters for a task. The planning user  could also assign the task to another user, or change the due date. Ideally we also give the task management user interface access to the WS Human Task people assignments, deadlines and escalations/notifications, which would allow jBPM's task service to be fully utilized. The CMMN Planning activity also allows the planner to create instances of discretionary task which are however still associated with and co-ordinated by the CaseInstance. It seems that we would have to implement significant amount of new functionality for the task management user interface. 


## 4.1. KIE Workbench
The first prize would be to use jBPM's GWT based task user interface modeler and infrastructure to render the task forms, available in KIE Workbench. However, CMMN tasks have different operations than those supported by the default user interface (WS Human Task). CMMN Planning activities also require a user interface that reflects the containment of subtasks within tasks, and this may need to be developed to support CMMN. Issues to consider here are:

1.  How extensible/pluggable is the current jBPM Task user interface?
2.  Is there a demand for CMMN on jBPM? If so, there would an incentive to implement the Task user interface here. 
3.  Would we be able to integrate jBPM's KIE Workbench with a content management system?

## 4.2 Jahia Digital Factory
The content management system we currently favor, Jahia, also has very solid support for jBPM's task management. It is more closely integrated with content management, and every task's structure is copied to a node in the document database. This has some advantages, and also allows us to leverage Jahia's templating facilities to automatically generate and easily customize forms for tasks. This could be particularly useful when Pavenecce is used to integrate with JCR, as the CaseFileItems being passed to and operated on in the various tasks would be represented as normal JCR documents. This aligns nicely with the Pavanecce vision of  participants building a web presence to "sell" their capabilities and products. Consdiderations here are

1. Would JCR be more preferable than JPA for persistence? Performance is a concern.
2. How difficult would the templating infrastructure in Jahia be for end users to leverage when customizing the user interface?

## 4.3. jBPM's GWT Task User Interface  in Jahia
Like jBPM, Jahia also makes extensive use of GWT. There are ways to implement your own custom GWT user interfaces, but it is not clear yet whether it would be possible to integrate jBPM's GWT Task user interface into Jahia. The big difference between jBPM and Jahia is that Jahia uses Spring and OSGi extensively, whereas jBPM uses CDI and Errai. Mixing them all together is not impossible, but it would make for a pretty complicated server architecture.

# 5. Implement diagram tooling.
This is the last phase of our CMMN implementation (we may need to revisit this, given the difficulty of visualizing and communicating the various cases and VDML collaborations). The vision for Pavenecce is that participants would access CMMN models from their browsers. When changes to CMMN cases (or any other shared model) are proposed, participants would want to be able to provide input on the decision, and possibly also vote on it. Given the contractual importance of these models, a change history for models would be essential.

## 5.1. jBPM Designer
Again jBPM Designer on the KIE Workbench would be first prize, first and foremost because it is the only web based modeling framework that we could find that is fully open source. Activiti KIS isn't, even though its code base was cannibalized from the same project that jBPM Designer's modeling framework was based on, Oryx. JointJS/Rappid is very promising, but again it is only half open source. The jBPM team have cleaned up the Oryx code base nicely, and got rid of its original clunky server side.

There are obvious benefits to using the KIE workbench and its built-inn Oryx modeler for CMMN. Its built in support for Git would immediately satisfy the requirement for a change history, and also works well when the inevitable custom code needs to be implemented to support the model. If used cleverly with an issue tracking system, one may even be able to implement the requirement for review and voting, albeit a very technical solution. 

However, one problem with the Oryx framework is that it does not support the separation of the diagram from the model. Generally, it works best when a single model element is displayed once only and only on one diagram. For BPMN, this works very well, as there is (mostly) a one-to-one mapping between what is seen on the diagram, and what gets represented in the diagram. The same could be said about CMMN. However, Pavanecce also leverages UML and VDML that rely heavily on the ability to display the same model element on a lot of different diagrams. In UML and VDML, when a model element gets updated/deleted from the underlying model, it could affect other diagrams that also render that model element, and this is tricky to do with Oryx. In fact, we intend to integrate this CMMN offering closely with UML models, as UML arguably remains the best modeling language to represent the the structure of the CaseFile (the document taxonomy). In this regard, the Oryx framework is not ideal.

## 5.2. JointJS on Jahia
Another possibility is to use the open source Javascript diagramming API, JointJS, to implement diagrams as custom content types inside Jahia. This would allow us to have a much more fine grained approach to versioning, review and voting workflows, which are built into the Jahia platform by default. Whereas in jBPM Designer and Git, a single model file, such as a CMMN file, would be the "unit of collaboration". However, in Jahia's JCR based framework, any element at any level in the model tree could potentially become the "unit of collaboration".  The 'social' components of these tools such as polling, commenting and wiki's can also add immense value to the collaboration around models. 

JointJS could complement Jahia as its AngularJS based architecture would give it access to Jahia's JCR JSON/REST API. However, JointJS is not a modeling framework, but rather just a useful diagramming API. Rappid is the real thing, but it is not open source.

## 5.3. jBPM Designer's client in Jahia
A third option would be to try to get jBPM's Designer, particularly the client code, to work in Jahia. However,  it is unlikely that we would be able to integrate jBPM Designer as a whole into Jahia, for reasons previously explored.


