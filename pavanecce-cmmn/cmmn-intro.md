# 1. Overview.
If you are reading this document, you probably have been exposed to work-flow and business process management technologies before. In some ways, CMMN is nothing new, in other ways it is radically new. CMMN 
has retained some usual suspects such as Human Tasks and Events, but others are conspicuously absent, such as decision nodes and looping structures. However, it introduces three things that make it refreshingly new
1. Users are given an entirely new level of control over a process.
2. It is very much event-driven - events from persistence engine, events from other nodes in the process.
3. It defines tight integration with hierarchical persistence storage, and it fits very well with document oriented storage such as content management systems or document databases.
All in all, it represents a significant shift away from sequential, predictive processes towards reactive, event-driven, adaptive process. 

CMMN is a relatively simple modeling language, with a very simple notation associated with it. However, the constructs introduced by the CMMN specification in themselves are loaded with meaning, and
this document will look into these constructs without considering the notation just yet. Examples of the CMMN notation can be explored in the CMMN specification itself. 

# 2. The Basics
At the root of a Case definition, one finds 

## 2.1. CaseFileItemDefinitions. 
These are definitions of the data structures that will be used in the Case. Generally, these definitions merely point to an external source for the definition of the data structures such as an XML Schema, 
or a UML model, or document structure definition and/or taxonomy.

## 2.2. The Case. 
Like any other process definition, a Case has input parameters and output parameters, and each of them has a type referring to one of the data structure definitions mentioned earlier, the CaseFileItemDefinitions. 
A Case also defines Roles that are typically associated with work in the Case, not unlike swim-lanes in other process languages. Lastly, the Case element contains the CasePlanModel, which is the root container 
for all work and event definitions defined in the case.

# 3. CasePlanModel
The CasePlanModel in the Case fulfills a very specific and important role. Firstly, it defines the structure of the CaseFile. A CaseFile is a hierarchical data structure that stores data relevant to the execution
of the case. It contains individual items called CaseFileItems that in turn may contain other CaseFileItems. These could be thought of as the 'variables' stored in the process.

The CasePlanModel also defines all the the tasks that people or other software can perform and the events that can occur. It also defines  containers, or groups of work and events called Stages that can be 
used explicitly include tasks and events in a flow of events. CMMN refers to these events, work and groups of work as PlanItemDefinitions. 

However, on their own, PlanItemDefinitions are not very useful - they need to be used, or included a specific context in a flow of events. At the top level, the CasePlanModel defines its own internal flow of events
 - which tasks it uses, which events it monitors, which tasks are at the discretion of the user, and how all of these items related to each 
other. When a PlanItemDefinition is included in a flow of events, it is done by means of either a PlanItem, which is an automated inclusion, or by means of a DiscretionaryItem, which means it's inclusion is at 
the discretion of a user. 

# 4.PlanItemDefinitions 
Let's first look at the PlanItemDefinitions. PlanItemDefinitions come in two flavors: those that involve work, such as Human Tasks, Process Tasks, Case Tasks and Stages, and those that simply happen, such as UserEvents,
TimerEvents and Milestones. The first group have a fairly complex life-cycle that can be controlled extensively by a user. The second group have simple life-cycles the involve the occurrence of something. 

## 4.1. Human Tasks
Almost all process flow languages have representation for the concept of a Human Task. A Human Task has input parameters, which a user is expected to either operate on, or at least use as information to do some work. 
It also defines output parameters, which could be seen as something that may be produced during the execution of the Human Task, but in a world where data is passed by reference, this may not always be the case. A
Human Task also defines the one Role from the Case which is required from the person performing the task. 

## 4.2. Process Tasks
A Process Task is used to call another process from the process engine. Almost all processes are supported, such as BPEL, XPDL, and BPMN. The Process Task defines how it maps its input parameters to the process
being called, and how the output of the process is mapped to its output parameters.

## 4.3 Case Tasks
A Case Task is almost like a Process Task, except that it know that the process being called is a Case. 

## 4.4 More about Tasks
All Tasks, such as Human Task have input parameters and output parameters, defined as CaseParameters. CaseParameters are bound to CaseFileItems. IN fact, it could also have a refinement of this binding to specify
exactly which instance of the CaseFileItem it is bound to. When a task input parameter is bound to a CaseFileItem, that binding is used to determine which data to send to the receiver of the task, be it a human, 
a process or another Case. With task output parameters a binding similarly implies which CaseFileItem the task will 'write' its output to, although in a world of pass-by-reference data structures, this isn't always
too important. However, with task output parameters, that binding has one additional implication: during the execution of that task, the case will be listening to events that are produced by the data structure, or 
'object' in question, i.e. the CaseFileItem that it is bound to. Typical events are 
- the creation of a new instance
- the deletion of an existing instance
- adding children to or removing them from the instance
- adding references to or removing them from the instance.
Once the task is completed, the Case no longer listens to events from the objects its output CaseParameters were bound to.
Lastly, all tasks can be marked as being 'non-blocking'. This means, the process is not dependent on its completion.  

## 4.5. User Events
User events are event that can be triggered by and has direct meaning for a User. These are events significant to the progress of the process, but that are not generated by the persisterelated nce storage or the process engine
itself. Only users fulfilling a certain Role in the Case are allowed to trigger the User EVetn 

## 4.6. Timer Events
Timer events are merely events that occur, once or repetitively, based on a time interval since it became relevant. A Timer Event becomes relevant when its parent Case or Stage became active, or additionally when a
certain trigger occurs, such as the occurrence of a specific persistence or process event.

## 4.7. Milestones
Milestones mark significant occurrences in the process when certain conditions became true.

