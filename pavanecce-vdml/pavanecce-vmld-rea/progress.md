To implement

Task commitment workflow: 
- confirmPlannedTask
- commitPlan
- getPlannedTaskAsPotentialProposer(by owner, by initiator)
- addProposalToPlannedTask
- selectProposalForTask
- getPlannedTaskAwaitingConfirmation(by owner, by initiator)
- getPlannedTaskAwaitingCommitment(by owner, by initiator)
- introduce state extensions (substates) for planningTableContainers:
-- awaiting confirmation- after submit plan by case owner
-- confirmed- after all items confirmed by actors
-- committed- after all items confirmed by actors and plan owner accepts
- introduce state extensions for planItems and discretionaryItems:
-- awaiting confirmation by selected actor
-- confirmed by selected actor
-- committed by case owner- inputs and actor cannot change after this

Add a 'correlation key' parameter
- for use in VDML
- used to identify tasks
- In expressions inside a Task, make this CaseParameter available. This would help with automatic selection of other parameters based on one input parameter
- once a user has selected the correlation parameter, have a mechanism to calculate values for the other parameters after planning 

Milestone with cost breakdown
- introduce payment service / accounting service with multiple currencies (local, national, etc)
- issue payment automatically when milestone achieved
- a further refinement could even freeze funds on commitment
- issue data to VDML db for tasks whose completion are entry criteria ???? what did I mean here

Want/Offer workflow
Approach one (construction, wantit)
- add a 'want parameter'
-- publish wants somewhere against the case planning user
- add a 'quote/offer parameter'
-- quotes must have a standardized link to the originating user and the task instance
-- multiple quotes should be linked
-- only one quote is selected, and with it the user it came from
-- generate this quote/link as part of the CND file for Jahia
-- put an expression somewhere in the metainfo that calculates the value in currency in terms of the 'want parameter'
Approach two (Kandu, arts and stuffs sold)   
- a task can also be started based on the offer first.
-- the user sees something, an entity, node, content he likes
-- a task, or many tasks, could be associated with this entity, potentially a CaseTask, 
-- the user selects the task and it takes this entity to build a 'quote/offer' parameter (e.g. a quantity would be needed)
-- when the user selects this entity, a task gets created and assigned to the provider, to be confirmed in the commitment workflow
 
Certain tasks could be multicast- think about that a bit, although we would like to make that a bit more 'built-in' with the want/offer workflow

Calendaring
- schedule tasks due date on Google?
- mark certain tasks as meetings and put them on Google Calendar

Allowing SentryInstances and OnPartInstances to be created at runtime
e-mail listener that creates tasks
allow user to associate the task with a case type and start the case 