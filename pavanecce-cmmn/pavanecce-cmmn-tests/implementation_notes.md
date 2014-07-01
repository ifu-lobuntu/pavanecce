# 1. Overview

This document is intended for developers that wish to understand how this CMMN implementation fits on top of jBPM. It discusses the current state of developments, and the approach followed implementing the CMMN specification.
It also discusses a couple of important design designs that were made and the reasoning behind them. Lastly, it highlights some problem areas that still need to be addressed. It assumes a basic understanding of the jBPM
codebase. It also assumes a basic understanding of the various CMMN constructs. I you are entirely new to CMMN, please read this [introduction](../cmmn-intro.md)

# 2. High level project phases.
This CMMN implementation is being built specifically to contribute usable functionality towards the [Pavanecce](http://www.ifu-lobuntu.org/home/projects/pavanecce.html) project. Our priority is getting online 
collaborative communities up and running, hence our initial focus was more on the runtime than the development tooling. We have broken the project up into the following high-level consecutive phases:

- **Phase 1.** Implement Runtime Semantics.
- **Phase 2.** Implement Task Management and Planning User Interface.
- **Phase 3.** Implement Diagram tooling.

# 3. Implement runtime semantics.
This phase is about 80% complete. The vast majority of the CMMN specification's semantics has been implemented, but there are still a couple of grey areas that need to be fleshed out. At this point, the CMMN runtime
contains enough functionality to start using in the other projects at Ifu Lobuntu. 

## 3.1. Approach followed.
The plan initially was to use as much as possible of the jBPM classes out of the box, and attempted to just configure and arrange them in a way that would support the CMMN semantics. We wanted to follow the same
approach as the BPMN extenion of jBPM. jBPM's BPMN extension introduces a couple of new Node types, but does not introduce any new NodeInstances. 
However, CMMN defines very specific life-cycle requirements for PlanItems (Tasks, CaseTasks, Stages, Events etc.). As a result, the need arose to implement custom NodeInstances for the CMMN elements, and hence a
CaseInstanceMarshaller class that supports these NodeInstances. The current and previous state of PlanItemInstances are marshaled as well, which allows the life-cycle to be managed across multiple interactions 
with the CaseInstance. As was expected, we also had to implement different Node classes as well, and eventually also a new NodeFactory class.

## 3.2. Separation of Definitions from Inclusions
One important aspect of CMMN to keep in mind is the separation of the DEFINITION of a PlanItem from it INCLUSION in a Stage or a Case. This is similar to the distinction that certain BPM engines have between 
standalone tasks and embedded tasks. PlanItemDefinitions are like  standalone tasks that need to be included into a process flow. A PlanItemDefinition is included in a Stage or Case by either using a 
user-controlled DiscretionaryItem, or a more 'automated' PlanItem.  This posed some interesting implementation challenges. However, the underlying PlanItemDefinition still needs to execute the same way no matter 
which context it is being used from. 
In order to address this requirement, PlanItemDefinitions, DiscretionaryItems and PlanItems are all just subclasses of the standard jBPM StateNode. The state of the PlanItemDefinition is copied into the 
DiscretionaryItem or PlanItem StateNode. The last step of building up the Case definition therefore uses reflection to physically copy the state from the node representing the definition to the node representing 
the 'inclusion'. This approach works currently and it ensures that the multiple uses of the same PlanItemDefinition don't interfere with each other, but it is not ideal. We're considering some other options here.

## 3.3. Statemachines/Life-cycles
Like the WS Human Tasks specification, CMMN provides well defined state-machines for the life-cycle of the various elements. In order to implement the CMMN specification's specific life-cycle requirements, 
this implementation uses a slightly modified version of the 'State pattern' to manage the life-cycle of Plan elements. A polymorphic enum class, PlanElementState, implements all the validations and transitions. 
The enum literal representing a Plan element's current state gets stored in the process against the node whose state it represents.  There are two different PlanItemInstance life-cycles: one for Tasks and Stages 
and another for Milestones and EventListeners.

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
be created on this transition. From the perspective of a traditional Object Oriented programming language this poses a challenge - implementing a single statemachine across multiple instances. In order to implement
this requirement, two extra class were introduced - the PlanItemInstanceFactoryNode and associated PlanItemInstanceFactoryNodeInstance. When a PlanItem or Discretionary item's entry criteria Sentry becomes 
true, this node gets to decide whether the corresponding PlanItem is required, whether it can repeat, and even whether it is included by the discretion of the user. This adds some complexity to the implementation, 
but it does seem to address the CMMN requirement.   

## 3.4. Updating task statuses from the process
CMMN requires much tighter integration between the TaskService and the Process runtime than what is be the case with standard BPMN. Every instance of a Case, Stage, Human Task, Process Task or Case Task creates an
associated Task in the jBPM TaskService on instantiation, that is, when their entry criteria are met. However, on occasion, a condition is reached within the context of the process instance that requires the 
associated Task to be updated. This is done by sending a 'UpdateTaskStatus' WorkItem to the WorkItemManager using ControllableItemInstanceImpl.triggerTransitionOnTask(PlanItemTransition) method. In the 
WorkItemManager, the UpdateTaskStatusWorkItemHandler class picks up the WorkItem and does interesting things to the associated jBPM Task. The user of this triggerTransitionOnTask method should assume that the 
WorkItem will be delivered asynchronously to the WorkItemManager. Currently, all these queued workItems are delivered in the ObjectPersistence.commit() method, but his is just a temporary 'workaround' 
to fake asynchronous behavior for tests. 

## 3.5. Eventing
Events are central to CMMN - events generated by the persistence engine and the process engine. CMMN also requires very tight integration between the task engine and process engine, which is mediated by WorkItems and
the WorkItemManager. Generally it would seem that this CMMN implementation may benefit from a more asynchronous approach to events and workitems, and this will be implemented over time. However, for testing purposes,
WorkItems and events are delivered synchronously in the ObjectPersistence.commit() method. This is not ideal for a production environment, and truly asynchronous behavior will have to be implemented. However, the
particulars of the implementation of this asynchronous delivery depends on whether this CMMN implementation will run in a non-JTA, non-CDI, non-JMS Jahia/Tomcat environment, or in a Kie Workbench, full JEE JBoss/Wildfly
environment. At least the eventing is pretty well isolated in the AbstractPersistentSubscriptionManager class, and it should not be overly complicated to support different eventing architectures. 

## 3.6. Planning
Planning introduces a new level of control over the processes. Again, these activities are mediated by the TaskService, and are initiated from the PlanningService class which co-ordinates the TaskService and 
the ProcessRuntime. During planning, new 'discretionary' Tasks may be created, but it is essential for them to be initialized from the CaseInstance into which they will be created, as their input parameters 
may very well depend on the current state of the CaseInstance. This communication between the TaskService and ProcessRuntime thus needed to be direct and synchronous, which means that the PlanningService does not
work via the WorkItemManager, but interacts directly with the CaseInstance in question, but it is well worth breaking the encapsulation strategies there. 

## 3.7. Persistence
Along with the tight integration with the Task engine, CMMN also requires tight integration with the persistence engine. CMMN requires a single persistence mechanism for both Variables (CaseFileItems) in 
CaseInstances as well as for CaseParameters on Tasks. CMMN also comes more of a document-oriented, pass-by-reference paradigm rather than the XML-oriented, pass-by-value paradigm of BPMN. As a result, this implementation
had to introduce a whole lot of new of ObjectMarshallingStrategies. A problem we encountered was that the jBPM TaskService did not consistently use an Environment object when doing parameter marshaling, which means 
it did not pick up our new ObjectMarshallingStrategies. We had to implement a couple of custom commands to ensure the parameters are marshaled correctly. During subscription demarcation, we also found that we needed
to make sure that the ObjectMarshallingStrategies used the same instance of the underlying persistence engine to ensure consistent behaviour in the TaskService and the process runtime. This was achieved by letting 
these marshaling strategies use implementations of the ObjectPersistence class from the Environment, which is simple a wrapper around a ThreadLocal persistence engine such as an EntityManager or Jackrabbit
ObjectContentManager. In the tests, the ObjectPersistence implementation is also responsible for ensure that all events are dispatched correctly.

### 3.7.1 JPA
For JPA, we found the fine grained command scoped EntityManager to be problematic for persistence of custom JPA entities. In fact, it does not seem like a good idea in general to have the same persistenceUnit
for custom entities and the core jBPM entities. The JpaCasePersistence also relies on Hibernate event listeners (Flush-entity, post-insert, post-delete) to queue the persistence events, which it then dispatches
just after flushing the underlying EntityManager. Signaling events this way has a slight performance impact. Under certain circumstances, specifically with the CasefFileItem.ADD_XXX and
CaseFileItem.REMOVE_XXX transitions, unininitalized lazy loaded collections need to be initialized to determine membership. Becaus of this, combined with the current synchronous delivery of events,
one can expect a performance impact on Hibernate, which may be alleviated to some extent when events are delivered asynchronously.  

### 3.7.2. Jackrabbit's ObjectContentManager (OCM)
For Jackrabbit's OCM, we followed a similar approach, and the OcmCasePersistence also requires a JCR EventListener to queue persistence events. We implemented support for OCM Events are generated by the JCR 
implementation from a standard JCR EventListener, but assume that the nodes that generate the events are mapped to OCM annotated Java domain classes. Interestingly, the default behaviour for JCR is that these events should be
delivered asynchronously, but thanks to a Jackrabbit, one has the option of delivering JCR persistence events synchronously. A problem we faces is that JCR events do not contain enough information to reconstruct deleted objects
or removed references. Deleted objects passed to the CaseInstance therefore only contain the UUID, but not data. OCM does not have a session based cache for object as JPA does, which makes it a bit slower which is 
a point of concern. It is also not very clear how well supported and active the OCM project is.

### 3.7.3 'Pure' JCR. 
It is also possible to associated standard JCR nodes with processes and tasks. We are currently working on this feature. Whereas Java and MVEL can be used for the JPA and OCM classes, JCR nodes 
would allow us to support XPath as another process dialect. 

## 3.8. Other challenges
Currently, there are a few challenges that still stands in the way of using this implementation in all environments. 

1. The JbpmServicesPersistenceManager is not available to the new commands that were implemented. All these new commands define an input parameter for this class, but the class from which they are 
instantiated can only get hold of the JbpmServicesPerstenceManager via CDI. Since it is very likely that this CMMN implementation will be used from a non-CDI environment, we had to resort to reflection to
get hold of the  JbpmServicesPerstenceManager  in the TaskServiceEndpoint. This is not ideal.
2. We faced a similar problem with the command scoped entitymanager when submitting events, and had to use reflection to get hold of this object.
3. Resolving the correct RuntimeEngine from the RuntimeManager, is problematic from classes such as the Hibernate event listener, whose instantiation we do not control. 

# 4. Implement task management user interface.
This is our next phase and we are about to embark on this phase.
Initially, we were going to use jBPM's GWT based task user interface modeler and infrastructure to render the task forms. However, it turned out that the content management system we currently favor, 
Jahia, also has very solid support for jBPM's task management. It is more closely integrated with content management, and every task's structure is copied to a node in the document database. This has some advantages,
and also allows us to leverage Jahia's templating facilities to automatically generate and easily customize forms for tasks.
 
However, as our implementation of the runtime semantics continued, it became clear that especially the Planning activity in CMMN would require more user interface functionality than what either Jahia or jBPM 
offers out of the box. One specific activity associated with planning is that the planning user can manuallly override task input parameters. Since task parameters always represent documents, passed by reference, 
it is possible for a planner to override and select other parameters for a task. One could also assign the task to another user, or change the due date. Ideally we also give the task management user interface 
access to the WS Human Task people assignments, deadlines and escalations/notifications, which would allow jBPM's task service to be fully utilized. The CMMN Planning activity also allows the planner to create
instances of discretionary task which are however still associated with and co-ordinated by the CaseInstance. It seem that we would have to implement significant amount of new functionality for the task management 
user interface here, whether it is the Kie Workbench's users interface or Jahia's', but no definite decisions have been made yet.

# 5. Implement diagram tooling.
This is the last phase of our CMMN implementation.

No final decisions have been made regarding diagramming, and no work has started on this phase. There are obvious benefits to using the Kie workbench and its built-inn Oryx modeler. However, this CMMN 
implementation forms part of a bigger offering which brings very specific requirements to the table. Firstly, we intend to integrate this CMMN offering closely with UML models. UML still remains the best
modeling language to represent the the structure of the CaseFile (the document taxonomy). We also need to integrate this CMMN oferring closely with VDML models, as participants will be selected based on 
their performance in the collaboration, which will be measure using VDML.

A further requirement is that these models need be established collaboratively by a group of people. This last requirement fits more intuitively with web content management systems such as Jahia, Hippo and 
Magnolia, all of which use JCR and jBPM. For this reason, we are considering integration with one or more of these content management systems, thus leveraging content management facilities such as versioning, 
checking out, checking in and workflows. The 'social' components of these tools such as polling, commenting and wiki's can also add immense value to the collaboration around models.

The Oryx modeler also poses a couple of problems. The original Oryx code is a bit hairy at times. The jBPM team did well in cleaning this up, but it just seems like it would be quite difficult to change some of the 
things the original Oryx developers took for granted. One specific problem is that Oryx does not support the separation of the diagram from the model very well. This works fine, especially for process flow modeling
languages, where a model element only appears on a single diagram. However, UML, VDML and to a lesser extent CMMN require the idea of a model that is separate from the diagrams from which they are viewed.
The same model element can appear on many different diagrams, and when it gets updated/deleted, it could affect these other diagrams. It does not seem like Oryx supports this line of thinking, which is a pity. 
The open source world really needs a nice, solid, model based web diagraming framework.

However, we are still hopeful that we can surmount these obstacles. Ideally there would be a way to make this work at least with the Oryx modeler from the Kie Workbench, but it is unlikely that we would be able 
to integrate Kie as a whole into Jahia, for instance.

