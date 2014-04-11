package org.pavanecce.cmmn.flow.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.drools.core.time.impl.DefaultJobHandle;
import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.kie.api.definition.process.Node;
import org.pavanecce.cmmn.flow.CaseFileItemDefinition;
import org.pavanecce.cmmn.flow.Definitions;
import org.pavanecce.cmmn.flow.OnCaseFileItemPart;
import org.pavanecce.cmmn.flow.OnPart;
import org.pavanecce.cmmn.flow.OnPlanItemPart;
import org.pavanecce.cmmn.flow.PlanItem;
import org.pavanecce.cmmn.flow.PlanItemDefinition;
import org.pavanecce.cmmn.flow.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class CaseHandler extends BaseAbstractHandler implements Handler {
	private static String DEFAULT = org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE;

	private static final Logger logger = LoggerFactory.getLogger(CaseHandler.class);

	public static final String CONNECTIONS = "BPMN.Connections";
	public static final String LINKS = "BPMN.ThrowLinks";
	public static final String ASSOCIATIONS = "BPMN.Associations";
	public static final String ERRORS = "BPMN.Errors";
	public static final String ESCALATIONS = "BPMN.Escalations";

	static final String PROCESS_INSTANCE_SIGNAL_EVENT = "kcontext.getProcessInstance().signalEvent(\"";
	static final String RUNTIME_SIGNAL_EVENT = "kcontext.getKnowledgeRuntime().signalEvent(\"";

	@SuppressWarnings("unchecked")
	public CaseHandler() {
		if ((this.validParents == null) && (this.validPeers == null)) {
			this.validParents = new HashSet();
			this.validParents.add(Definitions.class);

			this.validPeers = new HashSet();
			this.validPeers.add(null);
			this.validPeers.add(CaseFileItemDefinition.class);
			this.validPeers.add(RuleFlowProcess.class);

		}
	}

	public Object start(final String uri, final String localName, final Attributes attrs, final ExtensibleXmlParser parser) throws SAXException {
		parser.startElementBuilder(localName, attrs);

		String id = attrs.getValue("id");
		String name = attrs.getValue("name");
		String packageName = attrs.getValue("http://www.jboss.org/drools", "packageName");
		// String dynamic = attrs.getValue("http://www.jboss.org/drools",
		// "adHoc");
		// String version = attrs.getValue("http://www.jboss.org/drools",
		// "version");

		RuleFlowProcess process = new RuleFlowProcess();
		process.setId(id);
		if (name == null) {
			name = id;
		}
		process.setName(name);
		process.setType("RuleFlow");
		if (packageName == null) {
			packageName = "org.pavanecce.cmmn";
		}
		process.setPackageName(packageName);
		process.setDynamic(true);
		process.setAutoComplete(false);
		// if (version != null) {
		// process.setVersion(version);
		// }

		((ProcessBuildData) parser.getData()).addProcess(process);
		// register the definitions object as metadata of process.
		// process.setMetaData("Definitions", parser.getParent());
		// register bpmn2 imports as meta data of process
		// for unique id's of nodes, start with one to avoid returning wrong
		// nodes for dynamic nodes
		parser.getMetaData().put("idGen", new AtomicInteger(1));
		StartNode start = new StartNode();
		start.setId(0);
		process.addNode(start);
		start.setName("defaultStart");
		Split split = new Split(Split.TYPE_AND);
		split.setId(1);
		process.addNode(split);
		split.setName("defaultSplit");
		new ConnectionImpl(start,DEFAULT,split,DEFAULT);
		EndNode end = new EndNode();
		end.setName("defaultEnd");
		end.setId(2);
		process.addNode(end);
		return process;
	}

	@SuppressWarnings("unchecked")
	public Object end(final String uri, final String localName, final ExtensibleXmlParser parser) throws SAXException {
		parser.endElementBuilder();

		RuleFlowProcess process = (RuleFlowProcess) parser.getCurrent();
		Node defaultStart = process.getNode(1);
		Node defaultEnd = process.getNode(2);
		Join defaultJoin=null;
		
		//TODO rather check if there is no final node - exitCriteria will be final nodes
		if(defaultEnd.getIncomingConnections().isEmpty()){
			defaultJoin=new Join();
			defaultJoin.setId(3);
			defaultJoin.setType(Join.TYPE_AND);
			process.addNode(defaultJoin);
			new ConnectionImpl(defaultJoin, DEFAULT, defaultEnd, DEFAULT);
		}
		for (Node node : process.getNodes()) {
			if (node instanceof PlanItem) {
				PlanItem planItem = (PlanItem) node;
				if (defaultJoin!=null) {
					new ConnectionImpl(planItem, DEFAULT, defaultJoin, DEFAULT);
				}
				planItem.setDefinition(findPlanItemDefinition(process,planItem.getDefinitionRef()));
				for (String string : new ArrayList<String>(planItem.getEntryCriteria().keySet())) {
					Sentry entry = findSentry(process,string);
					planItem.putEntryCriterion(string, entry);
					new ConnectionImpl(entry, DEFAULT, planItem, DEFAULT);
				}
				for (String string : new ArrayList<String>(planItem.getExitCriteria().keySet())) {
					Sentry exit = findSentry(process,string);
					planItem.putExitCriterion(string, exit);
				}
			} else if (node instanceof Sentry) {
				new ConnectionImpl(defaultStart, DEFAULT, node, DEFAULT);
				Sentry sentry = (Sentry) node;
				for (OnPart onPart : sentry.getOnParts()) {
					if(onPart instanceof OnPlanItemPart){
						OnPlanItemPart apip=(OnPlanItemPart) onPart;
						apip.setPlanItem(findPlanItem(process, apip.getSourceRef()));
					}else{
						OnCaseFileItemPart ocfip=(OnCaseFileItemPart) onPart;
						ocfip.setCaseFileItem(process.getVariableScope().findVariable(ocfip.getSourceRef()));
					}
				}
			}
		}
		// 1. Link OnParts
		// 2. Link CaseParameters to CaseItems
		// 3. Link Parameter Mappings
		// List<IntermediateLink> throwLinks = (List<IntermediateLink>) process
		// .getMetaData(LINKS);
		// linkIntermediateLinks(process, throwLinks);
		//
		// List<SequenceFlow> connections = (List<SequenceFlow>)
		// process.getMetaData(CONNECTIONS);
		// linkConnections(process, connections);
		// linkBoundaryEvents(process);
		//
		// // This must be done *after* linkConnections(process, connections)
		// // because it adds hidden connections for compensations
		// List<Association> associations = (List<Association>)
		// process.getMetaData(ASSOCIATIONS);
		// linkAssociations((Definitions) process.getMetaData("Definitions"),
		// process, associations);
		//
		// List<Lane> lanes = (List<Lane>)
		// process.getMetaData(LaneHandler.LANES);
		// assignLanes(process, lanes);
		// postProcessNodes(process, process);
		return process;
	}

	private PlanItem findPlanItem(RuleFlowProcess process, String sourceRef) {
		for (Node node : process.getNodes()) {
			if(node instanceof PlanItem && ((PlanItem)node).getElementId().equals(sourceRef)){
				return (PlanItem) node;
			}
		}
		return null;
	}

	private PlanItemDefinition findPlanItemDefinition(RuleFlowProcess process, String definitionRef) {
		for (Node node : process.getNodes()) {
			if(node instanceof PlanItemDefinition && ((PlanItemDefinition)node).getElementId().equals(definitionRef)){
				return (PlanItemDefinition) node;
			}
		}
		return null;
	}

	private Sentry findSentry(RuleFlowProcess process, String elementId) {
		Node[] nodes = process.getNodes();
		for (Node node : nodes) {
			if(node instanceof Sentry && ((Sentry) node).getElementId().equals(elementId)){
				return (Sentry) node;
			}
		}
		return null;
	}

	public Class<?> generateNodeFor() {
		return RuleFlowProcess.class;
	}

	// public static void linkIntermediateLinks(NodeContainer process,
	// List<IntermediateLink> links) {
	//
	// if (null != links) {
	//
	// // Search throw links
	// ArrayList<IntermediateLink> throwLinks = new
	// ArrayList<IntermediateLink>();
	// for (IntermediateLink aLinks : links) {
	// if (aLinks.isThrowLink()) {
	// throwLinks.add(aLinks);
	// }
	// }
	//
	// // Look for catch links for a throw link
	// for (IntermediateLink throwLink : throwLinks) {
	//
	// ArrayList<IntermediateLink> linksWithSharedNames = new
	// ArrayList<IntermediateLink>();
	// for (IntermediateLink aLink : links) {
	// if (throwLink.getName().equals(aLink.getName())) {
	// linksWithSharedNames.add(aLink);
	// }
	// }
	//
	// if (linksWithSharedNames.size() < 2) {
	// throw new IllegalArgumentException(
	// "There should be at least 2 link events to make a connection");
	// }
	//
	// linksWithSharedNames.remove(throwLink);
	//
	// // Make the connections
	// Node t = findNodeByIdOrUniqueIdInMetadata(process,
	// throwLink.getUniqueId());
	//
	// // connect throw to catch
	// for (IntermediateLink catchLink : linksWithSharedNames) {
	//
	// Node c = findNodeByIdOrUniqueIdInMetadata(process,
	// catchLink.getUniqueId());
	// if (t != null && c != null) {
	// Connection result = new ConnectionImpl(t,
	// NodeImpl.CONNECTION_DEFAULT_TYPE, c,
	// NodeImpl.CONNECTION_DEFAULT_TYPE);
	// result.setMetaData("linkNodeHidden", "yes");
	// }
	// }
	//
	// // Remove processed links
	// links.remove(throwLink);
	// links.removeAll(linksWithSharedNames);
	// }
	//
	// if (links.size() > 0) {
	// throw new IllegalArgumentException(links.size()
	// + " links were not processed");
	// }
	//
	// }
	// }
	//
	// private static Object findNodeOrDataStoreByUniqueId(Definitions
	// definitions, NodeContainer nodeContainer, final String nodeRef, String
	// errorMsg) {
	// if( definitions != null ) {
	// List<DataStore> dataStores = definitions.getDataStores();
	// if( dataStores != null ) {
	// for( DataStore dataStore : dataStores ) {
	// if( nodeRef.equals(dataStore.getId()) ) {
	// return dataStore;
	// }
	// }
	// }
	// }
	// return findNodeByIdOrUniqueIdInMetadata(nodeContainer, nodeRef,
	// errorMsg);
	// }
	//
	// private static Node findNodeByIdOrUniqueIdInMetadata(
	// NodeContainer nodeContainer, String targetRef) {
	// return findNodeByIdOrUniqueIdInMetadata(nodeContainer, targetRef,
	// "Could not find target node for connection:" + targetRef);
	// }
	//
	// private static Node findNodeByIdOrUniqueIdInMetadata(NodeContainer
	// nodeContainer, final String nodeRef, String errorMsg) {
	// Node node = null;
	// // try looking for a node with same "UniqueId" (in metadata)
	// for (Node containerNode: nodeContainer.getNodes()) {
	// if (nodeRef.equals(containerNode.getMetaData().get("UniqueId"))) {
	// node = containerNode;
	// break;
	// }
	// }
	// if (node == null) {
	// throw new IllegalArgumentException(errorMsg);
	// }
	// return node;
	// }
	//
	//
	// public static void linkConnections(NodeContainer nodeContainer,
	// List<SequenceFlow> connections) {
	// if (connections != null) {
	// for (SequenceFlow connection: connections) {
	// String sourceRef = connection.getSourceRef();
	// Node source = findNodeByIdOrUniqueIdInMetadata(nodeContainer, sourceRef,
	// "Could not find source node for connection:" + sourceRef);
	//
	// if (source instanceof EventNode) {
	// for (EventFilter eventFilter : ((EventNode) source).getEventFilters()) {
	// if (eventFilter instanceof EventTypeFilter) {
	// if ("Compensation".equals(((EventTypeFilter) eventFilter).getType())) {
	// // While this isn't explicitly stated in the spec,
	// // BPMN Method & Style, 2nd Ed. (Silver), states this on P. 131
	// throw new IllegalArgumentException(
	// "A Compensation Boundary Event can only be *associated* with a compensation activity via an Association, not via a Sequence Flow element.");
	// }
	// }
	// }
	// }
	//
	// String targetRef = connection.getTargetRef();
	// Node target = findNodeByIdOrUniqueIdInMetadata(nodeContainer, targetRef,
	// "Could not find target node for connection:" + targetRef);
	//
	//
	// Connection result = new ConnectionImpl(
	// source, NodeImpl.CONNECTION_DEFAULT_TYPE,
	// target, NodeImpl.CONNECTION_DEFAULT_TYPE);
	// result.setMetaData("bendpoints", connection.getBendpoints());
	// result.setMetaData("UniqueId", connection.getId());
	//
	// if ("true".equals(System.getProperty("jbpm.enable.multi.con"))){
	// NodeImpl nodeImpl = (NodeImpl) source;
	// Constraint constraint = buildConstraint(connection, nodeImpl);
	// if (constraint != null) {
	// nodeImpl.addConstraint(new ConnectionRef(target.getId(),
	// NodeImpl.CONNECTION_DEFAULT_TYPE),
	// constraint);
	// }
	//
	// } else if (source instanceof Split) {
	// Split split = (Split) source;
	// Constraint constraint = buildConstraint(connection, split);
	// split.addConstraint(
	// new ConnectionRef(target.getId(), NodeImpl.CONNECTION_DEFAULT_TYPE),
	// constraint);
	// }
	// }
	// }
	// }
	//
	//
	// public static void linkBoundaryEvents(NodeContainer nodeContainer) {
	// for (Node node: nodeContainer.getNodes()) {
	// if (node instanceof EventNode) {
	// final String attachedTo = (String) node.getMetaData().get("AttachedTo");
	// if (attachedTo != null) {
	// for( EventFilter filter : ((EventNode) node).getEventFilters() ) {
	// String type = ((EventTypeFilter) filter).getType();
	// Node attachedNode = findNodeByIdOrUniqueIdInMetadata(nodeContainer,
	// attachedTo, "Could not find node to attach to: " + attachedTo);
	//
	// //
	// if (!(attachedNode instanceof StateBasedNode) &&
	// !type.equals("Compensation")) {
	// throw new
	// IllegalArgumentException("Boundary events are supported only on StateBasedNode, found node: "
	// + attachedNode.getClass().getName() + " [" +
	// attachedNode.getMetaData().get("UniqueId") + "]");
	// }
	//
	// if (type.startsWith("Escalation")) {
	// linkBoundaryEscalationEvent(nodeContainer, node, attachedTo,
	// attachedNode);
	// } else if (type.startsWith("Error-")) {
	// linkBoundaryErrorEvent(nodeContainer, node, attachedTo, attachedNode);
	// } else if (type.startsWith("Timer-")) {
	// linkBoundaryTimerEvent(nodeContainer, node, attachedTo, attachedNode);
	// } else if (type.equals("Compensation")) {
	// linkBoundaryCompensationEvent(nodeContainer, node, attachedTo,
	// attachedNode);
	// } else if (node.getMetaData().get("SignalName") != null ||
	// type.startsWith("Message-")) {
	// linkBoundarySignalEvent(nodeContainer, node, attachedTo, attachedNode);
	// } else if (type.startsWith("Condition-")) {
	// linkBoundaryConditionEvent(nodeContainer, node, attachedTo,
	// attachedNode);
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// private static void linkBoundaryEscalationEvent(NodeContainer
	// nodeContainer, Node node, String attachedTo, Node attachedNode) {
	// boolean cancelActivity = (Boolean)
	// node.getMetaData().get("CancelActivity");
	// String escalationCode = (String)
	// node.getMetaData().get("EscalationEvent");
	//
	// ContextContainer compositeNode = (ContextContainer) attachedNode;
	// ExceptionScope exceptionScope = (ExceptionScope)
	// compositeNode.getDefaultContext(ExceptionScope.EXCEPTION_SCOPE);
	// if (exceptionScope == null) {
	// exceptionScope = new ExceptionScope();
	// compositeNode.addContext(exceptionScope);
	// compositeNode.setDefaultContext(exceptionScope);
	// }
	//
	// ActionExceptionHandler exceptionHandler = new ActionExceptionHandler();
	// DroolsConsequenceAction action = new DroolsConsequenceAction("java",
	// PROCESS_INSTANCE_SIGNAL_EVENT + "Escalation-" + attachedTo + "-" +
	// escalationCode + "\", null);");
	//
	// exceptionHandler.setAction(action);
	// exceptionScope.setExceptionHandler(escalationCode, exceptionHandler);
	//
	// if (cancelActivity) {
	// List<DroolsAction> actions =
	// ((EventNode)node).getActions(EndNode.EVENT_NODE_EXIT);
	// if (actions == null) {
	// actions = new ArrayList<DroolsAction>();
	// }
	// DroolsConsequenceAction cancelAction = new
	// DroolsConsequenceAction("java", null);
	// cancelAction.setMetaData("Action", new
	// CancelNodeInstanceAction(attachedTo));
	// actions.add(cancelAction);
	// ((EventNode)node).setActions(EndNode.EVENT_NODE_EXIT, actions);
	// }
	// }
	//
	// private static void linkBoundaryErrorEvent(NodeContainer nodeContainer,
	// Node node, String attachedTo, Node attachedNode) {
	// ContextContainer compositeNode = (ContextContainer) attachedNode;
	// ExceptionScope exceptionScope = (ExceptionScope)
	// compositeNode.getDefaultContext(ExceptionScope.EXCEPTION_SCOPE);
	// if (exceptionScope == null) {
	// exceptionScope = new ExceptionScope();
	// compositeNode.addContext(exceptionScope);
	// compositeNode.setDefaultContext(exceptionScope);
	// }
	// String errorCode = (String) node.getMetaData().get("ErrorEvent");
	// ActionExceptionHandler exceptionHandler = new ActionExceptionHandler();
	//
	// DroolsConsequenceAction action = new DroolsConsequenceAction("java",
	// PROCESS_INSTANCE_SIGNAL_EVENT + "Error-" + attachedTo + "-" + errorCode +
	// "\", null);");
	//
	// exceptionHandler.setAction(action);
	// exceptionScope.setExceptionHandler(errorCode, exceptionHandler);
	//
	// List<DroolsAction> actions =
	// ((EventNode)node).getActions(EndNode.EVENT_NODE_EXIT);
	// if (actions == null) {
	// actions = new ArrayList<DroolsAction>();
	// }
	// DroolsConsequenceAction cancelAction = new
	// DroolsConsequenceAction("java", null);
	// cancelAction.setMetaData("Action", new
	// CancelNodeInstanceAction(attachedTo));
	// actions.add(cancelAction);
	// ((EventNode)node).setActions(EndNode.EVENT_NODE_EXIT, actions);
	// }
	//
	// private static void linkBoundaryTimerEvent(NodeContainer nodeContainer,
	// Node node, String attachedTo, Node attachedNode) {
	// boolean cancelActivity = (Boolean)
	// node.getMetaData().get("CancelActivity");
	// StateBasedNode compositeNode = (StateBasedNode) attachedNode;
	// String timeDuration = (String) node.getMetaData().get("TimeDuration");
	// String timeCycle = (String) node.getMetaData().get("TimeCycle");
	// String timeDate = (String) node.getMetaData().get("TimeDate");
	// Timer timer = new Timer();
	// if (timeDuration != null) {
	// timer.setDelay(timeDuration);
	// timer.setTimeType(Timer.TIME_DURATION);
	// compositeNode.addTimer(timer, new DroolsConsequenceAction("java",
	// PROCESS_INSTANCE_SIGNAL_EVENT + "Timer-" + attachedTo + "-" +
	// timeDuration + "\", null);"));
	// } else if (timeCycle != null) {
	// int index = timeCycle.indexOf("###");
	// if (index != -1) {
	// String period = timeCycle.substring(index + 3);
	// timeCycle = timeCycle.substring(0, index);
	// timer.setPeriod(period);
	// }
	// timer.setDelay(timeCycle);
	// timer.setTimeType(Timer.TIME_CYCLE);
	// compositeNode.addTimer(timer, new DroolsConsequenceAction("java",
	// PROCESS_INSTANCE_SIGNAL_EVENT + "Timer-" + attachedTo + "-" + timeCycle +
	// (timer.getPeriod() == null ? "" : "###" + timer.getPeriod()) +
	// "\", null);"));
	// } else if (timeDate != null) {
	// timer.setDate(timeDate);
	// timer.setTimeType(Timer.TIME_DATE);
	// compositeNode.addTimer(timer, new DroolsConsequenceAction("java",
	// PROCESS_INSTANCE_SIGNAL_EVENT + "Timer-" + attachedTo + "-" + timeDate +
	// "\", null);"));
	// }
	//
	// if (cancelActivity) {
	// List<DroolsAction> actions =
	// ((EventNode)node).getActions(EndNode.EVENT_NODE_EXIT);
	// if (actions == null) {
	// actions = new ArrayList<DroolsAction>();
	// }
	// DroolsConsequenceAction cancelAction = new
	// DroolsConsequenceAction("java", null);
	// cancelAction.setMetaData("Action", new
	// CancelNodeInstanceAction(attachedTo));
	// actions.add(cancelAction);
	// ((EventNode)node).setActions(EndNode.EVENT_NODE_EXIT, actions);
	// }
	// }
	//
	// private static void linkBoundaryCompensationEvent(NodeContainer
	// nodeContainer, Node node, String attachedTo, Node attachedNode) {
	// /**
	// * BPMN2 Spec, p. 264:
	// * "For an Intermediate event attached to the boundary of an activity:"
	// * ...
	// * The Activity the Event is attached to will provide the Id necessary
	// * to match the Compensation Event with the Event that threw the
	// compensation"
	// *
	// * In other words: "activityRef" is and should be IGNORED
	// */
	//
	// String activityRef = (String) node.getMetaData().get("ActivityRef");
	// if( activityRef != null ) {
	// logger.warn("Attribute activityRef={} will be IGNORED since this is a Boundary Compensation Event.",
	// activityRef);
	// }
	//
	// // linkAssociations takes care of the rest
	// }
	//
	// private static void linkBoundarySignalEvent(NodeContainer nodeContainer,
	// Node node, String attachedTo, Node attachedNode) {
	// boolean cancelActivity = (Boolean)
	// node.getMetaData().get("CancelActivity");
	// if (cancelActivity) {
	// List<DroolsAction> actions =
	// ((EventNode)node).getActions(EndNode.EVENT_NODE_EXIT);
	// if (actions == null) {
	// actions = new ArrayList<DroolsAction>();
	// }
	// DroolsConsequenceAction action = new DroolsConsequenceAction("java",
	// null);
	// action.setMetaData("Action", new CancelNodeInstanceAction(attachedTo));
	// actions.add(action);
	// ((EventNode)node).setActions(EndNode.EVENT_NODE_EXIT, actions);
	// }
	// }
	//
	// private static void linkBoundaryConditionEvent(NodeContainer
	// nodeContainer, Node node, String attachedTo, Node attachedNode) {
	// String processId = ((RuleFlowProcess) nodeContainer).getId();
	// String eventType = "RuleFlowStateEvent-" + processId + "-" + ((EventNode)
	// node).getUniqueId() + "-" + attachedTo;
	// ((EventTypeFilter) ((EventNode)
	// node).getEventFilters().get(0)).setType(eventType);
	// boolean cancelActivity = (Boolean)
	// node.getMetaData().get("CancelActivity");
	// if (cancelActivity) {
	// List<DroolsAction> actions =
	// ((EventNode)node).getActions(EndNode.EVENT_NODE_EXIT);
	// if (actions == null) {
	// actions = new ArrayList<DroolsAction>();
	// }
	// DroolsConsequenceAction action = new DroolsConsequenceAction("java",
	// null);
	// action.setMetaData("Action", new CancelNodeInstanceAction(attachedTo));
	// actions.add(action);
	// ((EventNode)node).setActions(EndNode.EVENT_NODE_EXIT, actions);
	// }
	// }
	//
	// public static void linkAssociations(Definitions definitions,
	// NodeContainer nodeContainer, List<Association> associations) {
	// if( associations != null ) {
	// for( Association association : associations ) {
	// String sourceRef = association.getSourceRef();
	// Object source = null;
	// try {
	// source = findNodeOrDataStoreByUniqueId(definitions, nodeContainer,
	// sourceRef,
	// "Could not find source [" + sourceRef + "] for association " +
	// association.getId() + "]" );
	// } catch (IllegalArgumentException e) {
	// // source not found
	// }
	// String targetRef = association.getTargetRef();
	// Object target = null;
	// try {
	// target = findNodeOrDataStoreByUniqueId(definitions, nodeContainer,
	// targetRef,
	// "Could not find target [" + targetRef + "] for association [" +
	// association.getId() + "]" );
	// } catch (IllegalArgumentException e) {
	// // target not found
	// }
	// if (source == null || target == null) {
	// // TODO: ignoring this association for now
	// } else if (target instanceof DataStore || source instanceof DataStore) {
	// // TODO: ignoring data store associations for now
	// } else if (source instanceof EventNode) {
	// EventNode sourceNode = (EventNode) source;
	// Node targetNode = (Node) target;
	// checkBoundaryEventCompensationHandler(association, sourceNode,
	// targetNode);
	//
	// // make sure IsForCompensation is set to true on target
	// NodeImpl targetNodeImpl = (NodeImpl) target;
	// String isForCompensation = "isForCompensation";
	// Object compensationObject =
	// targetNodeImpl.getMetaData(isForCompensation);
	// if( compensationObject == null ) {
	// targetNodeImpl.setMetaData(isForCompensation, true);
	// logger.warn("Setting {} attribute to true for node {}",
	// isForCompensation, targetRef );
	// } else if( ! Boolean.parseBoolean(compensationObject.toString()) ) {
	// throw new IllegalArgumentException(isForCompensation + " attribute [" +
	// compensationObject + "] should be true for Compensation Activity [" +
	// targetRef + "]");
	// }
	//
	// // put Compensation Handler in CompensationHandlerNode
	// NodeContainer sourceParent = sourceNode.getNodeContainer();
	// NodeContainer targetParent = targetNode.getNodeContainer();
	// if( ! sourceParent.equals(targetParent) ) {
	// throw new
	// IllegalArgumentException("Compensation Associations may not cross (sub-)process boundaries,");
	// }
	//
	// // connect boundary event to compensation activity
	// ConnectionImpl connection = new ConnectionImpl(sourceNode,
	// NodeImpl.CONNECTION_DEFAULT_TYPE, targetNode,
	// NodeImpl.CONNECTION_DEFAULT_TYPE);
	// connection.setMetaData("UniqueId", null);
	// connection.setMetaData("hidden", true );
	// connection.setMetaData("association", true );
	//
	// // Compensation use cases:
	// // - boundary event --associated-> activity
	// // - implicit sub process compensation handler + recursive?
	//
	// /**
	// * BPMN2 spec, p.442:
	// * "A Compensation Event Sub-process becomes enabled when its parent
	// Activity transitions into state
	// * Completed. At that time, a snapshot of the data associated with the
	// parent Acitivity is taken and kept for
	// * later usage by the Compensation Event Sub-Process."
	// */
	// }
	// }
	// }
	// }
	//
	// /**
	// * This logic belongs in {@link RuleFlowProcessValidator} -- except that
	// {@link Association}s are a jbpm-bpmn2 class,
	// * and {@link RuleFlowProcessValidator} is a jbpm-flow class..
	// * </p>
	// * Maybe we should have a BPMNProcessValidator class?
	// *
	// * @param association The association to check.
	// * @param source The source of the association.
	// * @param target The target of the association.
	// */
	// private static void checkBoundaryEventCompensationHandler(Association
	// association, Node source, Node target) {
	// // check that
	// // - event node is boundary event node
	// if( ! (source instanceof BoundaryEventNode) ) {
	// throw new
	// IllegalArgumentException("(Compensation) activities may only be associated with Boundary Event Nodes (not with"
	// +
	// source.getClass().getSimpleName() + " nodes [node " + ((String)
	// source.getMetaData().get("UniqueId")) + "].");
	// }
	// BoundaryEventNode eventNode = (BoundaryEventNode) source;
	//
	// // - event node has compensationEvent
	// List<EventFilter> eventFilters = eventNode.getEventFilters();
	// boolean compensationCheckPassed = false;
	// if( eventFilters != null) {
	// for( EventFilter filter : eventFilters ) {
	// if( filter instanceof EventTypeFilter ) {
	// String type = ((EventTypeFilter) filter).getType();
	// if( type != null && type.equals("Compensation") ) {
	// compensationCheckPassed = true;
	// }
	// }
	// }
	// }
	//
	// if( ! compensationCheckPassed ) {
	// throw new IllegalArgumentException("An Event [" + ((String)
	// eventNode.getMetaData("UniqueId"))
	// + "] linked from an association [" + association.getId()
	// + "] must be a (Boundary) Compensation Event.");
	// }
	//
	// // - boundary event node is attached to the correct type of node?
	// /**
	// * Tasks:
	// * business: RuleSetNode
	// * manual: WorkItemNode
	// * receive: WorkItemNode
	// * script: ActionNode
	// * send: WorkItemNode
	// * service: WorkItemNode
	// * task: WorkItemNode
	// * user: HumanTaskNode
	// */
	// String attachedToId = eventNode.getAttachedToNodeId();
	// Node attachedToNode = null;
	// for( Node node : eventNode.getNodeContainer().getNodes() ) {
	// if( attachedToId.equals(node.getMetaData().get("UniqueId")) ) {
	// attachedToNode = node;
	// break;
	// }
	// }
	// if( attachedToNode == null ) {
	// throw new IllegalArgumentException("Boundary Event [" + ((String)
	// eventNode.getMetaData("UniqueId"))
	// + "] is not attached to a node [" + attachedToId +
	// "] that can be found.");
	// }
	// if( !( attachedToNode instanceof RuleSetNode
	// || attachedToNode instanceof WorkItemNode
	// || attachedToNode instanceof ActionNode
	// || attachedToNode instanceof HumanTaskNode
	// || attachedToNode instanceof CompositeNode) ) {
	// throw new IllegalArgumentException("Compensation Boundary Event [" +
	// ((String) eventNode.getMetaData("UniqueId"))
	// + "] must be attached to a task or sub-process.");
	// }
	//
	// // - associated node is a task or subProcess
	// compensationCheckPassed = false;
	// if( target instanceof WorkItemNode || target instanceof HumanTaskNode
	// || target instanceof CompositeContextNode ) {
	// compensationCheckPassed = true;
	// } else if( target instanceof ActionNode ) {
	// Object nodeTypeObj = ((ActionNode) target).getMetaData("NodeType");
	// if( nodeTypeObj != null && nodeTypeObj.equals("ScriptTask") ) {
	// compensationCheckPassed = true;
	// }
	// }
	// if( ! compensationCheckPassed ) {
	// throw new IllegalArgumentException("An Activity ["
	// + ((String) ((NodeImpl)target).getMetaData("UniqueId")) +
	// "] associated with a Boundary Compensation Event must be a Task or a (non-Event) Sub-Process");
	// }
	//
	// // - associated node does not have outgoingConnections of it's own
	// compensationCheckPassed = true;
	// NodeImpl targetNode = (NodeImpl) target;
	// Map<String, List<org.kie.api.definition.process.Connection>>
	// connectionsMap = targetNode.getOutgoingConnections();
	// ConnectionImpl outgoingConnection = null;
	// for( String connectionType : connectionsMap.keySet() ) {
	// List<org.kie.api.definition.process.Connection> connections =
	// connectionsMap.get(connectionType);
	// if( connections != null && ! connections.isEmpty() ) {
	// for( org.kie.api.definition.process.Connection connection : connections )
	// {
	// Object hiddenObj = connection.getMetaData().get("hidden");
	// if( hiddenObj != null && ((Boolean) hiddenObj) ) {
	// continue;
	// }
	// outgoingConnection = (ConnectionImpl) connection;
	// compensationCheckPassed = false;
	// break;
	// }
	// }
	// }
	// if( ! compensationCheckPassed ) {
	// throw new IllegalArgumentException("A Compensation Activity ["
	// + ((String) targetNode.getMetaData("UniqueId"))
	// + "] may not have any outgoing connection ["
	// + (String) outgoingConnection.getMetaData("UniqueId") + "]");
	// }
	// }
	//
	// private void assignLanes(RuleFlowProcess process, List<Lane> lanes) {
	// List<String> laneNames = new ArrayList<String>();
	// Map<String, String> laneMapping = new HashMap<String, String>();
	// if (lanes != null) {
	// for (Lane lane: lanes) {
	// String name = lane.getName();
	// if (name != null) {
	// Swimlane swimlane = new Swimlane();
	// swimlane.setName(name);
	// process.getSwimlaneContext().addSwimlane(swimlane);
	// laneNames.add(name);
	// for (String flowElementRef: lane.getFlowElements()) {
	// laneMapping.put(flowElementRef, name);
	// }
	// }
	// }
	// }
	// assignLanes(process, laneMapping);
	// }
	//
	// private void postProcessNodes(RuleFlowProcess process, NodeContainer
	// container) {
	// for (Node node: container.getNodes()) {
	// if (node instanceof StateNode) {
	// StateNode stateNode = (StateNode) node;
	// String condition = (String) stateNode.getMetaData("Condition");
	// Constraint constraint = new ConstraintImpl();
	// constraint.setConstraint(condition);
	// constraint.setType("rule");
	// for (org.kie.api.definition.process.Connection connection:
	// stateNode.getDefaultOutgoingConnections()) {
	// stateNode.setConstraint(connection, constraint);
	// }
	// } else if (node instanceof NodeContainer) {
	// // prepare event sub process
	// if (node instanceof EventSubProcessNode) {
	// EventSubProcessNode eventSubProcessNode = (EventSubProcessNode) node;
	//
	// Node[] nodes = eventSubProcessNode.getNodes();
	// for (Node subNode : nodes) {
	// // avoids cyclomatic complexity
	// if (subNode == null || ! (subNode instanceof StartNode)) {
	// continue;
	// }
	// List<Trigger> triggers = ((StartNode) subNode).getTriggers();
	// if ( triggers == null ) {
	// continue;
	// }
	// for ( Trigger trigger : triggers ) {
	// if ( trigger instanceof EventTrigger ) {
	// final List<EventFilter> filters = ((EventTrigger)
	// trigger).getEventFilters();
	//
	// for ( EventFilter filter : filters ) {
	// if ( filter instanceof EventTypeFilter ) {
	// eventSubProcessNode.addEvent((EventTypeFilter) filter);
	//
	// String type = ((EventTypeFilter) filter).getType();
	// if (type.startsWith("Error-") || type.startsWith("Escalation")) {
	// String replaceRegExp = "Error-|Escalation-";
	// final String signalType = type;
	//
	// ExceptionScope exceptionScope = (ExceptionScope) ((ContextContainer)
	// eventSubProcessNode.getNodeContainer()).getDefaultContext(ExceptionScope.EXCEPTION_SCOPE);
	// if (exceptionScope == null) {
	// exceptionScope = new ExceptionScope();
	// ((ContextContainer)
	// eventSubProcessNode.getNodeContainer()).addContext(exceptionScope);
	// ((ContextContainer)
	// eventSubProcessNode.getNodeContainer()).setDefaultContext(exceptionScope);
	// }
	// ActionExceptionHandler exceptionHandler = new ActionExceptionHandler();
	// DroolsConsequenceAction action = new DroolsConsequenceAction("java",
	// PROCESS_INSTANCE_SIGNAL_EVENT + signalType+"\", null);");
	// exceptionHandler.setAction(action);
	// exceptionScope.setExceptionHandler(type.replaceFirst(replaceRegExp, ""),
	// exceptionHandler);
	// } else if( type.equals("Compensation") ) {
	// // 1. Find the parent sub-process to this event sub-process
	// NodeContainer parentSubProcess;
	// NodeContainer subProcess = eventSubProcessNode.getNodeContainer();
	// Object isForCompensationObj =
	// eventSubProcessNode.getMetaData("isForCompensation");
	// if( isForCompensationObj == null ) {
	// eventSubProcessNode.setMetaData("isForCompensation", true );
	// logger.warn(
	// "Overriding empty or false value of \"isForCompensation\" attribute on Event Sub-Process ["
	// + eventSubProcessNode.getMetaData("UniqueId") +
	// "] and setting it to true.");
	// }
	// if( subProcess instanceof RuleFlowProcess ) {
	// // If jBPM deletes the process (instance) as soon as the process
	// completes..
	// // ..how do you expect to signal compensation on the completed process
	// (instance)?!?
	// throw new
	// IllegalArgumentException("Compensation Event Sub-Processes at the process level are not supported.");
	// }
	// parentSubProcess = ((Node) subProcess).getNodeContainer();
	//
	// // 2. The event filter (never fires, purely for dumping purposes) has
	// already been added
	//
	// // 3. Add compensation scope
	// String compensationHandlerId = (String) ((CompositeNode)
	// subProcess).getMetaData("UniqueId");
	// addCompensationScope(process, eventSubProcessNode, parentSubProcess,
	// compensationHandlerId);
	// }
	// }
	// }
	// } else if (trigger instanceof ConstraintTrigger) {
	// ConstraintTrigger constraintTrigger = (ConstraintTrigger) trigger;
	//
	// if (constraintTrigger.getConstraint() != null) {
	// String processId = ((RuleFlowProcess) container).getId();
	// String type = "RuleFlowStateEventSubProcess-" + processId + "-" +
	// eventSubProcessNode.getUniqueId();
	// EventTypeFilter eventTypeFilter = new EventTypeFilter();
	// eventTypeFilter.setType(type);
	// eventSubProcessNode.addEvent(eventTypeFilter);
	// }
	// }
	// }
	// } // for( Node subNode : nodes)
	// }
	// postProcessNodes(process, (NodeContainer) node);
	// } else if( node instanceof EndNode ) {
	// handleIntermediateOrEndThrowCompensationEvent((EndNode) node);
	// } else if( node instanceof ActionNode ) {
	// handleIntermediateOrEndThrowCompensationEvent((ActionNode) node);
	// }
	// }
	// }
	//
	// private void assignLanes(NodeContainer nodeContainer, Map<String, String>
	// laneMapping) {
	// for (Node node: nodeContainer.getNodes()) {
	// String lane = null;
	// String uniqueId = (String) node.getMetaData().get("UniqueId");
	// if (uniqueId != null) {
	// lane = laneMapping.get(uniqueId);
	// } else {
	// lane = laneMapping.get(XmlBPMNProcessDumper.getUniqueNodeId(node));
	// }
	// if (lane != null) {
	// ((NodeImpl) node).setMetaData("Lane", lane);
	// if (node instanceof HumanTaskNode) {
	// ((HumanTaskNode) node).setSwimlane(lane);
	// }
	// }
	// if (node instanceof NodeContainer) {
	// assignLanes((NodeContainer) node, laneMapping);
	// }
	// }
	// }
	//
	// private static Constraint buildConstraint(SequenceFlow connection,
	// NodeImpl node) {
	// if (connection.getExpression() == null) {
	// return null;
	// }
	//
	// Constraint constraint = new ConstraintImpl();
	// String defaultConnection = (String) node.getMetaData("Default");
	// if (defaultConnection != null &&
	// defaultConnection.equals(connection.getId())) {
	// constraint.setDefault(true);
	// }
	// if (connection.getName() != null) {
	// constraint.setName(connection.getName());
	// } else {
	// constraint.setName("");
	// }
	// if (connection.getType() != null) {
	// constraint.setType(connection.getType());
	// } else {
	// constraint.setType("code");
	// }
	// if (connection.getLanguage() != null) {
	// constraint.setDialect(connection.getLanguage());
	// }
	// if (connection.getExpression() != null) {
	// constraint.setConstraint(connection.getExpression());
	// }
	// constraint.setPriority(connection.getPriority());
	//
	// return constraint;
	// }
	//
	// protected static void addCompensationScope(final RuleFlowProcess process,
	// final Node node,
	// final org.kie.api.definition.process.NodeContainer parentContainer, final
	// String compensationHandlerId) {
	// process.getMetaData().put("Compensation", true);
	//
	// assert parentContainer instanceof ContextContainer
	// : "Expected parent node to be a CompositeContextNode, not a " +
	// parentContainer.getClass().getSimpleName();
	//
	// ContextContainer contextContainer = (ContextContainer) parentContainer;
	// CompensationScope scope = null;
	// boolean addScope = false;
	// if (contextContainer.getContexts(CompensationScope.COMPENSATION_SCOPE) ==
	// null) {
	// addScope = true;
	// } else {
	// scope = (CompensationScope)
	// contextContainer.getContexts(CompensationScope.COMPENSATION_SCOPE).get(0);
	// if (scope == null) {
	// addScope = true;
	// }
	// }
	// if (addScope) {
	// scope = new CompensationScope();
	// contextContainer.addContext(scope);
	// contextContainer.setDefaultContext(scope);
	// scope.setContextContainer(contextContainer);
	// }
	//
	// CompensationHandler handler = new CompensationHandler();
	// handler.setNode(node);
	// if( scope.getExceptionHandler(compensationHandlerId) != null ) {
	// throw new IllegalArgumentException(
	// "More than one compensation handler per node (" + compensationHandlerId +
	// ")" + " is not supported!");
	// }
	// scope.setExceptionHandler(compensationHandlerId, handler);
	// }
	//
	// protected void
	// handleIntermediateOrEndThrowCompensationEvent(ExtendedNodeImpl
	// throwEventNode ) {
	// if( throwEventNode.getMetaData("compensation-activityRef") != null ) {
	// String activityRef = (String)
	// throwEventNode.getMetaData().remove("compensation-activityRef");
	//
	// NodeContainer nodeParent = (NodeContainer)
	// throwEventNode.getNodeContainer();
	// if( nodeParent instanceof EventSubProcessNode ) {
	// boolean compensationEventSubProcess = false;
	// List<Trigger> startTriggers = ((EventSubProcessNode)
	// nodeParent).findStartNode().getTriggers();
	// CESP_CHECK: for( Trigger trigger : startTriggers ) {
	// if( trigger instanceof EventTrigger ) {
	// for( EventFilter filter : ((EventTrigger) trigger).getEventFilters() ) {
	// if( ((EventTypeFilter) filter).getType().equals("Compensation") ) {
	// compensationEventSubProcess = true;
	// break CESP_CHECK;
	// }
	// }
	// }
	// }
	// if( compensationEventSubProcess ) {
	// // BPMN2 spec, p. 252, p. 248: intermediate and end compensation event
	// visibility scope
	// nodeParent = (NodeContainer) ((NodeImpl) nodeParent).getNodeContainer();
	// }
	// }
	// String parentId;
	// if( nodeParent instanceof RuleFlowProcess ) {
	// parentId = ((RuleFlowProcess) nodeParent).getId();
	// } else {
	// parentId = (String) ((NodeImpl) nodeParent).getMetaData("UniqueId");
	// }
	//
	// String compensationEvent;
	// if( activityRef.length() == 0 ) {
	// // general/implicit compensation
	// compensationEvent = CompensationScope.IMPLICIT_COMPENSATION_PREFIX +
	// parentId;
	// } else {
	// // specific compensation
	// compensationEvent = activityRef;
	// }
	//
	// DroolsConsequenceAction compensationAction = new
	// DroolsConsequenceAction("java",
	// PROCESS_INSTANCE_SIGNAL_EVENT + "Compensation\", \"" + compensationEvent
	// + "\");");
	//
	// if( throwEventNode instanceof ActionNode ) {
	// ((ActionNode) throwEventNode).setAction(compensationAction);
	// } else if( throwEventNode instanceof EndNode ) {
	// List<DroolsAction> actions = new ArrayList<DroolsAction>();
	// actions.add(compensationAction);
	// ((EndNode) throwEventNode).setActions(EndNode.EVENT_NODE_ENTER, actions);
	// }
	// }
	// }
	//
}
