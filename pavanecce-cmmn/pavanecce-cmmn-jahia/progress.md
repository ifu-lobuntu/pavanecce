To implement ASAP:
Extend the jBPM6WorkflowProvider
Generate JCR node types (CND) for tasks to store parameters.
Allow OCM objects as values for Parameters, and interpret them correctly when building up the JCR task
Parameters will be either "wants" or "offers" - publish them accordingly against the wanting/offering user
Generate the mod-xxxworkflow.xml spring descriptor
Generate Jahia permissions for tasks based on Role
XPath processDialect on JCR
Pure JCR persistent subscriptions
 - scope of subscriptions
 - bindingRefined
 - throwing CREATE,DELETE,ADD_CHILD,REMOVE_CHILD,ADD_REFERENCE,REMOVE_REFERENCE and UPDATE events

Pure JCR demarcated subscriptions
 - throwing CREATE,DELETE,ADD_CHILD,REMOVE_CHILD,ADD_REFERENCE,REMOVE_REFERENCE and UPDATE events

Pure JCR with xPath as expression language

jBPM on JCR
Create tasks directly under the user and make sure Jahia will interpret it in the correct way
Provide standard templates for tasklists, planning tables, etc. 