## 4.8. Stages
A Stage is where the modeler can use other PlanItemDefinitions (tasks/events) and link them up, tie them to each other to form a new flow of events that performs a specific chunk of work. In fact, the CasePlanModel
itself is also a Stage, but it has the additional ability to carry PlanItemDefinitions. Other Stages in the CasePlanModel are not allowed to have PlanItemDefinitions, and are merely allowed to connect existing 
PlanItemDefinitions up in new and interesting ways.

# 5. PlanItems
PlanItems are used to include a PlanItemDefinition in a work container such as a Stage or the CasePlanModel. A PlanItem has to refer to a definition - it has no useful work or event related information in itself, but
rather represents a mechanism for bringing the PlanItemDefinition's work or event definition into the Stage/CasePlanModel. In its simplest form, a PlanItem enables, or activates the Task or Stage it refers to when its parent 
Stage/CasePlanModel becomes active. Clearly this model of activation is very simplistic, but luckily CMMN can make things much more complicated.

A second method of activation for a PlanItem is when it has one or more Sentries attached to it that define a certain Condition. The Condition is an expression in a language such as XPath, Java or MVEL that is evaluated
in terms of the CaseFileItems in the CaseFile. When the Condition of a Sentry evaluates to true, we say that the entry criteria for the PlanItem have become true, and the PlanItem is enabled or activated. A PlanItem 
only requires one of its Sentries to become true to be activated or enabled. 

A third method of activation for a PlanItem is when a Sentry that is attached are actually subscribing to specific persistence or process events. Persistence events are called CaseFileItem events, and process events
are called PlanItemEvents. Only instances of PlanItems generate events - PlanItemDefinitions in themselves do not represent active process elements, and therefore do not generate events. The subscription to events are
done by means of OnParts - CaseFileItemOnParts and PlanItemOnParts. When all of the events that the Sentry has thus subscribed to have occurred, the the entry criteria of the PlanItem are considered to be true and the
associated Task or Stage is instantiated. But in this scenario another rule comes into play: based on the RepetitionRule associated with the PlanItem, a new instance may be created either once only, or every time the 
PlanItem's entry criteria become true. Thus a new HumanTask may be created every time a new instance is created of a certain object or document.

The fourth and most complex method of activation of a PlanItem is when it has a Sentry that has both OnParts and a Condition. When all the events subscribed to by the OnParts have occurred, the Sentry's Condition is 
evaluated. In this case, only if the Condition evaluates to 'true' will the PlanItem's entry criteria considered to be 'true', and the associated Task or Stage will be instantiated.

As mentioned before, instances of PlanItems generate events that other PlanItems can subscribe to by means of PlanItemOnParts on their Sentries. Some of the more interesting events that PlanItem instances generate are
- when it is activated, i.e. when the work it represents is being performed.
- when it is completed.
- when it is suspended or resumed.
- when it is terminated by a user
- when it is disabled during a planning activity by a user authorized to do planning.

Lastly, just like a PlanItem is activated when its entry criteria become true, it could also have exit criteria associated with it, which could see the PlanItemInstance being 'exited'. Again, all the different 
combinations of Sentries, Conditions and OnParts come to play, just as before. The only difference is that, as exit criteria, their occurrence brings the PlanItemInstance into a terminal state. No surprise, this
'exiting' of a PlanItem instance itself generates an event, called the 'exit' event.

As you can see, PlanItems, Sentries and OnParts allow the modeler to design an extremely reactive, event-driven workflow, but they still have one restriction: they are included automatically in the flow of events,
and the users have to deal with them in one way or another.  

# 6. DiscretionaryItems
DiscretionaryItems allow modelers to bring PlanItemDefinitions into the fray, but only at the discretion of a user. DiscretionaryItems are grouped together in a PlanningTable, and both the PlanningTable and the
DiscretionaryItems themselves define which Roles from the Case are allowed to instantiate the DiscretionaryItem's associated PlanItemDefinition. However, since the presence of a DiscretionaryItem in the Case instance
cannot be guaranteed, it has one very important consequence: other PlanItems cannot subscribe to events generated by the instances of the DiscretionaryItem. 
The CasePlanModel of a Case can define a PlanningTable, in which case, when a DiscretionaryItem is instantiated by the user, the associated instance is created in the CasePlanModel. It is the same with a Stage - it can 
define a PlanningTable, and the resulting instances of the DiscretionaryItem is added to the Stage instance. In both these cases, the planning activity, that is the activity where DiscretionaryItems included or excluded,
is merely assumed to take place at some point. This planning activity is not represented in the model itself. However, there is a third possible place where a PlanningTable may be defined - in  a HumanTask itself. In 
this case, the planning activity can be considered as part of the HumanTask in question, and any resulting instances of PlanItemDefinitions are created in the Stage or the CasePlanModel that contains the HumanTask via
a PlanItem or DiscretionaryItem.

# 7. More about Planning
The CMMN specification is not overly specific regarding what could happen during the planning activity. It is clear about the fact that the decision to include or exclude DiscretionaryItems is made during the planning
activity. However, given the flexibility of CMMN, it would also be possible to do some other things during the planning activity. Users that fulfill the role to do planning on a DiscretionaryItem could potentially
also select different input parameters to HumanTasks, CaseTasks or ProcessTasks. Such users could potentially also assign HumanTasks to other users. In fact, when enriching the HumanTasks in CMMN with the WS Human Task
standard, it is also conceivable that planning users could change the stakeholders, the business administrators and potential owners of a task. Deadlines, and the escalations that are followed when the deadline occurs
could also be configured during this activity.

# 8. The Notation.
Please view the [CMMN Specification](CMMN.pdf) for examples of the various CMMN modeling elements.
 
 
